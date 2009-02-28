package org.codehaus.multiverse.multiversionedheap.listenersupport;

import org.codehaus.multiverse.util.latches.Latch;

public interface VersionedLatchGroup {

    void activateVersion(long newActiveVersion);

    void addLatch(long newActiveVersion, Latch latch);
}
