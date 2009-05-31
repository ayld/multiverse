package org.multiverse.instrumentation;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import static org.multiverse.api.TransactionThreadLocal.getTransaction;
import org.multiverse.api.annotations.Atomic;

public class AtomicClassTransformer_AccessModifierTest {
    private static int originalValue;
    private static Handle<IntValue> handle;

    @Before
    public void setUp() {
        originalValue = 10000;
        handle = commit(new IntValue(originalValue));
    }

    public static void assertTransactionWorking() {
        IntValue value = getTransaction().read(handle);
        assertEquals(originalValue, value.getValue());
    }

    @Test
    public void privateMethod() {
        PrivateMethod method = new PrivateMethod();
        method.doIt();
    }

    public static class PrivateMethod {

        @Atomic
        private void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void publicMethod() {
        PublicMethod method = new PublicMethod();
        method.doIt();
    }

    public static class PublicMethod {

        @Atomic
        public void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void protectedMethod() {
        ProtectedMethod method = new ProtectedMethod();
        method.doIt();
    }

    public static class ProtectedMethod {

        @Atomic
        protected void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void packageFriendlyMethod() {
        PackageFriendlyMethod method = new PackageFriendlyMethod();
        method.doIt();
    }

    public static class PackageFriendlyMethod {

        @Atomic
        void doIt() {
            assertTransactionWorking();
        }
    }

    @Test
    public void abstractMethodFails() {
        //todo
    }

    @Test
    public void synchronizedMethodGivesNoProblems() {
        SynchronizedMethod method = new SynchronizedMethod();
        method.doIt();
    }

    public class SynchronizedMethod {

        @Atomic
        public synchronized void doIt() {
            assertTransactionWorking();
        }
    }
}

