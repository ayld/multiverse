package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class SleepingBarberTest {
    private MultiversionedStm stm;

    private int cutsCount = 100000000;

    private Handle<BarberShop> barberShopHandle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();

        BarberShop barberShop = new BarberShop();
        barberShop.chair1 = new Chair();
        barberShop.chair2 = new Chair();
        barberShop.chair3 = new Chair();
        barberShopHandle = commit(stm, barberShop);
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        BarberThread barberThread = new BarberThread();

        //startAll(barberThread);
        //joinAll(barberThread);
    }

    class CustomerSpawnThread extends TestThread {

    }

    class CustomerThread extends TestThread {
        private Handle<Customer> customerHandle;

        public CustomerThread(int id) {
            super("CustomerThread-" + id);
        }

        @Override
        public void run() {
            customerHandle = commit(stm, new Customer());

            if (atomicEnterBarber()) {
                atomicWaitForCompletion();
            }
        }

        //todo: when the atomic annotation is added,this method can be dropped.
        private boolean atomicEnterBarber() {
            return new TransactionTemplate<Boolean>(stm) {
                @Override
                protected Boolean execute(Transaction t) {
                    return enterBarber();
                }
            }.execute();
        }

        private boolean enterBarber() {
            Customer customer = getTransaction().read(customerHandle);
            BarberShop barberShop = getTransaction().read(barberShopHandle);
            return barberShop.placeIfPossible(customer);
        }

        //todo: when the atomic annotation is added, this method can be dropped.
        private void atomicWaitForCompletion() {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) {
                    waitForCompletion();
                    return null;
                }

            }.execute();
        }


        private void waitForCompletion() {
            Customer p = getTransaction().read(customerHandle);
            p.awaitCut();
        }
    }

    class BarberThread extends TestThread {

        public BarberThread() {
            super("BarberThread");
        }

        @Override
        public void run() {
            for (int k = 0; k < cutsCount; k++) {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        barberLogic(t);
                        return null;
                    }
                }.execute();
            }
        }

        private void barberLogic(Transaction t) {
            BarberShop barberShop = t.read(barberShopHandle);
            barberShop.doCut();
        }
    }

    @TmEntity
    static class BarberShop {
        private Chair chair1;
        private Chair chair2;
        private Chair chair3;

        public void doCut() {
            Customer customer = removeCustomerFromOneOfTheChairs();
            customer.cut();
        }

        private Customer removeCustomerFromOneOfTheChairs() {
            if (!chair1.isFree()) {
                return chair1.remove();
            } else if (!chair2.isFree()) {
                return chair2.remove();
            } else if (!chair3.isFree()) {
                return chair3.remove();
            } else {
                retry();
                return null;
            }
        }

        public boolean placeIfPossible(Customer p) {
            if (chair1.placeIfFree(p)) {
                return true;
            }

            if (chair2.placeIfFree(p)) {
                return true;
            }

            if (chair3.placeIfFree(p)) {
                return true;
            }

            return false;
        }
    }

    @TmEntity
    public static class Chair {
        private Customer customer;

        public Customer getCustomer() {
            return customer;
        }

        public Customer remove() {
            Customer oldCustomer = customer;
            customer = null;
            return oldCustomer;
        }

        public void setCustomer(Customer customer) {
            this.customer = customer;
        }

        public boolean isFree() {
            return customer == null;
        }

        public boolean placeIfFree(Customer p) {
            if (customer != null) {
                return false;
            }

            customer = p;
            return true;
        }
    }

    @TmEntity
    public static class Customer {

        private boolean isCut = false;

        public boolean isCut() {
            return isCut;
        }

        public void cut() {
            isCut = true;
        }

        public void awaitCut() {
            if (!isCut)
                retry();
        }
    }
}
