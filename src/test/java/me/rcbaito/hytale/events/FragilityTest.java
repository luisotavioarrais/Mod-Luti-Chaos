package me.rcbaito.hytale.events;
import me.rcbaito.hytale.events.Helper.EventHelper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FragilityTest {
    @Test void damageReturnsToNormalWithoutLeavingAHealthModifier() {
        assertEquals(30f, EventHelper.incomingDamage(10f,true));
        assertEquals(10f, EventHelper.incomingDamage(10f,false));
        assertEquals(0f, EventHelper.incomingDamage(0f,true));
        assertEquals(-5f, EventHelper.incomingDamage(-5f,true));
    }

}

