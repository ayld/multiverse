package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

public class RefTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void refIsTransformed() {
        Ref<Integer> ref = new Ref<Integer>();
        assertTrue(((Object) ref) instanceof AlphaAtomicObject);
    }

    @Test
    public void refWithTypeParametersIsTransformed() {
        Ref<Integer> ref = new Ref<Integer>();
        assertTrue(((Object) ref) instanceof AlphaAtomicObject);
    }
}
