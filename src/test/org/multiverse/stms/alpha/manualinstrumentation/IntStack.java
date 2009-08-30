package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

public final class IntStack extends FastAtomicObjectMixin {

    public IntStack() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                ((AlphaTransaction) t).attachNew(new IntStackTranlocal(IntStack.this));
                return null;
            }
        }.execute();
    }

    public int size() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal) ((AlphaTransaction) t).privatize(IntStack.this);
                return tranlocal.size();
            }
        }.execute();
    }

    public boolean isEmpty() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal) ((AlphaTransaction) t).privatize(IntStack.this);
                return tranlocal.isEmpty();
            }
        }.execute();
    }

    public int pop() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal) ((AlphaTransaction) t).privatize(IntStack.this);
                return tranlocal.pop();
            }
        }.execute();
    }

    public int pop(Transaction t) {
        IntStackTranlocal tranlocalThis = (IntStackTranlocal) ((AlphaTransaction) t).privatize(IntStack.this);
        return tranlocalThis.pop();
    }

    public void push(final int value) {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntStackTranlocal tranlocal = (IntStackTranlocal) ((AlphaTransaction) t).privatize(IntStack.this);
                tranlocal.push(value);
                return null;
            }
        }.execute();
    }

    public void push(Transaction t, final int value) {
        IntStackTranlocal tranlocalThis = (IntStackTranlocal) ((AlphaTransaction) t).privatize(this);
        tranlocalThis.push(value);
    }

    @Override
    public AlphaTranlocal privatize(long version) {
        IntStackTranlocal origin = (IntStackTranlocal) load(version);
        if (origin == null) {
            throw new LoadUncommittedException();
        }
        return new IntStackTranlocal(origin);
    }
}

