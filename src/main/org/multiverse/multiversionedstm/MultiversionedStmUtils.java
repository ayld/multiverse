package org.multiverse.multiversionedstm;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.util.Bag;

import java.util.Iterator;

public final class MultiversionedStmUtils {

    public static MaterializedObject initializeNextChain(Iterator<? extends LazyReference> it) {
        final Bag<MaterializedObject> traverseBag = new Bag<MaterializedObject>();
        final Ref<MaterializedObject> lastInChain = new Ref<MaterializedObject>();
        final Ref<MaterializedObject> first = new Ref<MaterializedObject>();

        for (; it.hasNext();) {
            LazyReference lazyReference = it.next();
            if (lazyReference.isLoaded()) {
                MaterializedObject obj = (MaterializedObject) lazyReference.get();

                if (first.value == null) {
                    first.value = obj;
                }

                traverseBag.add(obj);
                obj.setNextInChain(lastInChain.value);
                lastInChain.value = obj;
            }
        }

        MemberWalker walker = new MemberWalker() {
            @Override
            public void onMember(MaterializedObject member) {
                if (member.getNextInChain() == null && member != first.value) {
                    member.setNextInChain(lastInChain.value);
                    lastInChain.value = member;
                    traverseBag.add(member);
                }
            }
        };

        while (!traverseBag.isEmpty()) {
            MaterializedObject materializedObject = traverseBag.takeAny();
            materializedObject.walkMaterializedMembers(walker);
        }

        return lastInChain.value;
    }

    static class Ref<T> {
        T value;
    }

    public static void retry() {
        throw new RetryError();
    }

    public static <T> Handle<T> getHandle(T value) {
        return value == null ? null : ((MaterializedObject) value).getHandle();
    }

    public static <T> Object getValueOrHandle(LazyReference<T> ref, T value) {
        if (value == null) {
            return ref == null ? null : ref.getHandle();
        } else {
            if (value instanceof MaterializedObject) {
                return ((MaterializedObject) value).getHandle();
            } else {
                return value;
            }
        }
    }


    public static <T> Handle<T> getHandle(LazyReference<T> ref, T value) {
        if (ref != null)
            return ref.getHandle();

        return value != null ? ((MaterializedObject) value).getHandle() : null;
    }

    private MultiversionedStmUtils() {
    }
}
