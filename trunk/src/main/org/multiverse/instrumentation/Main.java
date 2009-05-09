package org.multiverse.instrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        System.out.println("Main called");

        SimplePair simplePair = new SimplePair();

        Class dematerializedClass = MultiverseClassLoader.INSTANCE.loadClass("org.multiverse.instrumentation.SimplePair$DematerializedSimplePair");
        Class materializedClass = MultiverseClassLoader.INSTANCE.loadClass("org.multiverse.instrumentation.SimplePair");
        Constructor c = dematerializedClass.getConstructor(materializedClass, Transaction.class);
        DematerializedObject x = (DematerializedObject) c.newInstance(null, null);
        System.out.println("x:" + dematerializedClass);
        System.out.println("x: " + x.getHandle());
        System.out.println("x.rematerialize: " + x.rematerialize(null));

        showMemberClasses(materializedClass);
        showFields(materializedClass);

        System.out.println("calling getNextInChain");
        ((MaterializedObject) simplePair).getNextInChain();

        System.out.println("Calling setNextInChain");
        ((MaterializedObject) simplePair).setNextInChain(null);

        ((MaterializedObject) simplePair).getHandle();

        ((MaterializedObject) simplePair).walkMaterializedMembers(new MemberWalkerImpl());

        System.out.println("finished");
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
