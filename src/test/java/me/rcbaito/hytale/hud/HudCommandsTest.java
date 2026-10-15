package me.rcbaito.hytale.hud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import me.rcbaito.hytale.plugin.ChaosGameManager;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;
class HudCommandsTest {
    @Test void progressUsesWholeAnchorObjectAcceptedByClient() {
        ChaosGameManager.maxChaosTime = 30;
        for (int seconds : new int[]{30, 15, 5, 0}) {
            ChaosGameManager.chaosTime = seconds;
            var builder = new UICommandBuilder();
            RandomChaosHUD.values(builder);
            var commands = Arrays.asList(builder.getCommands());
            assertFalse(commands.stream().anyMatch(c -> c.selector.endsWith(".Anchor.Width")));
            var anchor = commands.stream().filter(c -> c.selector.equals("#ChaosProgressBar.Anchor")).findFirst().orElseThrow();
            assertNotNull(anchor.data);
            assertTrue(anchor.data.contains("Width"));
            assertTrue(anchor.data.contains("Height"));
        }
    }
}
