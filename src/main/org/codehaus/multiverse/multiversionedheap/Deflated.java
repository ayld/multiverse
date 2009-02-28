package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.util.iterators.PLongIterator;

/**
 * A Deflated is the immutable result deflation of a Deflatable. A Deflated should not be mutable because
 * a single deflated instance could be shared between concurrent transactions.
 * <p/>
 * So if you have for example a Person class that is part of the stm, a Person can be deflated when the commit
 * happens and the result could be a DeflatedPerson. This DeflatedPerson is stored in the heap. Once a transaction
 * needs a instance, the DeflatedPerson is read, a new instance is created based on this DeflatedPerson (so an inflation)
 * and this instance can be used by the transaction. It is possible that the same DeflatedPerson is read by concurrent
 * transactions, that is why a DeflatedPerson should be immutable.
 * <p/>
 * By using polymorfism, ( {@link #___inflate(Transaction)} different classes can be instantiated.
 *
 * @author Peter Veentjer.
 * @see org.codehaus.multiverse.multiversionedstm.StmObject
 */
public interface Deflated {

    /**
     * Gets the version of this Deflated.
     *
     * @return the version of this Deflated.
     */
    long ___getVersion();

    /**
     * Returns the handle of the Deflated
     *
     * @return the handle of the Deflated.
     */
    long ___getHandle();

    /**
     * Returns an iterator containing all handles to the member {@link Deflated}. It could be that the
     * iterator contains the handle of the object itself. So if cycles become an issue, the caller should take
     * care of this.
     *
     * @return an iterator over all handles to Deflated members.
     */
    PLongIterator ___members();

    /**
     * Inflates a Deflated to a Deflatable.
     *
     * @param transaction the transaction the created Deflatable is part of.
     * @return the created StmObject.
     * @see org.codehaus.multiverse.multiversionedstm.StmObject#___deflate ()
     * @see org.codehaus.multiverse.multiversionedstm.StmObject#___isImmutable()
     */
    Deflatable ___inflate(Transaction transaction);
}
