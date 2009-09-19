package org.multiverse.utils.profiling;

/**
 * A repository for profiling information.
 * <p/>
 * Perhaps not needed to tell, but a Profiler should have the lowest amount of
 * performance overhead if possible.
 *
 * @author Peter Veentjer.
 */
public interface Profiler {

    void incCounter(String key1, String key2);

    void incCounter(String key1, String key2, long count);

    long getCount(String key1, String key2);

    long sumKey1(String key2);

    long sumKey2(String key1);

    void decCounter(String key1, String key2);

    void reset();

    String getProfileInfo();

    void print();
}
