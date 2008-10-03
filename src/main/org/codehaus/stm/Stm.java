package org.codehaus.stm;

import org.codehaus.stm.transaction.Transaction;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Part of the methods should be moved to a new interface: VersionedStm since these methods are very implementation
 * specific. A Lockbased stm doesn't need the versions.
 */
public interface Stm<T extends Transaction> {

    //todo: add version that allows params like readonly

    T startTransaction();

    T startTransaction(long[] addresses, long version) throws InterruptedException;

    T tryStartTransaction(long[] addresses, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
