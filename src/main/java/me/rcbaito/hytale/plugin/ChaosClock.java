package me.rcbaito.hytale.plugin;
import java.util.*;
import java.util.function.LongSupplier;
/** Monotonic deadlines: stalls never generate a burst of missed events. */
public final class ChaosClock {
    private final LongSupplier time;
    private long deadline;
    public ChaosClock(LongSupplier time) { this.time = time; }
    public void reset(int seconds) { deadline = time.getAsLong() + seconds * 1_000_000_000L; }
    public int remaining() { return (int)Math.max(0, (deadline - time.getAsLong() + 999_999_999L) / 1_000_000_000L); }
    public static <T> T choose(List<T> candidates, T previous, Random random) {
        var pool = new ArrayList<>(candidates);
        pool.remove(previous);
        return pool.isEmpty() ? null : pool.get(random.nextInt(pool.size()));
    }
}
