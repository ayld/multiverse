package org.codehaus.multiverse.util;

import java.util.concurrent.TimeUnit;

public interface Latch {

    void await() throws InterruptedException;

    void tryAwait(long timeout, TimeUnit unit)throws InterruptedException;

    /**
     * Opens the latch. If the latch already is open, the call is ignored.
     */
    void open();

    boolean isOpen();
}
