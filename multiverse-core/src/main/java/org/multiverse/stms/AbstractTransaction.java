package org.multiverse.stms;

import org.multiverse.api.ScheduleType;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.latches.Latch;
import org.multiverse.utils.restartbackoff.ExponentialRestartBackoffPolicy;
import org.multiverse.utils.restartbackoff.RestartBackoffPolicy;

import static java.text.MessageFormat.format;

/**
 * An abstract {@link Transaction} implementation that contains most of the plumbing logic. Extend this and prevent
 * duplicate logic.
 * <p/>
 * The on-methods can be overridden.
 * <p/>
 * The subclass needs to call the reset when it has completed its constructor. Can't be done inside the constructor of
 * the AbstractTransaction because properties in the subclass perhaps are not set.
 *
 * @author Peter Veentjer.
 */
public abstract class AbstractTransaction<D extends AbstractTransactionDependencies> implements Transaction {

    protected final D dependencies;
    protected final String familyName;

    protected TaskListNode scheduledTasks;
    protected long readVersion;
    //needs to be removed, readversion could be used for that.n
    private long commitVersion;
    protected TransactionStatus status;

    /**
     * Creates a new AbstractTransaction.
     *
     * @param familyName the familyName this transaction has.
     * @param clock      the Clock this transaction uses
     * @throws NullPointerException if clock or commitLockPolicy is null.
     */
    public AbstractTransaction(String familyName, Clock clock) {
        this((D) new AbstractTransactionDependencies(clock, ExponentialRestartBackoffPolicy.INSTANCE_10_MS_MAX),
             familyName);
    }

    public AbstractTransaction(D dependencies, String familyName) {
        assert dependencies != null;
        this.dependencies = dependencies;
        this.familyName = familyName;
    }

    @Override
    public RestartBackoffPolicy getRestartBackoffPolicy() {
        return dependencies.restartBackoffPolicy;
    }

    public String getFamilyName() {
        return familyName;
    }

    @Override
    public long getReadVersion() {
        return readVersion;
    }

    protected final void init() {
        this.scheduledTasks = null;
        this.readVersion = dependencies.clock.getTime();
        this.status = TransactionStatus.active;
        doInit();
    }

    protected void doInit() {
    }

    @Override
    public void schedule(Runnable task, ScheduleType scheduleType) {
        switch (status) {
            case active:
                if (task == null) {
                    throw new NullPointerException();
                }

                if(scheduleType == null){
                    throw new NullPointerException();
                }

                scheduledTasks = new TaskListNode(task, scheduleType, scheduledTasks);
                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't execute compensating task on already committed transaction '%s'", familyName));
            case aborted:
                throw new DeadTransactionException(
                        format("Can't execute compensating task on already aborted transaction '%s'", familyName));
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public Transaction abortAndReturnRestarted() {
        switch (status) {
            case active:
                //fall through
            case committed:
                //fall through
            case aborted:
                init();
                return this;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public TransactionStatus getStatus() {
        return status;
    }

    protected void excuteScheduledTasks(ScheduleType scheduleType) {
        if (scheduledTasks != null) {
            scheduledTasks.executeAll(scheduleType);
        }
    }

    @Override
    public void abort() {
        switch (status) {
            case active:
                try {
                    excuteScheduledTasks(ScheduleType.preAbort);
                    status = TransactionStatus.aborted;
                    doAbort();
                    excuteScheduledTasks(ScheduleType.postAbort);
                } finally {
                    scheduledTasks = null;
                }
                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't abort already committed transaction '%s'", familyName));
            case aborted:
                //ignore
                break;
            default:
                throw new RuntimeException();
        }
    }

    protected void doAbort() {
    }

    @Override
    public long commit() {
        switch (status) {
            case active:
                try {
                    excuteScheduledTasks(ScheduleType.preCommit);
                    boolean abort = true;
                    try {
                        commitVersion = onCommit();
                        status = TransactionStatus.committed;
                        abort = false;
                        excuteScheduledTasks(ScheduleType.postCommit);
                        return commitVersion;
                    } finally {
                        if (abort) {
                            doAbort();
                        }
                    }
                } finally {
                    scheduledTasks = null;
                }
            case committed:
                return commitVersion;
            case aborted:
                throw new DeadTransactionException(
                        format("Can't commit already aborted transaction '%s'", familyName));
            default:
                throw new IllegalStateException();
        }
    }

    protected long onCommit() {
        return readVersion;
    }

    @Override
    public void abortAndRegisterRetryLatch(Latch latch) {
        switch (status) {
            case active:

                if (latch == null) {
                    throw new NullPointerException();
                }

                doAbortAndRegisterRetryLatch(latch);

                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't call abortAndRegisterRetryLatch on already committed transaction '%s'",
                               familyName));
            case aborted: {
                throw new DeadTransactionException(
                        format("Can't call abortAndRegisterRetryLatch on already aborted transaction '%s'",
                               familyName));
            }
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Default implementation does the actual abort and then throws an {@link UnsupportedOperationException}.
     *
     * @param latch the Latch to register.
     * @throws UnsupportedOperationException depends on the implementation. But the default implementation will always
     *                                       throw this exception.
     */
    protected void doAbortAndRegisterRetryLatch(Latch latch) {
        doAbort();
        throw new UnsupportedOperationException();
    }

    @Override
    public void startOr() {
        switch (status) {
            case active:
                doStartOr();
                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't call startOr on already committed transaction '%s'", familyName));
            case aborted:
                throw new DeadTransactionException(
                        format("Can't call startOr on already aborted transaction '%s'", familyName));
            default:
                throw new RuntimeException();
        }
    }

    protected void doStartOr() {
    }

    @Override
    public void endOr() {
        switch (status) {
            case active:
                doEndOr();
                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't call endOr on already committed transaction '%s'", familyName));
            case aborted:
                throw new DeadTransactionException(
                        format("Can't call endOr on already aborted transaction '%s'", familyName));
            default:
                throw new RuntimeException();
        }
    }

    protected void doEndOr() {
    }

    @Override
    public void endOrAndStartElse() {
        switch (status) {
            case active:
                doEndOrAndStartElse();
                break;
            case committed:
                throw new DeadTransactionException(
                        format("Can't call endOrAndStartElse on already committed transaction '%s'", familyName));
            case aborted:
                throw new DeadTransactionException(
                        format("Can't call endOrAndStartElse on already aborted transaction '%s'", familyName));
            default:
                throw new RuntimeException();
        }
    }

    protected void doEndOrAndStartElse() {
    }

    private static class TaskListNode {

        private final Runnable task;
        private final ScheduleType scheduleType;
        private final TaskListNode next;

        private TaskListNode(Runnable task, ScheduleType scheduleType, TaskListNode next) {
            assert task != null;
            assert scheduleType != null;

            this.task = task;
            this.scheduleType = scheduleType;
            this.next = next;
        }

        public void executeAll(ScheduleType requiredScheduleType) {
            assert requiredScheduleType != null;

            TaskListNode node = this;
            do {
                if (scheduleType == requiredScheduleType) {
                    node.task.run();
                }
                node = node.next;
            } while (node != null);
        }
    }

}
