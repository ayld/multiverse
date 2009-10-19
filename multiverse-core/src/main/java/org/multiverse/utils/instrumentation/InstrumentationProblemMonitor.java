package org.multiverse.utils.instrumentation;

/**
 * Since it is not possible to disrupt the instrumentation process if problems
 * are encountered, some kind of warning mechanism needs to be created. That
 * is the task of this InstrumentationProblemMonitor.
 * <p/>
 * What is does is it launches a thread that prints warning messages every
 * 10 second to the System.err when the first problem is signalled. Following
 * problems are ignored.
 *
 * @author Peter Veentjer
 */
public final class InstrumentationProblemMonitor {

    public final static InstrumentationProblemMonitor INSTANCE = new InstrumentationProblemMonitor();

    private InstrumentationProblemMonitor() {
    }

    private volatile boolean isSignalled;

    public boolean isSignalled() {
        return isSignalled;
    }

    public void signalProblem() {
        if (isSignalled) {
            return;
        }

        synchronized (this) {
            isSignalled = true;
            new WarningThread().start();
        }
    }

    static class WarningThread extends Thread {
        WarningThread() {
            super("InstrumentationProblemMonitor-WarningThread");
            setDaemon(true);
        }

        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                System.err.println("STM integrity compromised, instrumentation problems encountered");
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
    }
}
