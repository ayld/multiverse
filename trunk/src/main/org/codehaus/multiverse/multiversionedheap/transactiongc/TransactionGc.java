package org.codehaus.multiverse.multiversionedheap.transactiongc;

import org.codehaus.multiverse.api.Transaction;

/**
 * The TransactionGc is responsible for aborting transactions that are 'dangling'. It is important
 * that transaction are committed/aborted so that the resources they have claimed (for example locks
 * in the heap) are cleared.
 * <p/>
 * Implementation probably are going to make use of some kind of weakreference approach to connect
 * to the gc.
 *
 * @author Peter Veentjer.
 */
public interface TransactionGc {

    void registerStarted(Transaction transaction);

    void registerFinished(Transaction transaction);

}
