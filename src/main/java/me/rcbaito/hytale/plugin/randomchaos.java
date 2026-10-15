package me.rcbaito.hytale.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.rcbaito.hytale.commands.ChaosCommand;
import me.rcbaito.hytale.events.Helper.EventHelper;

import javax.annotation.Nonnull;

public class randomchaos extends JavaPlugin {

    public randomchaos(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        ChaosGameManager.config.load(getDataDirectory());
        ChaosGameManager.maxChaosTime = ChaosGameManager.config.interval;
        getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.universe.world.events.RemoveWorldEvent.class, event -> { if (event.getWorld() == ChaosGameManager.currentWorld) ChaosGameManager.shutdown(); });
        getCommandRegistry().registerCommand(new ChaosCommand());        getEventRegistry().registerGlobal(com.hypixel.hytale.server.core.event.events.player.RemovedPlayerFromWorldEvent.class, event -> {
            var holder = event.getHolder();
            holder.tryRemoveComponent(EventHelper.ChaosSpeedComponent.TYPE);
            holder.tryRemoveComponent(EventHelper.ChaosJumpComponent.TYPE);
            holder.tryRemoveComponent(EventHelper.DamageMultiplierComponent.TYPE);
            holder.tryRemoveComponent(EventHelper.FragileComponent.TYPE);
        });

        try {
            System.out.println("[Chaos] Attempting to register ECS components...");

            EventHelper.DamageMultiplierComponent.TYPE =
                    this.getEntityStoreRegistry().registerComponent(
                            EventHelper.DamageMultiplierComponent.class,
                            EventHelper.DamageMultiplierComponent::new
                    );

            EventHelper.FragileComponent.TYPE = getEntityStoreRegistry().registerComponent(EventHelper.FragileComponent.class, EventHelper.FragileComponent::new);
            this.getEntityStoreRegistry().registerSystem(new EventHelper.DamageSystem());

            EventHelper.ChaosSpeedComponent.TYPE =
                    this.getEntityStoreRegistry().registerComponent(
                            EventHelper.ChaosSpeedComponent.class,
                            EventHelper.ChaosSpeedComponent::new
                    );
            EventHelper.ChaosJumpComponent.TYPE =
                    this.getEntityStoreRegistry().registerComponent(
                            EventHelper.ChaosJumpComponent.class,
                            EventHelper.ChaosJumpComponent::new
                    );
            this.getEntityStoreRegistry().registerSystem(new ChaosPlayerLifecycle());
            System.out.println("[Chaos] SUCCESS: All systems and components loaded!");

        } catch (Exception e) {
            System.out.println("[Chaos] CRITICAL ERROR IN SETUP: " + e.getMessage());
            throw new IllegalStateException("Luti Chaos ECS registration failed", e);
        }
    }
    @Override protected void shutdown() { ChaosGameManager.shutdown(); }
}

