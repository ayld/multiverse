package org.multiverse.instrumentation;

import org.junit.Test;
import org.multiverse.SharedStmInstance;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;

public class AtomicTest {
    private Handle<Account> fromHandle;
    private Handle<Account> toHandle;

    public void setUp() {
        fromHandle = commit(SharedStmInstance.getInstance(), new Account());
        toHandle = commit(SharedStmInstance.getInstance(), new Account());
    }

    @Test
    public void testNoArgVoidMethod() {
        new NoArgVoidMethod().transfer();
    }

    public class NoArgVoidMethod {

        @Atomic
        public void transfer() {
            Account from = getTransaction().read(fromHandle);
            Account to = getTransaction().read(toHandle);
        }
    }

    @Test
    public void testObjectArgsVoidMethod() {
        new ObjectArgsVoidMethod().transfer(null, null);
    }

    public class ObjectArgsVoidMethod {

        @Atomic
        public void transfer(Object o1, Object o2) {
            Account from = getTransaction().read(fromHandle);
            Account to = getTransaction().read(toHandle);
        }
    }

    @Test
    public void testNoArgObjectReturningMethod() {
        new NoArgObjectReturningMethod().transfer();
    }


    public class NoArgObjectReturningMethod {

        @Atomic
        public String transfer() {
            Account from = getTransaction().read(fromHandle);
            Account to = getTransaction().read(toHandle);
            return null;
        }
    }


    @TmEntity
    public static class Account {
        int amount;
    }
}
