package me.rcbaito.hytale.events;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class GamemodeCreativeEvent implements ChaosEvent{ public static final java.util.Map<Ref<EntityStore>, GameMode> original = new java.util.HashMap<>();

    @Override
    public String getId() {
        return "creative";
    }

    @Override
    public String getName() { return "/Creative"; }

    @Override
    public String getDescription() { return "Players enter Creative Mode."; }

    @Override
    public int getDuration() {
        return 10;
    }

    @Override
    public boolean isEffect() {
        return true;
    }

    @Override
    public void start(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        original.putIfAbsent(ref, player.getGameMode()); Player.setGameMode(ref, GameMode.Creative,store);
    }

    @Override
    public void stop(Player player, World world, Store<EntityStore> store, Ref<EntityStore> ref) {
        GameMode prior = original.remove(ref); if (prior != null) Player.setGameMode(ref, prior, store);
    }
}
