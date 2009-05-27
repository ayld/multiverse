package org.multiverse;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.instrumentation.utils.AsmUtils;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;

public class Rubish extends TransactionTemplate {

    public Rubish(Stm stm) {
        super(stm);
    }

    @Override
    protected Object execute(Transaction t) throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static void foo(Object item) {
        System.out.println(item.getClass());
    }

    public boolean equalsObject(Object l1, Object l2) {
        if (l1 != l2)
            return true;

        return false;
    }


    public boolean equals(boolean l1, boolean l2) {
        return l1 == l2;
    }

    public boolean equals(int l1, int l2) {
        return l1 == l2;
    }

    public boolean equalsFloat(float l1, float l2) {
        return l1 != l2;
    }

    public boolean equalsDouble(double l1, double l2) {
        return l1 != l2;
    }

    public static void main(String[] args) throws IOException {
        ClassNode classNode = AsmUtils.loadAsClassNode(Rubish.class);
        AsmUtils.writeToFixedTmpFile(AsmUtils.toBytecode(classNode));
    }
}
