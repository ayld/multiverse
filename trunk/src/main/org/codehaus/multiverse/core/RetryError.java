package org.codehaus.multiverse.core;

/**
 * An {@link Error} to indicate that an transaction should be retried. This error is required for the STM version
 * if the condition variables. The reason why it is an error and not an exception, is that it should not be
 * caught.
 * <p/>
 * Although the actual exception handling is not very expensive, creating the stacktrace inside the exception
 * is. That is why there alreay is an instance that can be thrown. Normally this would be a very bad practice
 * but we are using an exception to regulate control flow. This would normally also be a very bad practice, but
 * to make the retry behavior in the Java language completely transparant, this is needed.
 *
 * @author Peter Veentjer.
 */
public class RetryError extends Error {

    public final static RetryError INSTANCE = new RetryError();

    public RetryError() {
    }
}
