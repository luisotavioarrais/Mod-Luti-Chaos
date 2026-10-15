package me.rcbaito.hytale.plugin;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class ChaosConfig {
    private final Properties values = new Properties();
    public int interval = 30, effectDuration = 30, maxEntities = 20;
    public boolean hud = true, instantDeath = false;
    public void load(Path directory) {
        try {
            Files.createDirectories(directory);
            Path file = directory.resolve("luti-chaos.properties");
            values.setProperty("intervalSeconds", "30");
            values.setProperty("effectDurationSeconds", "30");
            values.setProperty("maxEntitiesPerEvent", "20");
            values.setProperty("hud", "true");
            values.setProperty("allowInstantDeath", "false");
            for (var e : ChaosEventRegistry.getEvents()) values.setProperty("event." + e.getId(), "true");
            if (Files.exists(file)) try (var in = Files.newInputStream(file)) { values.load(in); }
            interval = number("intervalSeconds", 30, 5, 3600);
            effectDuration = number("effectDurationSeconds", 30, 1, 600);
            maxEntities = number("maxEntitiesPerEvent", 20, 0, 100);
            hud = Boolean.parseBoolean(values.getProperty("hud"));
            instantDeath = Boolean.parseBoolean(values.getProperty("allowInstantDeath"));
            try (var out = Files.newOutputStream(file)) { values.store(out, "Luti Chaos - restart world after editing; event.<id>=true/false"); }
        } catch (IOException e) { throw new IllegalStateException("Cannot load Luti Chaos configuration", e); }
    }
    private int number(String key, int fallback, int min, int max) {
        try { return Math.clamp(Integer.parseInt(values.getProperty(key)), min, max); }
        catch (NumberFormatException e) { return fallback; }
    }
    public boolean enabled(String id) {
        return (!id.equals("kill") || instantDeath) && Boolean.parseBoolean(values.getProperty("event." + id, "true"));
    }
}
