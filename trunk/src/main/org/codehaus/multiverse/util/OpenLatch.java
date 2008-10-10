package org.codehaus.multiverse.util;

import java.util.concurrent.TimeUnit;

public class OpenLatch implements Latch{

    public final static OpenLatch INSTANCE = new OpenLatch();

    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
    }

    public void await() throws InterruptedException {
    }

    public void open() {
    }

    public boolean isOpen() {
        return true;
    }
}
