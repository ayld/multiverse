package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedstm.examples.IntegerConstant;

public class ImmutabilityTest extends AbstractMultiversionedStmTest {

    public void test() {
        IntegerConstant constant = new IntegerConstant(20);
        long handle = atomicInsert(constant);

        createActiveTransaction();
        IntegerConstant foundConstant = (IntegerConstant) transaction.read(handle);
        assertSame(constant, foundConstant);
        assertTransactionHasNoHydratedObjects();
    }

    public void testStack() {

    }
}
