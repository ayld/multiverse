package org.multiverse.api.exceptions;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

/**
 * A {@link CommitFailureException} that indicates that
 * the locks could not be acquired while doing a {@link org.multiverse.api.Transaction#commit}.
 *
 * @author Peter Veentjer
 */
public class FailedToObtainLocksException extends CommitFailureException {

    public final static FailedToObtainLocksException INSTANCE = new FailedToObtainLocksException();

    private final static boolean reuse = parseBoolean(getProperty(FailedToObtainLocksException.class.getName()+".reuse", "true"));

    public static FailedToObtainLocksException create() {
        if (reuse) {
            return FailedToObtainLocksException.INSTANCE;
        } else {
            return new FailedToObtainLocksException();
        }
    }

    public FailedToObtainLocksException() {
    }

    public FailedToObtainLocksException(String message) {
        super(message);
    }

    public FailedToObtainLocksException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedToObtainLocksException(Throwable cause) {
        super(cause);
    }
}
