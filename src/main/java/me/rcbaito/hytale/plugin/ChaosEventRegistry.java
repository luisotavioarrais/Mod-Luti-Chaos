package me.rcbaito.hytale.plugin;

import me.rcbaito.hytale.events.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChaosEventRegistry {

    private static final List<ChaosEvent> events = new ArrayList<>();
    private static final Random random = new Random();

    static {
        events.add(new KillEvent()); // Explicit opt-in only.
        events.add(new MlgEvent());
        events.add(new ChickenRainEvent()); // Now spider rain.
        events.add(new ToadSquadEvent());
        events.add(new GlassCanonEvent()); // Fragile health, without a damage bonus.
        events.add(new PacifistEvent());
        events.add(new TheSnailEvent());
        events.add(new CementShoesEvent());
        events.add(new HostileMobEvent("randommob", "Visitante Indesejado", 1));
        events.add(new HostileMobEvent("ambush", "Emboscada", 3));
        events.add(new HostileMobEvent("horde", "Horda Aleatória", 5));
        events.add(new HindranceEvent(false));
        events.add(new HindranceEvent(true));
    }
    public static void register(ChaosEvent event) {
        events.add(event);
    }

    public static ChaosEvent getRandomEvent() {
        if (events.isEmpty()) return null;
        return events.get(random.nextInt(events.size()));
    }

    public static List<ChaosEvent> getEvents() {
        return events;
    }
}


