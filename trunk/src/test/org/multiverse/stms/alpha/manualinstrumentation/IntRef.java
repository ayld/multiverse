package org.multiverse.stms.alpha.manualinstrumentation;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
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
                IntRefTranlocal tranlocal = new IntRefTranlocal(IntRef.this, value);
                ((AlphaTransaction) t).attachNew(tranlocal);
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
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                await(tranlocal, expectedValue);
                return null;
            }
        }.execute();
    }

    public void set(final int value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                set(tranlocal, value);
                return null;
            }
        }.execute();

    }

    public int get() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                return get(tranlocal);
            }
        }.execute();
    }

    public void loopInc(final int amount) {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                loopInc(tranlocal, amount);
                return null;
            }
        }.execute();
    }

    public void inc() {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                inc(tranlocal);
                return null;
            }
        }.execute();
    }

    public void dec() {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                IntRefTranlocal tranlocal = (IntRefTranlocal) ((AlphaTransaction) t).load(IntRef.this);
                dec(tranlocal);
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


    public void loopInc(IntRefTranlocal tranlocal, int amount) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            for (int k = 0; k < amount; k++) {
                inc(tranlocal);
            }
        }
    }

    public void set(IntRefTranlocal tranlocal, int newValue) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value = newValue;
        }
    }

    public int get(IntRefTranlocal tranlocal) {
        return tranlocal.value;
    }

    public void inc(IntRefTranlocal tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value++;
        }
    }

    public void dec(IntRefTranlocal tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value--;
        }
    }

    public void await(IntRefTranlocal tranlocal, int expectedValue) {
        if (tranlocal.value != expectedValue) {
            retry();
        }
    }
}

