package org.codehaus.multiverse.transaction;

/**
 *
 * todo: should this be an exception? And what about the costs of creating an exception (especially the stacktrace).
 * perhaps create them upfront and reuse them over and over?? Sounds crazy and is not something you normally want
 * to do, but a retry is not really an exception.
 *
 * @author Peter Veentjer.
 */
public class RetryException extends RuntimeException {
    
}
