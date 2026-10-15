package me.rcbaito.hytale.events;
import me.rcbaito.hytale.events.Helper.MlgLandingGuard;
import com.hypixel.hytale.protocol.MovementStates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MlgLandingTest {
    @Test void initialGroundDoesNotCancelTeleportButLandingDoes() {
        var guard = new MlgLandingGuard(); var s = new MovementStates();
        s.onGround=true; assertFalse(guard.shouldRestore(s));
        s.onGround=false; s.falling=true; assertFalse(guard.shouldRestore(s));
        s.falling=false; s.onGround=true; assertTrue(guard.shouldRestore(s));
    }
    @Test void jumpingImmediatelyAfterLandingAlsoEndsSteering() {
        var guard = new MlgLandingGuard(); var s = new MovementStates();
        s.falling=true; assertFalse(guard.shouldRestore(s));
        s.falling=false; s.jumping=true; assertTrue(guard.shouldRestore(s));
    }
    @Test void waterLandingEndsSteering() {
        var guard = new MlgLandingGuard(); var s = new MovementStates();
        s.inFluid=true; assertTrue(guard.shouldRestore(s));
    }
}
