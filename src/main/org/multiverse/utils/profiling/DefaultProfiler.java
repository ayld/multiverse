package org.multiverse.utils.profiling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultProfiler implements Profiler {

    private final ConcurrentMap<ComposedKey, AtomicLong> map = new ConcurrentHashMap<ComposedKey, AtomicLong>();

    @Override
    public void reset() {
        map.clear();
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
    public AtomicLong getCounter(String familyName, String eventName) {
        ComposedKey key = new ComposedKey(familyName, eventName);
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
        final String s1;
        final String s2;

        ComposedKey(String s1, String s2) {
            this.s1 = s1;
            this.s2 = s2;
        }

        @Override
        public String toString() {
            return s1 + "." + s2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ComposedKey that = (ComposedKey) o;

            if (s1 != null ? !s1.equals(that.s1) : that.s1 != null) return false;
            if (s2 != null ? !s2.equals(that.s2) : that.s2 != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = s1 != null ? s1.hashCode() : 0;
            result = 31 * result + (s2 != null ? s2.hashCode() : 0);
            return result;
        }
    }
}
