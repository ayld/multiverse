package org.multiverse.contentionmanagers;

import org.multiverse.api.Transaction;

/**
 * For more information see 'the art of multiprocessor programming' chapter 18.3.5
 *
 * ContentionManagers are not used yet.
 *
 * @author Peter Veentjer.
 */
public interface ContentionManager {

    void resolve(Transaction me, Transaction other);
}
