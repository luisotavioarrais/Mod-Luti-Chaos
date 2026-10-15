package me.rcbaito.hytale.events;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.rcbaito.hytale.events.Helper.EventHelper;
/** Fragility uses the normal damage pipeline, never maximum-health manipulation. */
public final class GlassCanonEvent implements ChaosEvent {
    public String getId() { return "glasscanon"; }
    public String getName() { return "Vida de Vidro"; }
    public String getDescription() { return "Você recebe três vezes mais dano, sem mudar a vida máxima."; }
    public int getDuration() { return 30; }
    public boolean isEffect() { return true; }
    public void start(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) { EventHelper.setFragile(s,r,true); }
    public void tick(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) {}
    public void stop(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) { EventHelper.setFragile(s,r,false); }
}
