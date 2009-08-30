package org.multiverse.utils.spinning;

/**
 * A task executed by the SpinPolicy.
 *
 * @author Peter Veentjer.
 */
public interface SpinTask {

    /**
     * Runs the SpinTask.
     *
     * @return true if the task was executed successfully, false otherwise. Tasks are allowed to
     *         throw runtime exceptions without causing any problems.
     */
    boolean run();
}
