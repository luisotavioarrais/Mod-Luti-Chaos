package me.rcbaito.hytale.events;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import me.rcbaito.hytale.events.Helper.EventHelper;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/** Curated hostile roles verified in the installed 0.7.1 asset pack. */
public final class HostileMobEvent implements ChaosEvent {
    public static final Map<String, String> HOSTILES = Map.of(
        "Spider", "Aranha", "Wolf_Black", "Lobo Negro", "Skeleton_Fighter", "Esqueleto Guerreiro",
        "Trork_Warrior", "Trork Guerreiro", "Toad_Rhino_Magma", "Sapo de Magma");
    private final String id, name;
    private final int count;
    public HostileMobEvent(String id, String name, int count) { this.id=id; this.name=name; this.count=count; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return count + " inimigo(s) aleatório(s) aparece(m) por perto."; }
    public int getDuration() { return 30; }
    public boolean isEffect() { return false; }
    public void start(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        var available = HOSTILES.keySet().stream().filter(role -> NPCPlugin.get().getIndex(role) >= 0).sorted().toList();
        if (available.isEmpty()) throw new IllegalStateException("No compatible hostile NPC roles available");
        var names = new ArrayList<String>();
        for (int i=0; i<count; i++) {
            String role = available.get(ThreadLocalRandom.current().nextInt(available.size()));
            if (EventHelper.spawnEntity(store, ref, role, 1, 0.5) > 0) names.add(HOSTILES.get(role));
        }
        var p = store.getComponent(ref, PlayerRef.getComponentType());
        if (p != null) p.sendMessage(Message.raw(names.isEmpty() ? "Limite de mobs atingido." : "Inimigos sorteados: " + String.join(", ", names)));
    }
    public void stop(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {}
}
