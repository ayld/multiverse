package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

public final class IntStack extends FastAtomicObjectMixin {

    public IntStack() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                t.attachNew(new IntStackTranlocal(IntStack.this));
                return null;
            }
        }.execute();
    }

    public int size() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal)t.privatize(IntStack.this);
                return tranlocal.size();
            }
        }.execute();
    }

    public boolean isEmpty() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal)t.privatize(IntStack.this);
                return tranlocal.isEmpty();
            }
        }.execute();
    }

    public int pop() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal)t.privatize(IntStack.this);
                return tranlocal.pop();
            }
        }.execute();
    }

    public int pop(Transaction t) {
        IntStackTranlocal tranlocalThis = (IntStackTranlocal) t.privatize(IntStack.this);
        return tranlocalThis.pop();
    }

    public void push(final int value) {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal)t.privatize(IntStack.this);
                tranlocal.push(value);
                return null;
            }
        }.execute();
    }

    public void push(Transaction t, final int value) {
        IntStackTranlocal tranlocalThis = (IntStackTranlocal) t.privatize(this);
        tranlocalThis.push(value);
    }

    @Override
    public Tranlocal privatize(long version) {
        IntStackTranlocal origin = (IntStackTranlocal) load(version);
        return new IntStackTranlocal(origin);
    }
}

