package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MaterializedObject;


public class SleepingBarberLongTest {
    private int cutsCount = 10000;

    private Handle<BarberShop> barberShopHandle;

    @Before
    public void setUp() {
        BarberShop barberShop = new BarberShop();
        barberShop.chair1 = new Chair();
        barberShop.chair2 = new Chair();
        barberShop.chair3 = new Chair();
        barberShopHandle = commit(barberShop);
    }

    @After
    public void tearDown() {
        //System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        BarberThread barberThread = new BarberThread();
        CustomerSpawnThread spawnThread = new CustomerSpawnThread();

        startAll(barberThread, spawnThread);
        joinAll(barberThread, spawnThread);
    }

    class CustomerSpawnThread extends TestThread {
        CustomerSpawnThread() {
            super("CustomerSpawnThread");
        }

        @Override
        public void run() {
            while (cutsCount > 0) {
                sleepRandomMs(5);
                new CustomerThread(cutsCount).start();
                cutsCount--;
            }
        }
    }

    class CustomerThread extends TestThread {
        private Handle<Customer> customerHandle;

        public CustomerThread(int id) {
            super("CustomerThread-" + id);
        }

        @Override
        public void run() {
            createCustomer();

            if (tryEnterBarberShop()) {
                waitForCompletion();
            }
        }

        @Atomic
        private void createCustomer() {
            customerHandle = getTransaction().attach(new Customer());
        }

        @Atomic
        public boolean tryEnterBarberShop() {
            Customer customer = getTransaction().read(customerHandle);
            BarberShop barberShop = getTransaction().read(barberShopHandle);
            return barberShop.tryPlace(customer);
        }

        @Atomic
        public void waitForCompletion() {
            Customer p = getTransaction().read(customerHandle);
            p.awaitCutCompletion();
        }
    }

    class BarberThread extends TestThread {

        public BarberThread() {
            super("BarberThread");
        }

        @Override
        public void run() {
            for (int k = 0; k < cutsCount; k++) {
                Handle<Customer> customerHandle = takeCustomer();
                cutCustomer(customerHandle);
            }
        }

        @Atomic
        public Handle<Customer> takeCustomer() {
            BarberShop barberShop = getTransaction().read(barberShopHandle);
            //ugly
            return ((MaterializedObject) barberShop.removeCustomerFromOneOfTheChairs()).getHandle();
        }

        @Atomic
        public void cutCustomer(Handle<Customer> customerHandle) {
            BarberShop barberShop = getTransaction().read(barberShopHandle);
            Customer customer = getTransaction().read(customerHandle);

            barberShop.cut(customer);
            sleepRandomMs(2);
        }
    }

    @TmEntity
    public static class BarberShop {
        private Chair chair1;
        private Chair chair2;
        private Chair chair3;

        public void cut(Customer customer) {
            customer.cut();
        }

        public Customer removeCustomerFromOneOfTheChairs() {
            if (!chair1.isFree()) {
                return chair1.tryTake();
            } else if (!chair2.isFree()) {
                return chair2.tryTake();
            } else if (!chair3.isFree()) {
                return chair3.tryTake();
            } else {
                retry();
                return null;
            }
        }

        public boolean tryPlace(Customer p) {
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

        public Customer tryTake() {
            Customer oldCustomer = customer;
            customer = null;
            return oldCustomer;
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

        public void cut() {
            if (isCut) {
                throw new IllegalStateException("Should not cut if the customer already is cut");
            }

            isCut = true;
        }

        public void awaitCutCompletion() {
            if (!isCut) {
                retry();
            }
        }
    }
}
