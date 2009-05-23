package org.multiverse.api;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class TransactionTemplateTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        TransactionTemplate t = new TransactionTemplate(stm) {
            @Override
            protected Object execute(Transaction t) throws Exception {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        return null;
                    }
                }.execute();

                return null;
            }
        };

        try {
            t.execute();
            fail();
        } catch (RuntimeException ex) {
        }
    }
}
