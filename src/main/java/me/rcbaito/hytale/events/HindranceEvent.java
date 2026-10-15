package me.rcbaito.hytale.events;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import me.rcbaito.hytale.events.Helper.EventHelper;

public final class HindranceEvent implements ChaosEvent {
    private final boolean heavy;
    public HindranceEvent(boolean heavy) { this.heavy = heavy; }
    public String getId() { return heavy ? "heavy" : "weakarms"; }
    public String getName() { return heavy ? "Peso Morto" : "Braços Fracos"; }
    public String getDescription() { return heavy ? "Movimento lento e pulo baixo." : "Você causa apenas 25% do dano normal."; }
    public boolean isEffect() { return true; }
    public int getDuration() { return 30; }
    public void start(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) {
        if (heavy) { EventHelper.setPlayerSpeed(s,r,1.5f); EventHelper.setPlayerJump(s,r,3f); }
        else EventHelper.setPlayerDamage(s,r,0.25f);
    }
    public void tick(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) { start(p,w,s,r); }
    public void stop(Player p, World w, Store<EntityStore> s, Ref<EntityStore> r) {
        if (heavy) EventHelper.restoreMovement(s,r); else EventHelper.setPlayerDamage(s,r,1);
    }
}
