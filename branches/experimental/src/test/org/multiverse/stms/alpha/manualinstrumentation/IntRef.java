package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;

public final class IntRef extends FastAtomicObjectMixin {

    public static IntRef createUncommitted() {
        return new IntRef(String.class);
    }

    public IntRef() {
        this(0);
    }

    public IntRef(final int value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                IntRefTranlocal tranlocalThis = new IntRefTranlocal(IntRef.this, value);
                ((AlphaTransaction) t).attachNew(tranlocalThis);
                return null;
            }
        }.execute();
    }

    //this constructor is used for creating an uncommitted IntValue, class is used to prevent
    //overloading problems
    private IntRef(Class someClass) {
    }

    public void await(final int expectedValue) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                tranlocalThis.await(expectedValue);
                return null;
            }
        }.execute();
    }

    public void set(final int value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                tranlocalThis.set(value);
                return null;
            }
        }.execute();

    }

    public int get() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                return tranlocalThis.get();
            }
        }.execute();
    }

    public void loopInc(final int amount) {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                tranlocalThis.loopInc(amount);
                return null;
            }
        }.execute();
    }

    public void inc() {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                tranlocalThis.inc();
                return null;
            }
        }.execute();
    }

    public void dec() {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocalThis = (IntRefTranlocal) ((AlphaTransaction) t).privatize(IntRef.this);
                tranlocalThis.dec();
                return null;
            }
        }.execute();
    }

    @Override
    public IntRefTranlocal privatize(long version) {
        IntRefTranlocal origin = (IntRefTranlocal) load(version);
        if (origin == null) {
            throw new LoadUncommittedException();
        }
        return new IntRefTranlocal(origin);
    }
}

