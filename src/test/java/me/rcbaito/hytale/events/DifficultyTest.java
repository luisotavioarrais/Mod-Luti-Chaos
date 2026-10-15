package me.rcbaito.hytale.events;
import com.hypixel.hytale.protocol.MovementSettings;
import me.rcbaito.hytale.events.Helper.AirControlState;
import me.rcbaito.hytale.plugin.ChaosEventRegistry;
import me.rcbaito.hytale.hud.EventText;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class DifficultyTest {
    @Test void helpfulEventsCannotBeDrawnEvenWithOldConfiguration() {
        var excluded = Set.of("poop", "rifle", "creative", "tank", "sonic", "superjump", "randomarmor", "2xbar", "5xbar", "chickenrain");
        for (var event : ChaosEventRegistry.getEvents()) {
            assertFalse(excluded.contains(event.getId()), event.getId());
            var text = EventText.forId(event.getId(), event.getName());
            assertFalse(text.line1().isBlank());
        }
        assertTrue(ChaosEventRegistry.getEvents().stream().anyMatch(e -> e.getId().equals("randommob")));
    }
    @Test void mlgSteeringRestoresOriginalSettingsWithoutChangingJumpOrFlight() {
        var s = new MovementSettings();
        s.airSpeedMultiplier=0.8f; s.airControlMinSpeed=2; s.airControlMaxSpeed=20;
        s.airControlMinMultiplier=0.4f; s.airControlMaxMultiplier=0.02f;
        s.jumpForce=9; s.baseSpeed=5.5f;
        var before = new MovementSettings(s);
        var saved = AirControlState.capture(s);
        AirControlState.enableSteering(s);
        assertEquals(1, s.airControlMinMultiplier);
        assertEquals(1, s.airControlMaxMultiplier);
        assertEquals(before.jumpForce, s.jumpForce);
        assertEquals(before.baseSpeed, s.baseSpeed);
        assertEquals(before.fly, s.fly);
        saved.restore(s);
        assertEquals(before, s);
    }
}

