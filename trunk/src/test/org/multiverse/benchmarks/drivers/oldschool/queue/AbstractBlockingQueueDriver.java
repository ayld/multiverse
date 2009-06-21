package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.TestCaseResult;
import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBlockingQueueDriver extends AbstractDriver {

    //the object moved in the queue, same object is reused to prevent unwanted gc.
    private static final Object ITEM = new Object();

    private long count;
    private int threadCount;

    private QueueThread[] queueThreads;
    private BlockingQueue queue;
    private StarterThread starterThread;

    public abstract BlockingQueue createQueue();

    /**
     * In some cases (as with the SynchronousBlockingQueue) the system won't run until there is a
     * take.
     */
    public boolean needsStartingTake() {
        return false;
    }

    public int outputMod() {
        return 1000 * 1000;
    }

    @Override
    public void preRun(TestCase testCase) {
        threadCount = testCase.getIntProperty("threadCount");
        count = testCase.getLongProperty("count");
        queue = createQueue();

        queueThreads = new QueueThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            queueThreads[k] = new QueueThread(k, count);
        }

        if (needsStartingTake()) {
            starterThread = new StarterThread();
        } else {
            starterThread = null;
        }
    }

    @Override
    public void postRun(TestCaseResult caseResult) {
        long transactions = count * threadCount;
        long durationNs = caseResult.getLongProperty("duration(ns)");
        double transactionSec = (TimeUnit.SECONDS.toNanos(1) * transactions / durationNs);
        caseResult.put("transactions/second", transactionSec);
    }


    @Override
    public void run() {
        if (starterThread != null) {
            startAll(starterThread);
        }

        startAll(queueThreads);
        joinAll(queueThreads);

        if (starterThread != null) {
            joinAll(starterThread);
        }
    }

    private class QueueThread extends TestThread {
        private final long count;

        public QueueThread(int id, long count) {
            super("QueueThread-" + id);
            this.count = count;
        }

        @Override
        public void run() {
            try {
                for (long k = 0; k < count; k++) {
                    queue.put(ITEM);
                    queue.take();

                    if (k % outputMod() == 0 && k > 0) {
                        System.out.printf("%s is at %s\n", getName(), k);
                    }
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException();
            }
        }
    }

    private class StarterThread extends TestThread {
        public StarterThread() {
            super("StarterThread");
        }

        @Override
        public void run() {
            try {
                System.out.println("Starter thread started");
                queue.take();
                queue.put(ITEM);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
