package me.rcbaito.hytale.plugin;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.rcbaito.hytale.events.ChaosEvent;
import me.rcbaito.hytale.events.Helper.EventHelper;
import me.rcbaito.hytale.hud.RandomChaosHUD;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.*;

/** All ECS operations are confined to the owning world's executor. One challenge per plugin. */
public final class ChaosGameManager {
    public static final ChaosConfig config = new ChaosConfig();
    public static final Logger LOG = Logger.getLogger("LutiChaos");
    public static volatile boolean isRunning;
    public static volatile ScheduledFuture<?> activeTask;
    public static volatile int chaosTime = 30;
    public static int maxChaosTime = 30, timerSpeed = 1;
    public static volatile World currentWorld;
    public static Store<EntityStore> currentStore;
    public static final Map<UUID, PlayerRef> affectedPlayers = new ConcurrentHashMap<>();
    public static final List<EventActive> activeEvents = new CopyOnWriteArrayList<>();
    private static final Set<String> failed = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, RandomChaosHUD> huds = new HashMap<>();
    private static final ChaosClock clock = new ChaosClock(System::nanoTime);
    private static final AtomicBoolean queued = new AtomicBoolean();
    private static final ChallengeDeck<ChaosEvent> deck = new ChallengeDeck<>(new Random());
    private static volatile long generation;
    public static String lastEvent = "Aguardando primeiro evento";
    public static String descriptionLine1 = "Um evento será sorteado em breve.";
    public static String descriptionLine2 = "";
    public static int challengeNumber, challengeTotal;
    private static long lastTick;
    private static int warned = -1;
    private record Running(ChaosEvent event, long expires, List<PlayerRef> players) {}
    private static final List<Running> running = new ArrayList<>();

    public static synchronized void startLoop() {
        if (isRunning || currentWorld == null || !currentWorld.isAlive()) return;
        stopLoop();
        deck.reset(ChaosEventRegistry.getEvents().stream().filter(ChaosGameManager::eligible).toList());
        challengeNumber = 0; challengeTotal = deck.total();
        isRunning = true;
        lastEvent = "Aguardando primeiro evento";
        descriptionLine1 = "Um evento será sorteado em breve."; descriptionLine2 = "";
        clock.reset(maxChaosTime); chaosTime = maxChaosTime; warned = -1;
        lastTick = System.nanoTime();
        updateHUD();
        long token = ++generation;
        World world = currentWorld;
        activeTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> {
            if (!isRunning || token != generation) return;
            if (!world.isAlive()) { cancelTimer(); return; }
            if (!queued.compareAndSet(false, true)) return;
            try { world.execute(() -> {
                try { if (isRunning && token == generation) pulse(); }
                catch (Exception e) { LOG.log(Level.SEVERE, "Chaos pulse failed", e); stopLoop(); }
                finally { queued.set(false); }
            }); } catch (Exception e) { queued.set(false); LOG.log(Level.SEVERE, "World rejected Chaos task", e); cancelTimer(); }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }
    private static void clearDeadPlayerEffects() {
        for (var p : affectedPlayers.values()) {
            if (!belongs(p) || currentStore.getComponent(p.getReference(), com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) == null) continue;
            for (var r : List.copyOf(running)) if (r.players.contains(p)) {
                if (r.event.isEffect()) invoke(r.event, p, 2);
                running.remove(r);
                running.add(new Running(r.event, r.expires, r.players.stream().filter(x -> !x.getUuid().equals(p.getUuid())).toList()));
                cleanup(p);
            }
        }
    }
    private static void pulse() {
        long now = System.nanoTime();
        clearDeadPlayerEffects();
        EventHelper.processAirControlTick();
        for (var r : List.copyOf(running)) {
            if (now >= r.expires || failed.contains(r.event.getId())) { finish(r); continue; }
            if (now - lastTick >= 1_000_000_000L) for (var p : r.players) invoke(r.event, p, 1);
        }
        if (now - lastTick >= 1_000_000_000L) { EventHelper.processMovementTick(); lastTick = now; }
        int remaining = clock.remaining();
        if (remaining != chaosTime) { chaosTime = remaining; updateHUD(); }
        if (remaining <= 5 && remaining > 0 && warned != remaining) {
            warned = remaining;
            if (remaining == 5) for (var p : affectedPlayers.values()) p.sendMessage(Message.raw("LUTI CHAOS: evento em 5 segundos!"));
        }
        if (remaining == 0) { triggerRandomEvent(); clock.reset(maxChaosTime); warned = -1; }
    }
    public static synchronized void cancelTimer() {
        isRunning = false; generation++;
        if (activeTask != null) activeTask.cancel(false);
        activeTask = null;
    }
    public static void stopLoop() {
        cancelTimer();
        for (var r : List.copyOf(running)) finish(r);
        EventHelper.cleanupSpawned();
        for (var p : List.copyOf(affectedPlayers.values())) { try { cleanup(p); } catch (Exception e) { LOG.log(Level.SEVERE, "Player cleanup failed: " + p.getUuid(), e); } }
        timerSpeed = 1; chaosTime = maxChaosTime;
    }
    public static void shutdown() {
        World w = currentWorld; cancelTimer();
        if (w != null && w.isAlive()) w.execute(ChaosGameManager::stopLoop);
    }
    public static boolean belongs(PlayerRef p) {
        var ref = p == null ? null : p.getReference();
        return ref != null && ref.isValid() && ref.getStore() == currentStore;
    }
    private static void invoke(ChaosEvent e, PlayerRef p, int phase) {
        if (!belongs(p)) return;
        var ref = p.getReference();
        Player player = currentStore.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        if (phase != 2 && currentStore.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) != null) return;
        try {
            if (phase == 0) e.start(player, currentWorld, currentStore, ref);
            else if (phase == 1) e.tick(player, currentWorld, currentStore, ref);
            else e.stop(player, currentWorld, currentStore, ref);
        } catch (Exception | LinkageError ex) {
            failed.add(e.getId());
            LOG.log(Level.SEVERE, "Disabled event " + e.getId() + " phase=" + phase + " player=" + p.getUuid(), ex);
        }
    }
    private static boolean eligible(ChaosEvent e) { return config.enabled(e.getId()) && !failed.contains(e.getId()); }
    public static void changeInterval(int seconds) {
        maxChaosTime = seconds; clock.reset(seconds); chaosTime = seconds; warned = -1;
        if (isRunning) updateHUD();
    }
    public static void triggerRandomEvent() {
        for (var r : List.copyOf(running)) finish(r); // Avoid conflicting movement/damage modifiers.
        ChaosEvent event = deck.draw(ChaosGameManager::eligible);
        challengeNumber = deck.drawn(); challengeTotal = deck.total();
        if (event == null) {
            String message = challengeNumber > 0 ? "LUTI CHAOS concluído! " + challengeNumber + "/" + challengeTotal + " desafios sem repetir. Use /chaos start para uma nova sequência." : "LUTI CHAOS: nenhum desafio ativo disponível.";
            for (var p : affectedPlayers.values()) p.sendMessage(Message.raw(message));
            stopLoop();
            lastEvent = "Desafio concluído";
            return;
        }
        var text = me.rcbaito.hytale.hud.EventText.forId(event.getId(), event.getName());
        lastEvent = text.name();
        descriptionLine1 = text.line1(); descriptionLine2 = text.line2();
        var players = affectedPlayers.values().stream().filter(ChaosGameManager::belongs).toList();
        EventHelper.beginSpawnBudget(config.maxEntities);
        var r = new Running(event, System.nanoTime() + config.effectDuration * 1_000_000_000L, players);
        running.add(r); // Register before start, so partial failures still receive cleanup.
        for (var p : players) { invoke(event, p, 0); p.sendMessage(Message.raw("LUTI CHAOS [" + challengeNumber + "/" + challengeTotal + "]: " + lastEvent + " — " + descriptionLine1 + " " + descriptionLine2)); }
        updateHUD();
    }
    private static void finish(Running r) {
        for (var p : r.players) { if (r.event.isEffect()) invoke(r.event, p, 2); }
        running.remove(r);
        EventHelper.cleanupSpawned();
    }
    public static String eventState(ChaosEvent e) { return failed.contains(e.getId()) ? "FALHOU (ver log)" : config.enabled(e.getId()) ? "ativo" : "desativado"; }
    public static void addPlayer(PlayerRef p) { if (belongs(p)) affectedPlayers.put(p.getUuid(), p); }
    public static void removePlayer(PlayerRef p) {
        for (var r : running) if (r.players.contains(p) && r.event.isEffect()) invoke(r.event, p, 2);
        // Remove from every event snapshot to prevent further ticks.
        for (var r : List.copyOf(running)) if (r.players.contains(p)) {
            running.remove(r); running.add(new Running(r.event, r.expires, r.players.stream().filter(x -> x != p).toList()));
        }
        cleanup(p); affectedPlayers.remove(p.getUuid());
    }
    private static void cleanup(PlayerRef p) {
        if (belongs(p)) {
            var ref = p.getReference();
            EventHelper.restoreMovement(currentStore, ref);
            EventHelper.setPlayerDamage(currentStore, ref, 1);
            EventHelper.setFragile(currentStore, ref, false);
            EventHelper.resetStat(currentStore, ref, com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), "tank_hp");
            EventHelper.resetStat(currentStore, ref, com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth(), "glass_hp");
            Player player = currentStore.getComponent(ref, Player.getComponentType());
            if (player != null) player.getHudManager().removeCustomHud(p, "luti-chaos");
        }
        huds.remove(p.getUuid());
    }
    private static void updateHUD() {
        if (!config.hud) return;
        for (var p : affectedPlayers.values()) if (belongs(p)) {
            try {
                Player player = currentStore.getComponent(p.getReference(), Player.getComponentType());
                if (player == null) continue;
                var hud = huds.get(p.getUuid());
                if (hud == null) { hud = new RandomChaosHUD(p); huds.put(p.getUuid(), hud); player.getHudManager().addCustomHud(p, hud); }
                else hud.refresh();
            } catch (Exception ex) { config.hud = false; LOG.log(Level.WARNING, "HUD disabled for this session after update failure", ex); }
        }
    }
}




