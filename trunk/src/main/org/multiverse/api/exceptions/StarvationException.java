package org.multiverse.api.exceptions;

/**
 * An {@link StmException} that is thrown to indicate the a transaction could not get access to some kind
 * of resource (for example a lock) and therefor is suffering from starvation.
 *
 * @author Peter Veentjer
 */
public class StarvationException extends StmException {

    public static final StarvationException INSTANCE = new StarvationException();

    public StarvationException() {
    }

    public StarvationException(String msg) {
        super(msg);
    }
}
