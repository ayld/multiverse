package org.multiverse.utils.profiling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 */
public class SimpleProfiler implements Profiler {

    private final ConcurrentMap<ComposedKey, AtomicLong> map = new ConcurrentHashMap<ComposedKey, AtomicLong>();

    @Override
    public void reset() {
        map.clear();
    }

    @Override
    public void print() {
        System.out.print(getProfileInfo());
    }

    @Override
    public String getProfileInfo() {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<ComposedKey, AtomicLong> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public void incCounter(String key1, String key2) {
        incCounter(key1, key2, 1);
    }

    @Override
    public void incCounter(String key1, String key2, long count) {
        AtomicLong counter = getCounter(key1, key2);
        counter.incrementAndGet();
    }

    @Override
    public long getCount(String key1, String key2) {
        AtomicLong counter = getCounter(key1, key2);
        return counter == null ? -1 : counter.get();
    }

    @Override
    public void decCounter(String familyName, String key2) {
        incCounter(familyName, key2, -1);
    }

    @Override
    public void startEvent(String key1, String key2) {
        //todo
    }

    @Override
    public void endEvent(String key1, String key2) {
        //todo
    }

    @Override
    public long countOnKey1(String key1) {
        long result = 0;

        for (Map.Entry<ComposedKey, AtomicLong> entry : map.entrySet()) {
            if (entry.getKey().key1.equals(key1)) {
                result += entry.getValue().get();
            }
        }

        return result;
    }

    @Override
    public long countOnKey2(String key2) {
        long result = 0;

        for (Map.Entry<ComposedKey, AtomicLong> entry : map.entrySet()) {
            if (entry.getKey().key2.equals(key2)) {
                result += entry.getValue().get();
            }
        }

        return result;
    }

    public AtomicLong getCounter(String key1, String key2) {
        ComposedKey key = new ComposedKey(key1, key2);
        AtomicLong counter = map.get(key);
        if (counter == null) {
            counter = new AtomicLong();
            AtomicLong found = map.putIfAbsent(key, counter);
            if (found != null) {
                counter = found;
            }
        }

        return counter;
    }

    static class ComposedKey {
        final String key1;
        final String key2;

        ComposedKey(String key1, String key2) {
            this.key1 = key1;
            this.key2 = key2;
        }

        @Override
        public String toString() {
            return key1 + "#" + key2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComposedKey that = (ComposedKey) o;

            if (key1 != null ? !key1.equals(that.key1) : that.key1 != null) return false;
            if (key2 != null ? !key2.equals(that.key2) : that.key2 != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = key1 != null ? key1.hashCode() : 0;
            result = 31 * result + (key2 != null ? key2.hashCode() : 0);
            return result;
        }
    }
}
