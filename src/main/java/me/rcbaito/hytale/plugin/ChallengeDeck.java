package me.rcbaito.hytale.plugin;
import java.util.*;
import java.util.function.Predicate;
/** One finite challenge run: a drawn entry never returns until reset. */
public final class ChallengeDeck<T> {
    private final Random random;
    private final List<T> pending = new ArrayList<>();
    private int drawn;
    public ChallengeDeck(Random random) { this.random = random; }
    public void reset(Collection<T> events) { pending.clear(); pending.addAll(new LinkedHashSet<>(events)); drawn=0; }
    public T draw(Predicate<T> eligible) {
        pending.removeIf(eligible.negate());
        if (pending.isEmpty()) return null;
        T event = pending.remove(random.nextInt(pending.size()));
        drawn++; return event;
    }
    public int drawn() { return drawn; }
    public int total() { return drawn + pending.size(); }
}
