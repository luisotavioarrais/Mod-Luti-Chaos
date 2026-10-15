package me.rcbaito.hytale.events.Helper;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.OrderPriority;
import com.hypixel.hytale.component.dependency.SystemGroupDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import org.joml.Vector3f;
import com.hypixel.hytale.protocol.CalculationType;
import com.hypixel.hytale.protocol.EntityStatEffects;
import com.hypixel.hytale.protocol.packets.entities.ChangeVelocity;
import com.hypixel.hytale.server.core.command.commands.player.stats.PlayerStatsAddCommand;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.player.movement.MovementManager;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.npc.INonPlayerCharacter;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.server.npc.NPCPlugin;
import it.unimi.dsi.fastutil.Pair;
import me.rcbaito.hytale.plugin.ChaosGameManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EventHelper {

    public static class ChaosSpeedComponent implements Component<EntityStore> {
        public static ComponentType<EntityStore, ChaosSpeedComponent> TYPE;
        public float targetSpeed;
        public ChaosSpeedComponent() { this.targetSpeed = 5.5f; }
        public ChaosSpeedComponent(float speed) { this.targetSpeed = speed; }
        @Nullable @Override public Component<EntityStore> clone() { return new ChaosSpeedComponent(this.targetSpeed); }
    }

    public static class ChaosJumpComponent implements Component<EntityStore> {
        public static ComponentType<EntityStore, ChaosJumpComponent> TYPE;
        public float targetJump;
        public ChaosJumpComponent() { this.targetJump = 8.0f; } // 8.0f é um valor base aproximado
        public ChaosJumpComponent(float jump) { this.targetJump = jump; }
        @Nullable @Override public Component<EntityStore> clone() { return new ChaosJumpComponent(this.targetJump); }
    }

    public static class DamageMultiplierComponent implements Component<EntityStore> {
        public static ComponentType<EntityStore, DamageMultiplierComponent> TYPE;
        public float multiplier;
        public DamageMultiplierComponent() { this.multiplier = 1.0f; }
        public DamageMultiplierComponent(float multiplier) { this.multiplier = multiplier; }
        @Nullable @Override public Component<EntityStore> clone() { return new DamageMultiplierComponent(this.multiplier); }
    }

    public static void processMovementTick() {
        if (ChaosSpeedComponent.TYPE == null || ChaosJumpComponent.TYPE == null) return;

        for (PlayerRef pRef : ChaosGameManager.affectedPlayers.values()) {
            if (!ChaosGameManager.belongs(pRef)) continue;

            try {
                Ref<EntityStore> ref = pRef.getReference();
                Store<EntityStore> store = ref.getStore();

                ComponentType<EntityStore, MovementManager> moveType = EntityModule.get().getMovementManagerComponentType();
                MovementManager movement = store.getComponent(ref, moveType);

                if (movement == null) continue;

                boolean needsUpdate = false;

                ChaosSpeedComponent mySpeed = store.getComponent(ref, ChaosSpeedComponent.TYPE);
                if (mySpeed != null) {
                    if (movement.getSettings().baseSpeed != mySpeed.targetSpeed) {
                        movement.getSettings().baseSpeed = mySpeed.targetSpeed;
                        needsUpdate = true;
                    }
                }

                ChaosJumpComponent myJump = store.getComponent(ref, ChaosJumpComponent.TYPE);
                if (myJump != null) {
                    if (movement.getSettings().jumpForce != myJump.targetJump) {
                        movement.getSettings().jumpForce = myJump.targetJump;
                        needsUpdate = true;
                    }
                }

                if (needsUpdate) {
                    if (pRef.getPacketHandler() != null) {
                        movement.update(pRef.getPacketHandler());
                    }
                }

            } catch (Exception e) { throw new IllegalStateException("Movement operation failed", e); }
        }
    }

    private static final Map<Ref<EntityStore>, float[]> originalMovement = new java.util.HashMap<>();
    private static final java.util.List<Ref<EntityStore>> spawned = new java.util.ArrayList<>();
    private static int spawnBudget;
    private static final Map<Ref<EntityStore>, AirControlState> airControl = new java.util.HashMap<>();
    private static final Map<Ref<EntityStore>, MlgLandingGuard> landingGuards = new java.util.HashMap<>();
    private static final Map<Ref<EntityStore>, Long> airDeadlines = new java.util.HashMap<>();
    public static void processAirControlTick() {
        for (var ref : java.util.List.copyOf(airControl.keySet())) {
            if (!ref.isValid()) { airControl.remove(ref); landingGuards.remove(ref); airDeadlines.remove(ref); continue; }
            var store = ref.getStore();
            if (store != ChaosGameManager.currentStore) continue;
            boolean ended = System.nanoTime() >= airDeadlines.getOrDefault(ref, 0L)
                || store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent.getComponentType()) != null;
            // The client must acknowledge the teleport before its landing state is meaningful.
            if (!ended && store.getComponent(ref, com.hypixel.hytale.server.core.modules.entity.teleport.Teleport.getComponentType()) == null) {
                var states = store.getComponent(ref, com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent.getComponentType());
                ended = states != null && landingGuards.get(ref).shouldRestore(states.getMovementStates());
            }
            if (ended) restoreAirControl(store, ref);
        }
    }
    public static void enableAirControl(Store<EntityStore> store, Ref<EntityStore> ref) {
        var movement = store.getComponent(ref, MovementManager.getComponentType());
        if (movement == null) throw new IllegalStateException("MLG requires MovementManager");
        airControl.putIfAbsent(ref, AirControlState.capture(movement.getSettings()));
        landingGuards.putIfAbsent(ref, new MlgLandingGuard());
        airDeadlines.putIfAbsent(ref, System.nanoTime() + 20_000_000_000L);
        AirControlState.enableSteering(movement.getSettings());
        var p = store.getComponent(ref, PlayerRef.getComponentType());
        if (p != null) movement.update(p.getPacketHandler());
    }
    public static void restoreAirControl(Store<EntityStore> store, Ref<EntityStore> ref) {
        landingGuards.remove(ref); airDeadlines.remove(ref);
        var saved = airControl.remove(ref);
        var movement = store.getComponent(ref, MovementManager.getComponentType());
        if (saved != null && movement != null) {
            saved.restore(movement.getSettings());
            var p = store.getComponent(ref, PlayerRef.getComponentType());
            if (p != null) movement.update(p.getPacketHandler());
        }
    }
    public static void beginSpawnBudget(int limit) { spawnBudget = limit; }
    public static void cleanupSpawned() {
        for (var entity : java.util.List.copyOf(spawned)) {
            try { if (entity.isValid()) entity.getStore().removeEntity(entity, com.hypixel.hytale.component.RemoveReason.REMOVE); }
            catch (Exception e) { ChaosGameManager.LOG.log(java.util.logging.Level.WARNING, "NPC cleanup failed", e); }
        }
        spawned.clear();
    }
    private static void captureMovement(Store<EntityStore> store, Ref<EntityStore> ref) {
        var m = store.getComponent(ref, EntityModule.get().getMovementManagerComponentType());
        if (m != null) originalMovement.putIfAbsent(ref, new float[]{m.getSettings().baseSpeed, m.getSettings().jumpForce});
    }
    public static void restoreMovement(Store<EntityStore> store, Ref<EntityStore> ref) {
        restoreAirControl(store, ref);
        setPlayerSpeed(store, ref, 5.5f); setPlayerJump(store, ref, -1f);
        originalMovement.remove(ref);
    }
    public static void restoreBeforeRemoval(Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef p, CommandBuffer<EntityStore> cb) {
        var movement = store.getComponent(ref, EntityModule.get().getMovementManagerComponentType());
        landingGuards.remove(ref); airDeadlines.remove(ref);
        var air = airControl.remove(ref);
        if (air != null && movement != null) air.restore(movement.getSettings());
        var saved = originalMovement.remove(ref);
        if (saved != null && movement != null) { movement.getSettings().baseSpeed = saved[0]; movement.getSettings().jumpForce = saved[1]; }
        var stats = store.getComponent(ref, EntityStatMap.getComponentType());
        if (stats != null) { stats.removeModifier(DefaultEntityStatTypes.getHealth(), "tank_hp"); stats.removeModifier(DefaultEntityStatTypes.getHealth(), "glass_hp"); }
        var prior = me.rcbaito.hytale.events.GamemodeCreativeEvent.original.remove(ref);
        if (prior != null) com.hypixel.hytale.server.core.entity.entities.Player.setGameMode(ref, prior, cb);
        var player = store.getComponent(ref, com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        if (player != null) player.getHudManager().removeCustomHud(p, "luti-chaos");
    }
    // --- SETTERS ---

    public static void setPlayerSpeed(Store<EntityStore> store, Ref<EntityStore> ref, float speed) {
        if (ChaosSpeedComponent.TYPE == null) return; captureMovement(store, ref);

        if (store.getComponent(ref, ChaosSpeedComponent.TYPE) != null) {
            store.removeComponent(ref, ChaosSpeedComponent.TYPE);
        }

        if (speed == 5.5f) {
            forceResetMovement(store, ref, true, false);
        } else {
            store.addComponent(ref, ChaosSpeedComponent.TYPE, new ChaosSpeedComponent(speed));
        }
    }

    public static void setPlayerJump(Store<EntityStore> store, Ref<EntityStore> ref, float jumpForce) {
        if (ChaosJumpComponent.TYPE == null) return; captureMovement(store, ref);

        if (store.getComponent(ref, ChaosJumpComponent.TYPE) != null) {
            store.removeComponent(ref, ChaosJumpComponent.TYPE);
        }

        if (jumpForce == -1f) {
            forceResetMovement(store, ref, false, true);
        } else {
            store.addComponent(ref, ChaosJumpComponent.TYPE, new ChaosJumpComponent(jumpForce));
        }
    }

    private static void forceResetMovement(Store<EntityStore> store, Ref<EntityStore> ref, boolean resetSpeed, boolean resetJump) {
        try {
            ComponentType<EntityStore, MovementManager> moveType = EntityModule.get().getMovementManagerComponentType();
            MovementManager movement = store.getComponent(ref, moveType);
            if (movement == null) return;

            if (resetSpeed && originalMovement.containsKey(ref)) movement.getSettings().baseSpeed = originalMovement.get(ref)[0];
            if (resetJump && originalMovement.containsKey(ref)) movement.getSettings().jumpForce = originalMovement.get(ref)[1];

            for (PlayerRef pRef : ChaosGameManager.affectedPlayers.values()) {
                if (ref.equals(pRef.getReference()) && pRef.getPacketHandler() != null) {
                    movement.update(pRef.getPacketHandler());
                    break;
                }
            }
        } catch (Exception e) { throw new IllegalStateException("Movement operation failed", e); }
    }



    public static class FragileComponent implements Component<EntityStore> {
        public static ComponentType<EntityStore, FragileComponent> TYPE;
        @Override public Component<EntityStore> clone() { return new FragileComponent(); }
    }
    public static void setFragile(Store<EntityStore> store, Ref<EntityStore> ref, boolean enabled) {
        if (FragileComponent.TYPE == null) return;
        if (enabled) store.putComponent(ref, FragileComponent.TYPE, new FragileComponent());
        else if (store.getComponent(ref, FragileComponent.TYPE) != null) store.removeComponent(ref, FragileComponent.TYPE);
    }
    public static float incomingDamage(float amount, boolean fragile) { return fragile && amount > 0 ? amount * 3f : amount; }
    public static class DamageSystem extends DamageEventSystem {
        public DamageSystem() { super(); }

        @Override
        public void handle(int i, @Nonnull ArchetypeChunk<EntityStore> chunk, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> cb, @Nonnull Damage damage) {
            var victim = chunk.getReferenceTo(i);
            boolean fragile = FragileComponent.TYPE != null && store.getComponent(victim, FragileComponent.TYPE) != null;
            damage.setAmount(incomingDamage(damage.getAmount(), fragile));
            Ref<EntityStore> attacker = damage.getSource() instanceof Damage.EntitySource source ? source.getRef() : null;
            if (attacker != null && attacker.isValid() && attacker.getStore() == store && DamageMultiplierComponent.TYPE != null) {
                DamageMultiplierComponent comp = store.getComponent(attacker, DamageMultiplierComponent.TYPE);
                if (comp != null && comp.multiplier != 1.0f) {
                    damage.setAmount(damage.getAmount() * comp.multiplier);
                }
            }
        }

        @Nonnull @Override public Query<EntityStore> getQuery() { return Query.any(); } // Query simples
        @Nullable @Override public SystemGroup<EntityStore> getGroup() { return DamageModule.get().getFilterDamageGroup(); }
        @Nonnull @Override public Set<Dependency<EntityStore>> getDependencies() { return Collections.emptySet(); }
    }

    public static boolean hasDamageComponent(Store<EntityStore> store, Ref<EntityStore> ref) {
        if (DamageMultiplierComponent.TYPE == null) return false;
        return store.getComponent(ref, DamageMultiplierComponent.TYPE) != null;
    }

    public static void setPlayerDamage(Store<EntityStore> store, Ref<EntityStore> ref, float multiplier) {
        if (DamageMultiplierComponent.TYPE == null) return;
        if (store.getComponent(ref, DamageMultiplierComponent.TYPE) != null) store.removeComponent(ref, DamageMultiplierComponent.TYPE);
        if (multiplier != 1.0f) store.addComponent(ref, DamageMultiplierComponent.TYPE, new DamageMultiplierComponent(multiplier));
    }


    public static void modifyStat(Store<EntityStore> store, Ref<EntityStore> ref, int rawStat, String key, float desiredMultiplier, StaticModifier.CalculationType type) {
        try {
            EntityStatMap stats = store.getComponent(ref, EntityStatMap.getComponentType());
            if (stats != null) {
                int statId = rawStat;
                if (statId == -1) return;

                stats.removeModifier(statId, key);

                float fixedValue = desiredMultiplier;
                stats.putModifier(statId, key, new StaticModifier(Modifier.ModifierTarget.MAX, type, fixedValue));
                stats.maximizeStatValue(EntityStatMap.Predictable.ALL,statId);

            }
        } catch (Exception e) { throw new IllegalStateException("Stat modifier failed", e); }
    }

    /** Remove only our modifier. Never revive, heal or replace the stat component during death. */
    public static void resetStat(Store<EntityStore> store, Ref<EntityStore> ref, int statId, String key) {
        var stats = store.getComponent(ref, EntityStatMap.getComponentType());
        if (stats != null && statId >= 0 && stats.getModifier(statId, key) != null) stats.removeModifier(statId, key);
    }
    public static int spawnEntity(Store<EntityStore> store, Ref<EntityStore> ref, String entityName, int count, double yOffset) {
        try {
            Transform transform = TargetUtil.getLook(ref, store);
            Vector3d centerPos = transform.getPosition();

            int created = 0;
            for (int i = 0; i < count && spawnBudget > 0; i++) { spawnBudget--;
                double angle = Math.random() * Math.PI * 2; double radius = 2.5 + Math.random() * 1.5;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;
                Vector3d spawnPos = new Vector3d(centerPos.x + offsetX, centerPos.y + yOffset, centerPos.z + offsetZ);
                var rotation = new com.hypixel.hytale.math.vector.Rotation3f();

                Pair<Ref<EntityStore>, INonPlayerCharacter> result = NPCPlugin.get().spawnNPC(store, entityName, null, spawnPos, rotation);
                if (result == null) throw new IllegalStateException("NPC unavailable: " + entityName); else { spawned.add(result.first()); created++; }
            }
            return created;
        } catch (Exception e) {
            throw new IllegalStateException("NPC spawn failed: " + entityName, e);
        }
    }
}



