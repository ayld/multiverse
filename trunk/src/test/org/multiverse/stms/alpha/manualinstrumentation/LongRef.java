package org.multiverse.stms.alpha.manualinstrumentation;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class LongRef extends FastAtomicObjectMixin {

    public LongRef(final long value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                LongRefTranlocal tranlocal = new LongRefTranlocal(LongRef.this, value);
                ((AlphaTransaction) t).attachNew(tranlocal);
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void await(long expectedValue) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        await(tranlocal, expectedValue);
    }

    @AtomicMethod
    public void set(long value) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        set(tranlocal, value);
    }

    @AtomicMethod
    public long get() {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        return get(tranlocal);
    }

    @AtomicMethod
    public void inc() {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        inc(tranlocal);
    }

    @AtomicMethod
    public void dec() {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        dec(tranlocal);
    }

    @AtomicMethod
    public LongRef add(LongRef ref) {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        LongRefTranlocal tranlocal = (LongRefTranlocal) t.load(LongRef.this);
        return tranlocal.add(ref);
    }

    @Override
    public LongRefTranlocal privatize(long version) {
        LongRefTranlocal origin = (LongRefTranlocal) load(version);
        if (origin == null) {
            throw new LoadUncommittedException();
        }
        return new LongRefTranlocal(origin);
    }

    public void set(LongRefTranlocal tranlocal, long newValue) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value = newValue;
        }
    }

    public long get(LongRefTranlocal tranlocal) {
        return tranlocal.value;
    }

    public void inc(LongRefTranlocal tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value++;
        }
    }

    public void dec(LongRefTranlocal tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        } else {
            tranlocal.value--;
        }
    }

    public void await(LongRefTranlocal tranlocal, long expectedValue) {
        if (tranlocal.value != expectedValue) {
            retry();
        }
    }
}

class LongRefTranlocal extends AlphaTranlocal {
    private final LongRef atomicObject;
    public long value;
    LongRefTranlocal origin;

    public LongRefTranlocal(LongRefTranlocal origin) {
        this.origin = origin;
        this.version = origin.version;
        this.value = origin.value;
        this.atomicObject = origin.atomicObject;
    }

    public LongRefTranlocal(LongRef atomicObject, long value) {
        this.version = Long.MIN_VALUE;
        this.value = value;
        this.atomicObject = atomicObject;
    }

    @Override
    public AlphaAtomicObject getAtomicObject() {
        return atomicObject;
    }


    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.value != this.value) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }

    @Override
    public AlphaTranlocalSnapshot takeSnapshot() {
        return new LongRefTranlocalSnapshot(this);
    }

    public LongRef add(LongRef ref) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static LongRef add(LongRefTranlocal owner, LongRefTranlocal arg) {
        return owner.atomicObject.add(owner.atomicObject);
    }
}

class LongRefTranlocalSnapshot extends AlphaTranlocalSnapshot {
    final LongRefTranlocal tranlocal;
    final long value;

    LongRefTranlocalSnapshot(LongRefTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        this.value = tranlocal.value;
    }

    @Override
    public AlphaTranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.value = value;
    }
}