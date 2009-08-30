package org.multiverse.utils.spinning;

public interface SpinPolicy {

    /**
     * @return true
     */
    boolean execute(SpinTask spinTask);
}
