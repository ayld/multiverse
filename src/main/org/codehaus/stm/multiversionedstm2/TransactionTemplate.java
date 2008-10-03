package org.codehaus.stm.multiversionedstm2;

import org.codehaus.stm.transaction.AbortedException;
import org.codehaus.stm.transaction.RetryException;

public abstract class TransactionTemplate<E> {

    private final MultiversionedStm stm;

    public TransactionTemplate(MultiversionedStm stm) {
        this.stm = stm;
    }

    abstract public E execute(Transaction transaction);

    public E execute() {
        try {
            boolean success = false;
            Transaction beforeTransaction = null;
            do {
                Transaction transaction = beforeTransaction == null ? stm.startTransaction() : stm.startTransaction(beforeTransaction);
                try {
                    System.out.println(Thread.currentThread()+" start transaction");
                    beforeTransaction = null;
                    E result = execute(transaction);
                    transaction.commit();
                    System.out.println(Thread.currentThread()+" committed");
                    return result;
                } catch (RetryException ex) {
                    System.out.println(Thread.currentThread()+" retry");
                    beforeTransaction = transaction;
                    transaction.abort();
                } catch (AbortedException ex) {
                    System.out.println(Thread.currentThread()+" abort");
                    throw new RuntimeException();
                }
            } while (true);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }
}
