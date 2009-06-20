package org.multiverse.benchmarks.drivers.deucetests;

import org.benchy.executor.AbstractDriver;
import org.benchy.executor.TestCase;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;

import java.util.Random;

public class BankDriver extends AbstractDriver {

    static volatile boolean s_disjoint;
    static volatile boolean s_yield;

    private int m_max;
    private int readFrequency;
    private int writeFrequency;
    private Handle<Account>[] accounts;
    private int accountCount;
    private float initialBalance;
    private int threadCount;
    private BenchmarkThread[] threads;

    @Override
    public void preRun(TestCase testCase) {
        threadCount = testCase.getIntProperty("threadCount");
        accountCount = testCase.getIntProperty("accountCount", 8);
        initialBalance = testCase.getIntProperty("balance", 10000);
        readFrequency = testCase.getIntProperty("readFrequency", 0);
        writeFrequency = testCase.getIntProperty("writeFrequency", 0);
        m_max = testCase.getIntProperty("m_max", 10);
        s_disjoint = testCase.getBooleanProperty("yield", false);
        s_yield = testCase.getBooleanProperty("disjoint", false);

        accounts = new Handle[accountCount];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = commit(new Account("Account-" + i, initialBalance));
        }

        threads = new BenchmarkThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            //threads[k]=new BenchmarkThread();
        }
    }

    @Override
    public void run() {
        startAll(threads);
        joinAll(threads);
    }

    @Atomic
    public void addInterest(float rate) {
        Transaction t = getTransaction();

        for (Handle<Account> h : accounts) {
            Account a = t.read(h);

            a.deposit(a.getBalance() * rate);
            if (s_yield)
                Thread.yield();
        }
    }

    @Atomic
    public double computeTotal() {
        Transaction t = getTransaction();

        double total = 0.0;
        for (Handle<Account> h : accounts) {
            Account a = t.read(h);
            total += a.getBalance();
            if (s_yield)
                Thread.yield();
        }
        return total;
    }

    @Atomic
    public void transfer(Handle<Account> src, Handle<Account> dst, float amount) throws OverdraftException {
        Transaction t = getTransaction();

        t.read(src).deposit(amount);
        if (s_yield)
            Thread.yield();
        t.read(dst).withdraw(amount);
    }

    public class BenchmarkThread extends TestThread {

        private Random random = new Random();

        BenchmarkThread(int id) {
            super("BenchmarkThread-" + id);
        }

        @Override
        public void run() {
            int i = random.nextInt(100);

            if (i < readFrequency) {
                // Compute total of all accounts (read-all transaction)
                computeTotal();
            } else if (i < readFrequency + writeFrequency) {
                // Add 0% interest (write-all transaction)
                addInterest(0);
            } else {
                int amount = random.nextInt(m_max) + 1;
                Handle<Account> src;
                Handle<Account> dst;
                //if (s_disjoint && m_nb <= accounts.length) {
                //    src = accounts[random.nextInt(accounts.length / m_nb) * m_nb + m_id];
                //    dst = accounts[random.nextInt(accounts.length / m_nb) * m_nb + m_id];
                // } else {
                //     src = accounts[random.nextInt(accounts.length)];
                //     dst = accounts[random.nextInt(accounts.length)];
                // }

                // try {
                //    transfer(src, dst, amount);
                // } catch (OverdraftException e) {
                //     System.err.println("Overdraft: " + e.getMessage());
                // }
            }
        }

        //public String getStats() {
        //    return "T=" + transferCount + ", R=" + readCount + ", W=" + writeCount;
        //}
    }

    public static class Account {

        private String name;
        private float balance;

        public Account(String name, float balance) {
            this.name = name;
            this.balance = balance;
        }

        public String getName() {
            return name;
        }

        public float getBalance() {
            return balance;
        }

        public void deposit(float amount) {
            balance += amount;
        }

        public void withdraw(float amount) throws OverdraftException {
            if (balance < amount)
                throw new OverdraftException("Cannot withdraw $" + amount + " from $" + balance);

            balance -= amount;
        }
    }
}
