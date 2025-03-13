package io.collective;

import java.time.Clock;
import java.time.Instant;

public class SimpleAgedCache {
    private final Clock clock;
    private ExpirableEntry head;
    private int size;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
        this.head = null;
        this.size = 0;
    }

    public SimpleAgedCache() {
        this(Clock.systemDefaultZone());
    }

    public void put(Object key, Object value, int retentionInMillis) {
        Instant expiryTime = clock.instant().plusMillis(retentionInMillis);
        ExpirableEntry newEntry = new ExpirableEntry(key, value, expiryTime);
        newEntry.next = head;
        head = newEntry;
        size++;
    }

    public boolean isEmpty() {
        cleanUp();
        return size == 0;
    }

    public int size() {
        cleanUp();
        return size;
    }

    public Object get(Object key) {
        cleanUp();
        ExpirableEntry current = head;
        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    private void cleanUp() {
        Instant now = clock.instant();
        ExpirableEntry prev = null;
        ExpirableEntry current = head;

        while (current != null) {
            if (current.expiryTime.isBefore(now)) {
                if (prev == null) {
                    head = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
            } else {
                prev = current;
            }
            current = current.next;
        }
    }

    private static class ExpirableEntry {
        private final Object key;
        private final Object value;
        private final Instant expiryTime;
        private ExpirableEntry next;

        ExpirableEntry(Object key, Object value, Instant expiryTime) {
            this.key = key;
            this.value = value;
            this.expiryTime = expiryTime;
            this.next = null;
        }
    }
}
