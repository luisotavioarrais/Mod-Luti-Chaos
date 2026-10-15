package me.rcbaito.hytale.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.math.vector.Transform;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport; // Importe o Teleport

import java.util.Collections;

public class MlgEvent implements ChaosEvent {

    @Override
    public String getId() {
        return "mlg";
    }

    @Override
    public String getName() { return "MLG Challenge"; }

    @Override
    public String getDescription() { return "You are teleported high into the sky and given a single Water Bucket."; }

    @Override
    public int getDuration() {
        return 30;
    }

    @Override
    public boolean isEffect() {
        return true;
    }



    @Override
    public void start(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            me.rcbaito.hytale.events.Helper.EventHelper.enableAirControl(store, ref);
            Transform current = TargetUtil.getLook(ref, store);
            Vector3d pos = current.getPosition();
            Vector3d newPos = new Vector3d(pos.x, pos.y + 300, pos.z);

            Teleport teleportReq = Teleport.createForPlayer(
                    world,
                    newPos,
                    current.getRotation()
            );

            store.putComponent(
                    ref,
                    Teleport.getComponentType(),
                    teleportReq
            );

            var inv = com.hypixel.hytale.server.core.inventory.InventoryComponent.getCombined(store, ref, com.hypixel.hytale.server.core.inventory.InventoryComponent.HOTBAR_FIRST);
            if (inv != null) {
                inv.addItemStacks(
                        Collections.singletonList(new ItemStack("*Container_Bucket_State_Filled_Water", 1))
                );
            }
        } catch (Exception e) {
            throw new IllegalStateException("Event failed", e);
        }
    }

    @Override public void tick(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        // Applied only in start: landing ends steering for this event.
    }
    @Override
    public void stop(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        me.rcbaito.hytale.events.Helper.EventHelper.restoreAirControl(store, ref);

    }
}



