package org.multiverse.tmutils;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.TmEntity;

import static java.lang.String.format;

@TmEntity
public final class TmLatch {

    private boolean isOpen;

    public TmLatch() {
        this.isOpen = false;
    }

    public TmLatch(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void awaitOpen() {
        if (!isOpen) {
            retry();
        }
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
