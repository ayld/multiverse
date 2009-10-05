package org.multiverse.utils.profiling;

import java.util.Iterator;

/**
 * A repository for profiling information.
 * <p/>
 * Perhaps not needed to tell, but a Profiler should have the lowest amount of
 * performance overhead if possible.
 *
 * @author Peter Veentjer.
 */
public interface ProfileDataRepository {

    void incCounter(String key);

    void incCounter(String key, int amount);

    void incCounter(String key1, String key2);

    void incCounter(String key1, String key2, long count);

    Iterator<String> getKey1Iterator();

    long getCount(String key1, String key2);

    long sumKey1(String key2);

    long sumKey2(String key1);

    void decCounter(String key1, String key2);

    void reset();
}
