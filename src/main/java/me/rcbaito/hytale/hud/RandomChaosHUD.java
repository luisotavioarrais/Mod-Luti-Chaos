package me.rcbaito.hytale.hud;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import me.rcbaito.hytale.plugin.ChaosGameManager;
public final class RandomChaosHUD extends CustomUIHud {
    public RandomChaosHUD(PlayerRef ref) { super(ref, "luti-chaos"); }
    @Override public void build(UICommandBuilder b) { b.append("Hud/ChaosBar.ui"); values(b); }
    public void refresh() { var b = new UICommandBuilder(); values(b); update(false, b); }
    static void values(UICommandBuilder b) {
        int t = ChaosGameManager.chaosTime;
        b.set("#Timer.Text", "LUTI CHAOS  |  " + t + "s");
        b.set("#ChallengeCount.Text", "Desafio " + ChaosGameManager.challengeNumber + "/" + ChaosGameManager.challengeTotal);
        b.set("#Event.Text", ChaosGameManager.lastEvent);
        b.set("#Description1.Text", ChaosGameManager.descriptionLine1);
        b.set("#Description2.Text", ChaosGameManager.descriptionLine2);
        b.set("#Warning.Visible", t <= 5);
        int width = Math.clamp((int)(280.0 * t / ChaosGameManager.maxChaosTime), 0, 280);
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(width));
        anchor.setHeight(Value.of(6));
        b.setObject("#ChaosProgressBar.Anchor", anchor);
        b.set("#Speed.Text", ChaosGameManager.timerSpeed > 1 ? ChaosGameManager.timerSpeed + "X SPEED BAR (visual)" : "");
        b.set("#Speed.Visible", ChaosGameManager.timerSpeed > 1 && (t % ChaosGameManager.timerSpeed != 0));
    }
}



