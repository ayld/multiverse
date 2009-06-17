package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.executor.AbstractDriver;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * A {@link org.benchy.executor.Driver} for benchmarking a
 * {@link LinkedBlockingDeque}.
 *
 * @author Peter Veentjer
 */
public class ContendedLinkedBLockingDequeDriver extends AbstractDriver {

    private LinkedBlockingDeque deque;

    @Override
    public void run() {
        //todo
    }
}
