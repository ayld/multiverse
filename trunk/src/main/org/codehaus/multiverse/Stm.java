package org.codehaus.multiverse;

import org.codehaus.multiverse.transaction.Transaction;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Part of the methods should be moved to a new interface: VersionedStm since these methods are very implementation
 * specific. A Lockbased stm doesn't need the versions.
 */
public interface Stm<T extends Transaction> {

    //todo: add version that allows params like readonly

    T startTransaction();

    T startTransaction(Transaction predecessor) throws InterruptedException;

    T tryStartTransaction(Transaction predecessor, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
