package org.multiverse.benchmarks.drivers.deucetests;

public class OverdraftException extends Exception {

    public OverdraftException() {
    }

    public OverdraftException(String reason) {
        super(reason);
    }
}
