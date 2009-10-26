package org.multiverse.stms.alpha.instrumentation.asm;

import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.api.exceptions.RecoverableThrowable;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.templates.AbortedException;
import org.multiverse.utils.ThreadLocalTransaction;

/**
 * The donor class that can be used while instrumenting atomic methods and adding the transaction management
 * logic.
 *
 * @author Peter Veentjer.
 */
public class AtomicLogicDonor {

    //all values of these constants can be derived from the atomicmethod annotation.
    //in the bytecode a reference to one of these constants can be replaced by loading
    //some constant from the constant pool.
    private final int retryCount = -1;
    private final String transactionFamilyName = "";
    private final boolean readonly = false;

    public void executeAtomic() {
    }

    public void start() throws Exception {
        //all local variables here need to be added to the method where this code is injected.

        Transaction t = ThreadLocalTransaction.getThreadLocalTransaction();

        if (t != null && t.getStatus() == TransactionStatus.active) {
            executeAtomic();
        } else {
            try {
                t = null;
                Stm stm = GlobalStmInstance.getGlobalStmInstance();
                int tryCount = 1;
                while (tryCount <= retryCount + 1) {
                    t = t == null ? stm.startUpdateTransaction(transactionFamilyName) : t.restart();
                    try {
                        executeAtomic();

                        if (t.getStatus() == TransactionStatus.aborted) {
                            throw new AbortedException();
                        }

                        t.commit();
                    } catch (RetryError er) {
                        t.abortAndWaitForRetry();
                    } catch (Throwable throwable) {
                        if (throwable instanceof RecoverableThrowable) {
                            tryCount++;
                        } else if (throwable instanceof Exception) {
                            throw (Exception) throwable;
                        } else if (throwable instanceof Error) {
                            throw (Error) throwable;
                        } else {
                            throw new PanicError("Unthrowable ", throwable);
                        }
                    } finally {
                        if (t.getStatus() == TransactionStatus.active) {
                            t.abort();
                        }
                    }
                }
            } finally {
                ThreadLocalTransaction.clearThreadLocalTransaction();
            }
        }
    }

    //when a method is called on an atomic object, by an atomic method, it could call a clone ofo this
    //method that passes the transaction. This reduces the need for logic in the method and in turn
    //make execution speed better (less code) but also memory usage better (stack frames get smaller since
    //there are less variables needed.
}
