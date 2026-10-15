package me.rcbaito.hytale.plugin;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.rcbaito.hytale.events.Helper.EventHelper;
/** Restore data before the player holder is saved or transferred to another world. */
public final class ChaosPlayerLifecycle extends RefSystem<EntityStore> {
    @Override public Query<EntityStore> getQuery() { return Player.getComponentType(); }
    @Override public void onEntityAdded(Ref<EntityStore> ref, AddReason reason, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        // Migrate only our legacy health modifiers when a player enters the world.
        int health = com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes.getHealth();
        EventHelper.resetStat(store, ref, health, "glass_hp");
        EventHelper.resetStat(store, ref, health, "tank_hp");
    }
    @Override public void onEntityRemove(Ref<EntityStore> ref, RemoveReason reason, Store<EntityStore> store, CommandBuffer<EntityStore> cb) {
        if (store != ChaosGameManager.currentStore) return;
        var p = store.getComponent(ref, PlayerRef.getComponentType());
        if (p == null || !ChaosGameManager.affectedPlayers.containsKey(p.getUuid())) return;
        EventHelper.restoreBeforeRemoval(store, ref, p, cb);
        ChaosGameManager.affectedPlayers.remove(p.getUuid());
        if (ChaosGameManager.affectedPlayers.isEmpty()) { ChaosGameManager.cancelTimer(); cb.run(s -> ChaosGameManager.stopLoop()); }
    }
}

