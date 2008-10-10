package org.codehaus.multiverse.util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public final class StandardLatch implements Latch {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private volatile boolean open = false;

    public void await() throws InterruptedException {
        if(open)
            return;

        lock.lockInterruptibly();
        try{
            while(!open)
                condition.await();
        } finally{
            lock.unlock();
        }
    }

    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        if(unit == null)throw new NullPointerException();

        throw new RuntimeException("Not implemented yet");
    }

    public void open() {
        if (open)
            return;

        lock.lock();
        try {
            open = true;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public boolean isOpen() {
        return open;
    }
}
