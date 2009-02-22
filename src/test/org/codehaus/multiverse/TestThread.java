package org.codehaus.multiverse;

import static org.junit.Assert.assertNull;

public class TestThread extends Thread {

    private volatile Throwable throwable;

    public TestThread(String name) {
        this();
        setName(name);
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

    public void assertNoThrowable() {
        assertNull(throwable);
    }
}
