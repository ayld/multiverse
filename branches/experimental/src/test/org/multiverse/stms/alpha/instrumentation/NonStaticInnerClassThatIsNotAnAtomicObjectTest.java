package org.multiverse.stms.alpha.instrumentation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class NonStaticInnerClassThatIsNotAnAtomicObjectTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void testAnonymousInnerClass() {
        AnonymousInnerClass o = new AnonymousInnerClass();
    }

    @AtomicObject
    public static class AnonymousInnerClass {

        private int i;

        public AnonymousInnerClass() {
            new Runnable() {
                @Override
                public void run() {
                    //todo
                }
            };
        }

    }

    @Test
    public void testNamedInnerClass() {
        NamedInnerClass executor = new NamedInnerClass();
    }

    @AtomicObject
    public static class NamedInnerClass {

        private int i;

        public NamedInnerClass() {
            new SomeRunnable();
        }

        class SomeRunnable implements Runnable {
            @Override
            public void run() {
                //todo
            }
        }
    }
}