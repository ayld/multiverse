package org.multiverse.applications;

import static org.multiverse.api.StmUtils.executePostCommit;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.collections.DoubleLinkedList;
import org.multiverse.datastructures.collections.DoubleLinkedQueue;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

/**
 * STM based implementation of the {@link Executor}. It is created to gain more experience with
 * stm structures.
 * <p/>
 * Atm is has a fixed threadpool.
 * <p/>
 * A big problem is the queue: if there are multiple workers, there is a big change that they
 * are going to pick up the same task and start processing it and eventually come to the conclusion
 * that all but one can't commit. This is causes by the non concurrent nature of the Queue.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class StmExecutor implements Executor {

    private final DoubleLinkedList<WorkerThread> workerThreads;
    private final DoubleLinkedQueue<Runnable> workQueue;
    private StmExecutorState state;
    private int poolSize;

    public StmExecutor(int poolSize, int maxCapacity) {
        if (poolSize < 0) {
            throw new IllegalArgumentException();
        }

        this.workQueue = new DoubleLinkedQueue<Runnable>(maxCapacity);
        this.state = StmExecutorState.started;
        this.workerThreads = new DoubleLinkedList<WorkerThread>();
        this.poolSize = poolSize;
    }

    public void execute(Runnable task) {
        if (task == null) {
            throw new NullPointerException();
        }

        if (state != StmExecutorState.started) {
            throw new RejectedExecutionException();
        }

        workQueue.push(task);

        if (poolSize > workerThreads.size() && workQueue.size() > 1) {
            createWorkers(1);
        }
    }

    public int getPoolSize() {
        return poolSize;
    }

    public int getActualPoolsize() {
        return workerThreads.size();
    }

    public boolean isShutdown() {
        return state == StmExecutorState.shutdown;
    }

    public void setPoolSize(int newPoolSize) {
        if (state != StmExecutorState.started) {
            throw new IllegalStateException();
        }

        createWorkers(newPoolSize - poolSize);
        this.poolSize = newPoolSize;
    }

    private void createWorkers(int workerCount) {
        if (workerCount > 0) {
            DoubleLinkedList<WorkerThread> newWorkers = new DoubleLinkedList<WorkerThread>();
            for (int k = 0; k < workerCount; k++) {
                newWorkers.add(new WorkerThread(this));
            }
            workerThreads.addAll(newWorkers);
            executePostCommit(new StartWorkerThreadsTask(newWorkers));
        }
    }

    public void shutdown() {
        if (workerThreads.isEmpty()) {
            state = StmExecutorState.shutdown;
        } else {
            state = StmExecutorState.shutdownInProgress;
        }
    }

    public void awaitShutdown() {
        if (!isShutdown()) {
            retry();
        }
    }

    //needed to overcome an instrumentation issue
    private StmExecutorState getState() {
        return state;
    }

    //needed to overcome an instrumentation issue
    private void setState(StmExecutorState state) {
        this.state = state;
    }

    //needed to overcome an instrumentation issue
    private DoubleLinkedList<WorkerThread> getWorkerThreads() {
        return workerThreads;
    }

    //needs to be a static inner class for now.
    static class WorkerThread extends Thread {
        private final StmExecutor executor;

        public WorkerThread(StmExecutor executor) {
            setName("Worker");
            this.executor = executor;
        }

        public void run() {
            boolean again;

            do {
                try {
                    again = runTask();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    again = true;
                }
            } while (again);
        }

        @AtomicMethod
        private boolean runTask() {
            if (isShuttingDown() || isTooMany()) {
                executor.getWorkerThreads().remove(this);

                if (isShuttingDown() && executor.getWorkerThreads().isEmpty()) {
                    executor.setState(StmExecutorState.shutdown);
                }
                return false;
            }

            Runnable task = executor.workQueue.take();

            task.run();
            return true;
        }

        private boolean isTooMany() {
            return executor.getActualPoolsize() > executor.getPoolSize();
        }

        private boolean isShuttingDown() {
            return executor.getState() == StmExecutorState.shutdownInProgress;
        }
    }

    static class StartWorkerThreadsTask implements Runnable {

        final DoubleLinkedList<WorkerThread> workerList;

        StartWorkerThreadsTask(DoubleLinkedList<WorkerThread> workerList) {
            this.workerList = workerList;
        }

        @Override
        public void run() {
            for (WorkerThread thread : workerList) {
                thread.start();
            }
        }
    }
}

enum StmExecutorState {
    started, shutdownInProgress, shutdown
}