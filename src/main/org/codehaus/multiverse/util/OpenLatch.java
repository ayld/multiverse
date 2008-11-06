package org.codehaus.multiverse.util;

import java.util.concurrent.TimeUnit;

/**
 * A {@link Latch} that already is open.
 *
 * @author Peter Veentjer.
 */
public class OpenLatch implements Latch{

    public final static OpenLatch INSTANCE = new OpenLatch();

    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        //ignore
    }

    public void await() throws InterruptedException {
        //ignore
    }

    public void open() {
        //ignore
    }

    public boolean isOpen() {
        return true;
    }
}
