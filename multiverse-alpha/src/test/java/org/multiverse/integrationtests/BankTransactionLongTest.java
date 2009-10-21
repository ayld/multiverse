package org.multiverse.integrationtests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;

/**
 * The BankTransactionTest is a test where money is transferred from one account to another. In the beginning there is
 * one account with money. Money is transferred from account to account in a random fashion.
 * <p/>
 * When the test has completed, the total amount of money should be the same as the original
 *
 * @author Peter Veentjer.
 */
public class BankTransactionLongTest {

    private long initialAmount;
    private final int threadCount = 20;
    private final int accountCount = 10;
    private BankAccount[] bankAccounts;
    private final int transferCount = 1000;

    private Stm stm;
    private TransferThread[] threads;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();

        bankAccounts = new BankAccount[accountCount];

        for (int k = 0; k < accountCount; k++) {
            long amount = randomInt(1000);
            initialAmount+=amount;
            bankAccounts[k] = new BankAccount(amount);
        }

        threads = createThreads();
    }


    @Test
    public void test() {
        startAll(threads);
        joinAll(threads);

        assertEquals(initialAmount, getTotal());
    }

    private long getTotal() {
        long sum = 0;
        for (BankAccount account : bankAccounts) {
            sum += account.getBalance();
        }
        return sum;
    }

    private TransferThread[] createThreads() {
        TransferThread[] threads = new TransferThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new TransferThread(k);
        }
        return threads;
    }

    private class TransferThread extends TestThread {

        public TransferThread(int id) {
            super("TransferThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k=0;
            do{
                try{
                    transferBetweenRandomAccounts();
                    if((k % 100)==0){
                        System.out.printf("Thread %s is at iteration %s\n",getName(),k);
                    }
                    k++;
                }catch(NotEnoughMoneyException exception){
                }
            }while(k<transferCount);
        }

        @AtomicMethod
        private void transferBetweenRandomAccounts() {
            BankAccount from = bankAccounts[randomInt(bankAccounts.length-1)];
            BankAccount to = bankAccounts[randomInt(bankAccounts.length-1)];
            int amount = randomInt(1000);
            to.setBalance(to.getBalance()+amount);
            //place some delay so that the transaction is very likely to conflict 
            sleepRandomMs(20);
            from.setBalance(from.getBalance()-amount);
        }
    }

    @AtomicObject
    private static class BankAccount {

        private long balance;

        private BankAccount(final long balance) {
            this.balance = balance;
        }

        @AtomicMethod(readonly = true)
        public long getBalance() {
            return balance;
        }

        public void setBalance(long balance) {
            if (balance < 0) {
                throw new NotEnoughMoneyException();
            }

            this.balance = balance;
        }
    }

    private static class NotEnoughMoneyException extends RuntimeException {

    }
}
