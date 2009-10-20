package org.multiverse.stms.alpha.instrumentation;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;

/**
 * @author Peter Veentjer
 */
public class NonStaticInnerClassThatIsNotAnAtomicObjectTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }

    @Test
    public void testAnonymousInnerClass() {
        AnonymousInnerClass o = new AnonymousInnerClass();
        testIncomplete();
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
        testIncomplete();
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