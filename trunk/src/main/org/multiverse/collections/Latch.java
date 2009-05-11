package org.multiverse.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.TmEntity;

import static java.lang.String.format;

@TmEntity
public final class Latch {

    protected boolean isOpen;

    public Latch() {
        this.isOpen = false;
    }

    public Latch(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void awaitOpen() {
        if (!isOpen)
            retry();
    }

    public void open() {
        isOpen = true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return format("Latch(isOpen=%s)", isOpen);
    }
}
