package org.multiverse.applications;

import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.multiverse.TestUtils;
import org.multiverse.api.Transaction;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class StmExecutorTest {

    //@Test
    public void test() {
        StmExecutor executor = new StmExecutor(1, 100);
        assertFalse(executor.isShutdown());
    }

    @Test
    public void testFoo() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                TestUtils.sleepMs(1000);
                System.out.println("hello");
            }
        };

        Transaction t = GlobalStmInstance.get().startUpdateTransaction();
        setThreadLocalTransaction(t);
        StmExecutor executor = new StmExecutor(1,10);

        executor.execute(task);
        executor.execute(task);
        executor.execute(task);
        executor.execute(task);
    }

    @Test
    public void testExecute() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                TestUtils.sleepMs(1000);
                System.out.println("hello");
            }
        };

        StmExecutor executor = new StmExecutor(1,10);
        executor.execute(task);
        executor.execute(task);
        executor.execute(task);
        executor.execute(task);
        executor.execute(task);
        executor.shutdown();
        executor.awaitShutdown();
    }


    @Test
    public void testIgnore(){}
}
