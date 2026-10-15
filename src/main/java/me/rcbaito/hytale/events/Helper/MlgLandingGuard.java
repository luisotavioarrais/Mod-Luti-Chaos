package me.rcbaito.hytale.events.Helper;
import com.hypixel.hytale.protocol.MovementStates;
/** Ignore the pre-teleport ground state; stop steering on the first landing or new jump. */
public final class MlgLandingGuard {
    private boolean airborne;
    public boolean shouldRestore(MovementStates s) {
        if (s == null) return false;
        if (s.inFluid || s.swimming || s.climbing) return true;
        if (airborne && (s.onGround || s.jumping)) return true;
        if (!s.onGround && !s.jumping && (s.falling || s.fallingFar)) airborne=true;
        return false;
    }
}
