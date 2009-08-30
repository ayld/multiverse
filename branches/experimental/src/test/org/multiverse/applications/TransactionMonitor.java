package org.multiverse.applications;

import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.api.annotations.Exclude;
import org.multiverse.datastructures.collections.BalancedTree;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * Problems:
 * - Needs to have its own stm, so it doesn't delude the other.
 * - How to receive events from transaction completion/failure etc.
 * - How to do a readonly transaction
 * - How to make sure that the review remains valid for some read.
 * - FIXED: How to start something after the transaction commits
 *
 * @author Peter Veentjer
 */
@AtomicObject
public class TransactionMonitor {

    @Exclude
    private final Stm stm = new AlphaStm();

    private final BalancedTree<String, TransactionInfo> infoTree = new BalancedTree<String, TransactionInfo>();

    private Thread printThread;

    public void registerSuccess(String name) {
        TransactionInfo info = getInfo(name);
        info.successCount++;
    }

    private TransactionInfo getInfo(String name) {
        TransactionInfo info = infoTree.get(name);
        if (info == null) {
            info = new TransactionInfo(name);
            infoTree.put(name, info);
        }
        return info;
    }

    public void registerFailure(String name) {
        TransactionInfo info = getInfo(name);
        info.failureCount++;
    }

    public String getStatistics() {
        return infoTree.toString();
    }

    public void startPrintingStatistics(int ms) {
        if (printThread != null) {
            return;
        }

        printThread = new PrintThread(this, ms);
        getThreadLocalTransaction().executePostCommit(new StartThreadRunnable(printThread));
    }

    @AtomicObject
    private static class TransactionInfo {
        private final String name;
        private int successCount;
        private int failureCount;

        private TransactionInfo(String name) {
            this.name = name;
        }
    }

    private static class StartThreadRunnable implements Runnable{
        private final Thread thread;

        private StartThreadRunnable(Thread thread) {
            this.thread = thread;
        }

        @Override
        public void run() {
            thread.start();
        }
    }

    private static class PrintThread extends Thread {
        private int delayMs;
        private TransactionMonitor monitor;

        public PrintThread(TransactionMonitor monitor, int delayMs) {
            super("PrintThread");
            setDaemon(true);
            this.delayMs = delayMs;
            this.monitor = monitor;
        }

        public void run() {
            while (true) {
                try {
                    sleep(delayMs);
                } catch (InterruptedException e) {
                    //ignore
                }

                String text = monitor.getStatistics();
                System.out.println(text);
            }
        }
    }
}
