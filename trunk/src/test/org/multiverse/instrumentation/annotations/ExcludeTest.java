package org.multiverse.instrumentation.annotations;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commitAndRead;
import org.multiverse.api.annotations.Exclude;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * Test to see if the @Exclude annotation is working.
 *
 * @author Peter Veentjer
 */
public class ExcludeTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        SomeEntity original = new SomeEntity();
        original.excluded = 10;
        original.included = 25;

        SomeEntity found = commitAndRead(stm, original);
        assertEquals(0, found.excluded);
        assertEquals(25, found.included);

        found.excluded = 20;
        //todo: as soon as the isDirty is implemented
        //assertFalse(((MaterializedObject) found).isDirty());

        found.included = 100;
        //todo: as soon as the isDirty is implemented        
        //assertTrue(((MaterializedObject) found).isDirty());
    }

    @TmEntity
    public static class SomeEntity {

        @Exclude
        private int excluded;

        private int included;
    }
}