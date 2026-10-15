package me.rcbaito.hytale.plugin;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;
class ChaosCoreTest {
 @Test void thirtyRealSeconds() {
  var now = new AtomicLong(); var clock = new ChaosClock(now::get); clock.reset(30);
  assertEquals(30, clock.remaining()); now.set(29_000_000_000L); assertEquals(1, clock.remaining());
  now.set(30_000_000_000L); assertEquals(0, clock.remaining());
 }
 @Test void stallDoesNotCreateCatchUpBurst() {
  var now = new AtomicLong(); var clock = new ChaosClock(now::get); clock.reset(30);
  now.set(300_000_000_000L); assertEquals(0, clock.remaining()); clock.reset(30); assertEquals(30, clock.remaining());
 }
 @Test void neverRepeatsConsecutively() {
  var random = new Random(4); String previous = null;
  for (int i=0;i<10000;i++) { String selected = ChaosClock.choose(List.of("a","b","c"), previous, random); assertNotEquals(previous, selected); previous=selected; }
 }
 @Test void singleEventIsSkippedInsteadOfRepeated() {
  assertNull(ChaosClock.choose(List.of("a"), "a", new Random()));
  assertNull(ChaosClock.choose(List.of(), null, new Random()));
 }
 @Test void safeConfigurationDefaults(@TempDir Path dir) {
  var c = new ChaosConfig(); c.load(dir);
  assertEquals(30,c.interval); assertTrue(c.hud); assertFalse(c.enabled("kill"));
  assertEquals(13, ChaosEventRegistry.getEvents().size());
 }
 @Test void configurationValidationAndIndividualSwitches(@TempDir Path dir) throws Exception {
  Files.writeString(dir.resolve("luti-chaos.properties"), "intervalSeconds=-1\neffectDurationSeconds=invalid\nmaxEntitiesPerEvent=10000\nallowInstantDeath=true\nevent.kill=false\nevent.sonic=false\n");
  var c = new ChaosConfig(); c.load(dir);
  assertEquals(5,c.interval); assertEquals(30,c.effectDuration); assertEquals(100,c.maxEntities); assertFalse(c.enabled("kill")); assertFalse(c.enabled("sonic"));
 }
 @Test void instantDeathRequiresOptIn(@TempDir Path dir) throws Exception {
  Files.writeString(dir.resolve("luti-chaos.properties"), "allowInstantDeath=true\nevent.kill=true\n");
  var c = new ChaosConfig(); c.load(dir); assertTrue(c.enabled("kill"));
 }
}


