package org.multiverse;

import static org.junit.Assert.assertNull;

/**
 * A TestThread that tracks if any throwable has been thrown by a thread.
 *
 * @author Peter Veentjer.
 */
public abstract class TestThread extends Thread {

    private volatile Throwable throwable;

    public TestThread(String name) {
        super(name);
    }

    public TestThread() {
        setUncaughtExceptionHandler(
                new UncaughtExceptionHandler() {
                    public void uncaughtException(Thread t, Throwable e) {
                        System.out.printf("Thread %s has thrown an exception\n", t.getName());
                        e.printStackTrace();
                        throwable = e;
                    }
                }
        );
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void assertNothingThrown() {
        assertNull(throwable);
    }
}
