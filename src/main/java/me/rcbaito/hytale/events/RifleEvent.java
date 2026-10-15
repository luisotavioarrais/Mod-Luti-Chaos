package me.rcbaito.hytale.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.Collections;

public class RifleEvent implements ChaosEvent {

    @Override
    public String getId() {
        return "rifle";
    }

    @Override
    public String getName() { return "RIFLE"; }

    @Override
    public String getDescription() { return "Players receive rifles."; }

    @Override
    public int getDuration() {
        return 0;
    }

    @Override
    public boolean isEffect() {
        return false;
    }



    @Override
    public void start(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        try {
            var inv = com.hypixel.hytale.server.core.inventory.InventoryComponent.getCombined(store, ref, com.hypixel.hytale.server.core.inventory.InventoryComponent.HOTBAR_FIRST);
            if (inv != null) {
                inv.addItemStacks(
                        Collections.singletonList(new ItemStack("Weapon_Assault_Rifle", 1))
                );
                inv.addItemStacks(Collections.singletonList(new ItemStack("Weapon_Arrow_Crude", 50)));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Event failed", e);
        }
    }

    @Override
    public void stop(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {

    }
}
