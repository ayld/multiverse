package org.codehaus.multiverse.util;

import java.util.concurrent.TimeUnit;

/**
 * A {@link Latch} that already is open. It is a special case implementation.
 *
 * @author Peter Veentjer.
 */
public class OpenLatch implements Latch{

    public final static OpenLatch INSTANCE = new OpenLatch();

    public void tryAwait(long timeout, TimeUnit unit) {
        //ignore
    }

    public void await() {
        //ignore
    }

    public void open() {
        //ignore
    }

    public boolean isOpen() {
        return true;
    }

    @Override
    public String toString(){
        return "OpenLatch";
    }
}
