package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.DirtinessStatus;
import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.ReadonlyException;
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
                LongRefTranlocal tranlocalThis = new LongRefTranlocal(LongRef.this, value);
                t.attachNew(tranlocalThis);
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void await(long expectedValue) {
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        tranlocalThis.await(expectedValue);
    }

    @AtomicMethod
    public void set(final long value) {
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        tranlocalThis.set(value);
    }

    @AtomicMethod
    public long get() {
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        return tranlocalThis.get();
    }

    @AtomicMethod
    public void inc() {
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        tranlocalThis.inc();
    }

    @AtomicMethod
    public void dec() {
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        tranlocalThis.dec();

    }

    @AtomicMethod
    public LongRef add(LongRef ref){
        Transaction t = getThreadLocalTransaction();
        LongRefTranlocal tranlocalThis = (LongRefTranlocal) t.privatize(LongRef.this);
        return tranlocalThis.add(ref);
    }

    @Override
    public LongRefTranlocal privatize(long version) {
        LongRefTranlocal origin = (LongRefTranlocal) load(version);
        return new LongRefTranlocal(origin);
    }


}

class LongRefTranlocal extends Tranlocal {
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
    public Object getAtomicObject() {
        return atomicObject;
    }

    public void set(long newValue) {
        if (committed) {
            throw new ReadonlyException();
        } else {
            this.value = newValue;
        }
    }

    public long get() {
        return value;
    }

    public void inc() {
        if (committed) {
            throw new ReadonlyException();
        } else {
            value++;
        }
    }

    public void dec() {
        if (committed) {
            throw new ReadonlyException();
        } else {
            value--;
        }
    }

    public void await(long expectedValue) {
        if (value != expectedValue) {
            retry();
        }
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
    public TranlocalSnapshot takeSnapshot() {
        return new LongRefTranlocalSnapshot(this);
    }

    public LongRef add(LongRef ref) {
        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    public static LongRef add(LongRefTranlocal owner, LongRefTranlocal arg){
         return owner.atomicObject.add(owner.atomicObject);       
    }
}

class LongRefTranlocalSnapshot extends TranlocalSnapshot {
    final LongRefTranlocal tranlocal;
    final long value;

    LongRefTranlocalSnapshot(LongRefTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        this.value = tranlocal.value;
    }

    @Override
    public Tranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.value = value;
    }
}