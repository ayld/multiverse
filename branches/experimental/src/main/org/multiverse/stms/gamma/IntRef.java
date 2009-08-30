package org.multiverse.stms.gamma;

import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import org.multiverse.utils.spinning.SpinPolicy;

public class IntRef extends AbstractGammaAtomicObject {

    public IntRef(int value) {
        this(((GammaTransaction) getThreadLocalTransaction()), value);
    }

    public IntRef(GammaTransaction t, int value) {
        IntRefTranlocal tranlocal = new IntRefTranlocal(this);
        t.attachAsNew(tranlocal);

        tranlocal.value = value;
    }

    public int get() {
        GammaTransaction t = ((GammaTransaction) getThreadLocalTransaction());
        return get(t);
    }

    public int get(GammaTransaction transaction) {
        IntRefTranlocal tranlocal = (IntRefTranlocal) transaction.privatize(this);
        return get(tranlocal);
    }

    public int get(IntRefTranlocal tranlocal) {
        return tranlocal.value;
    }

    public void set(int newValue) {
        GammaTransaction t = ((GammaTransaction) getThreadLocalTransaction());
        set(t, newValue);
    }

    public void set(GammaTransaction t, int newValue) {
        set((IntRefTranlocal) t.privatize(this), newValue);
    }

    public void set(IntRefTranlocal tranlocal, int newValue) {
        tranlocal.value = newValue;
    }

    public void inc() {
        GammaTransaction t = (GammaTransaction) getThreadLocalTransaction();
        inc(t);
    }

    public void inc(GammaTransaction t) {
        IntRefTranlocal tranlocal = (IntRefTranlocal) t.privatize(this);
        inc(tranlocal);
    }

    public void inc(IntRefTranlocal tranlocal) {
        tranlocal.value++;
    }


    public void loopInc(int amount) {
        GammaTransaction t = ((GammaTransaction) getThreadLocalTransaction());
        loopInc(amount);
    }

    public void loopInc(GammaTransaction t, int amount) {
        loopInc((IntRefTranlocal) t.privatize(this), amount);
    }

    public void loopInc(IntRefTranlocal tranlocal, int amount) {
        for (int k = 0; k < amount; k++) {
            tranlocal.value++;
        }
    }

    public void transfer(IntRef to, int amount) {
        transfer(((GammaTransaction) getThreadLocalTransaction()), to, amount);
    }

    public void transfer(GammaTransaction t, IntRef to, int amount) {
        transfer(t, (IntRefTranlocal) t.privatize(this), to, amount);
    }

    public void transfer(GammaTransaction t, IntRefTranlocal tranlocal, IntRef to, int amount) {
        tranlocal.value -= amount;
        ((IntRefTranlocal) t.privatize(to)).value += amount;
    }

    public void selfCall(int depth) {
        selfCall(((GammaTransaction) getThreadLocalTransaction()), depth);
    }

    public void selfCall(GammaTransaction t, int depth) {
        selfCall((IntRefTranlocal) t.privatize(this), depth);
    }

    public void selfCall(IntRefTranlocal tranlocal, int depth) {
        if (depth > 0) {
            selfCall(tranlocal, --depth);
        }
    }

    @Override
    public GammaTranlocal privatize(long version, SpinPolicy spinPolicy) {
        IntRefTranlocal origin = (IntRefTranlocal) load(version, spinPolicy);
        return new IntRefTranlocal(origin);
    }
}

class IntRefTranlocal extends AbstractGammaTranlocal<IntRef> {
    int value;

    public IntRefTranlocal(IntRef atomicObject) {
        super(atomicObject);
    }

    public IntRefTranlocal(IntRefTranlocal origin) {
        super(origin);
        this.value = origin.value;
    }

    @Override
    protected boolean isDirty(GammaTranlocal o) {
        IntRefTranlocal origin = (IntRefTranlocal) o;
        if (origin.value != value) {
            return true;
        }

        return false;
    }
}
