package me.rcbaito.hytale.events.Helper;
import com.hypixel.hytale.protocol.MovementSettings;
/** Only the air steering fields are touched; no flight, gravity or invulnerability. */
public record AirControlState(float speed, float minSpeed, float maxSpeed, float minControl, float maxControl) {
    public static AirControlState capture(MovementSettings s) {
        return new AirControlState(s.airSpeedMultiplier, s.airControlMinSpeed, s.airControlMaxSpeed, s.airControlMinMultiplier, s.airControlMaxMultiplier);
    }
    public static void enableSteering(MovementSettings s) {
        s.airSpeedMultiplier = Math.max(s.airSpeedMultiplier, 1.5f);
        s.airControlMinSpeed = 0; s.airControlMaxSpeed = 1000;
        s.airControlMinMultiplier = 1; s.airControlMaxMultiplier = 1;
    }
    public void restore(MovementSettings s) {
        s.airSpeedMultiplier=speed; s.airControlMinSpeed=minSpeed; s.airControlMaxSpeed=maxSpeed;
        s.airControlMinMultiplier=minControl; s.airControlMaxMultiplier=maxControl;
    }
}
