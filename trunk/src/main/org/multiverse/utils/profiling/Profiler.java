package org.multiverse.utils.profiling;

/**
 * Perhaps not needed to tell, but a Profiler should have the lowest amount of performance overhead
 * if possible.
 *
 * @author Peter Veentjer.
 */
public interface Profiler {

    void startEvent(String key1, String key2);

    void endEvent(String key1, String key2);

    void incCounter(String key1, String key2);

    void incCounter(String key1, String key2, long count);

    long getCount(String key1, String key2);

    long countOnKey1(String key1);

    long countOnKey2(String key2);

    void decCounter(String key1, String key2);

    void reset();

    String getProfileInfo();

    void print();
}
