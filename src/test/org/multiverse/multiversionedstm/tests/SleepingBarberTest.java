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
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class SleepingBarberTest {
    private MultiversionedStm stm;

    private int cutsCount = 100000000;
    private int customerChairCount = 10;

    private Handle<IntegerValue> barberChairHandle;
    private Handle<IntegerValue> chair1;
    private Handle<IntegerValue> chair2;
    private Handle<IntegerValue> chair3;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();

        chair1 = commit(stm, new IntegerValue(0));
        chair2 = commit(stm, new IntegerValue(0));
        chair3 = commit(stm, new IntegerValue(0));
        barberChairHandle = commit(stm, new IntegerValue(0));
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
        public CustomerThread(int id) {
            super("CustomerThread-" + id);
        }

        @Override
        public void run() {
            if (enterBarber()) {
                waitForCompletion();
            }
        }

        private boolean enterBarber() {
            return new TransactionTemplate<Boolean>(stm) {
                @Override
                protected Boolean execute(Transaction t) throws Exception {
                    return enterBarberLogic();
                }
            }.execute();
        }

        private boolean enterBarberLogic() {
            //first look in the barber chair if there is place

            //then look in the customer chair if there is place

            //if there is no place, walk out of the store.

            return false;
        }

        private void waitForCompletion() {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) throws Exception {

                    return null;
                }
            }.execute();
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
            IntegerValue barberChair = t.read(barberChairHandle);

            if (barberChair.get() == 1) {
                //now cut the customer
                barberChair.dec();
            } //else
            //        for ()

            //if no customer is found, do retry.
            retry();
        }
    }
}
