package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedheap.Deflated;

/**
 * Created by IntelliJ IDEA.
 * User: alarmnummer
 * Date: Mar 23, 2009
 * Time: 5:17:52 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DehydratedStmObject extends Deflated {

    /**
     * Inflates a Deflated to a Deflatable.
     * <p/>
     * todo: this method should be removed from this interface.
     *
     * @param transaction the transaction the created Deflatable is part of.
     * @return the created StmObject.
     * @see org.codehaus.multiverse.multiversionedstm.StmObject#___deflate ()
     * @see org.codehaus.multiverse.multiversionedstm.StmObject#___isImmutableObjectGraph()
     */
    StmObject ___inflate(Transaction transaction);
}
