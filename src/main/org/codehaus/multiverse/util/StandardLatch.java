package org.codehaus.multiverse.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import static java.lang.String.format;

/**
 * A {@link Latch} implementation that is based on the {@link Lock}.
 *
 * @author Peter Veentjer.
 */
public final class StandardLatch implements Latch {

    private final Lock lock = new ReentrantLock();
    private final Condition isOpenCondition = lock.newCondition();
    private volatile boolean isOpen = false;

    public void await() throws InterruptedException {
        if(isOpen)
            return;

        lock.lockInterruptibly();
        try{
            while(!isOpen)
                isOpenCondition.await();
        } finally{
            lock.unlock();
        }
    }

    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        if(unit == null)throw new NullPointerException();
        throw new RuntimeException("Not implemented yet");
    }

    public void open() {
        if (isOpen)
            return;

        lock.lock();
        try {
            isOpen = true;
            isOpenCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString(){
        return format("StandardLatch(open=%s)",isOpen);
    }
}
