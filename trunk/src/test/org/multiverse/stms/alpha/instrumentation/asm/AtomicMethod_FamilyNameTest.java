package org.multiverse.stms.alpha.instrumentation.asm;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

public class AtomicMethod_FamilyNameTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void providedFamilyName() {
        ProvidedFamilyName method = new ProvidedFamilyName();
        method.execute();

        assertEquals("provided", method.found);
    }

    private class ProvidedFamilyName {
        private String found;

        @AtomicMethod(familyName = "provided")
        public void execute() {
            found = getThreadLocalTransaction().getFamilyName();
        }
    }

    @Test
    public void defaultFamilyName() {
        DefaultFamilyName method = new DefaultFamilyName();
        method.execute();

        assertEquals("org/multiverse/stms/alpha/instrumentation/asm/AtomicMethod_FamilyNameTest$DefaultFamilyName.()V", method.found);
    }

    private class DefaultFamilyName {
        private String found;

        @AtomicMethod
        public void execute() {
            found = getThreadLocalTransaction().getFamilyName();
        }
    }
}
