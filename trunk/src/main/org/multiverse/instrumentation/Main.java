package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.collections.Latch;
import org.multiverse.collections.Queue;
import org.multiverse.collections.Stack;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedStm;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        System.out.println("Main called");

        testLatch();
        testStack();
    }

    private static void testLatch() {
        Latch latch = new Latch();
        latch.open();
        System.out.println("original " + latch);

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Latch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Latch found = t2.read(handle);
        System.out.println("found: " + found);
        t2.commit();
    }

    private static void testQueue() {
        Queue queue = new Queue();

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Queue> handle = t.attach(queue);
        t.commit();
    }

    private static void testStack() {
        Stack stack = new Stack();

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Stack> handle = t.attach(stack);
        stack.push("hallo");
        t.commit();

        Transaction t2 = stm.startTransaction();
        Stack found = t2.read(handle);
        System.out.println("stack.handle: " + ((MaterializedObject) (Object) found).getHandle());

        System.out.println("found.size: " + found.size());
        t2.commit();

    }

    private static void showMemberClasses(Class simplePairClass) {
        for (Class memberClass : simplePairClass.getClasses()) {
            System.out.println("member: " + memberClass.getName());
        }
    }

    private static void showFields(Class simplePairClass) {
        for (Field field : simplePairClass.getFields()) {
            System.out.println("fields: " + field.getName());
        }
    }

    static class MemberWalkerImpl implements MemberWalker {
        @Override
        public void onMember(MaterializedObject member) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
