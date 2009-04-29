package org.multiverse.multiversionedstm;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Originator;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.util.Bag;

import java.util.Iterator;

public final class MultiversionedStmUtils {

    public static MaterializedObject initializeNextChain(Iterator<? extends LazyReference> it) {
        final Bag<MaterializedObject> traverseBag = new Bag<MaterializedObject>();
        final Ref<MaterializedObject> lastInChain = new Ref<MaterializedObject>();
        final Ref<MaterializedObject> first = new Ref<MaterializedObject>();

        //System.out.println("starting initializeNextChain");

        int count = 0;
        for (; it.hasNext();) {
            LazyReference lazyReference = it.next();
            count++;
        //    System.out.println("first loop: " + count);
            if (lazyReference.isLoaded()) {
                MaterializedObject obj = (MaterializedObject) lazyReference.get();
        //        System.out.println("obj: " + obj);

                if (first.value == null) {
                    first.value = obj;
                }

                traverseBag.add(obj);
                obj.setNextInChain(lastInChain.value);
                lastInChain.value = obj;
            }
        }

        //System.out.println("traversing todo bag");
        count = 0;
        while (!traverseBag.isEmpty()) {
            MaterializedObject materializedObject = traverseBag.takeAny();
       //     System.out.println("second loop: " + count);
            count++;
            MemberTracer tracer = new MemberTracer() {
                @Override
                public void onMember(MaterializedObject member) {
         //           System.out.println("on member: " + member);
                    if (member.getNextInChain() == null && member != first.value) {
           //             System.out.println("member: " + member);
                        member.setNextInChain(lastInChain.value);
                        lastInChain.value = member;
                        traverseBag.add(member);
                    }
                }
            };

            materializedObject.memberTrace(tracer);
        }
        //System.out.println("finishes traversing todo bag");
        //System.out.println("length: " + length(lastInChain.value));


        //int length = length(lastInChain.value);
        //if (length > 12)
        //    throw new RuntimeException("length =" + length);

        // printChain(lastInChain.value);
        //try {
        //    Thread.sleep(200);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        //}

        return lastInChain.value;
    }

    static int length(MaterializedObject o) {
        int result = 0;
        while (o != null) {
            result++;
            o = o.getNextInChain();
        }
        return result;
    }

    static void printChain(MaterializedObject o) {
        while (o != null) {
            System.out.print(o + ".");
            o = o.getNextInChain();
        }
        System.out.println();
    }

    static class Ref<T> {
        T value;
    }

    public static void retry() {
        throw new RetryError();
    }

    public static <T> Originator<T> getOriginator(T value) {
        return value == null ? null : ((MaterializedObject) value).getOriginator();
    }

    public static <T> Object getValueOrOriginator(LazyReference<T> ref, T value) {
        if (value == null) {
            return ref == null ? null : ref.getOriginator();
        } else {
            if (value instanceof MaterializedObject) {
                return ((MaterializedObject) value).getOriginator();
            } else {
                return value;
            }
        }
    }


    public static <T> Originator<T> getOriginator(LazyReference<T> ref, T value) {
        if (ref != null)
            return ref.getOriginator();

        return value != null ? ((MaterializedObject) value).getOriginator() : null;
    }

    private MultiversionedStmUtils() {
    }
}
