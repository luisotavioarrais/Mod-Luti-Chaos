package me.rcbaito.hytale.plugin;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ChallengeDeckTest {
    @Test void allEventsAreDrawnExactlyOnceAndThenRunEnds() {
        for(int seed=0; seed<100; seed++) {
            var deck = new ChallengeDeck<Integer>(new Random(seed));
            var events = java.util.stream.IntStream.range(0,12).boxed().toList(); deck.reset(events);
            var seen = new HashSet<Integer>();
            for(int i=1;i<=12;i++) { assertTrue(seen.add(deck.draw(e -> true))); assertEquals(i,deck.drawn()); assertEquals(12,deck.total()); }
            assertEquals(new HashSet<>(events),seen); assertNull(deck.draw(e -> true)); assertEquals(12,deck.drawn());
        }
    }
    @Test void disabledEntriesAreSkippedAndRemovedFromDenominator() {
        var deck = new ChallengeDeck<String>(new Random(0)); deck.reset(List.of("a","b","c"));
        assertEquals("a", deck.draw(e -> e.equals("a"))); assertEquals(1,deck.total()); assertNull(deck.draw(e -> true));
    }
    @Test void resetStartsANewRunAndEmptyRunEndsImmediately() {
        var deck = new ChallengeDeck<String>(new Random()); deck.reset(List.of("one"));
        assertEquals("one",deck.draw(e -> true)); assertNull(deck.draw(e -> true));
        deck.reset(List.of("one")); assertEquals(0,deck.drawn()); assertEquals("one",deck.draw(e -> true));
        deck.reset(List.of()); assertEquals(0,deck.total()); assertNull(deck.draw(e -> true));
    }
}
