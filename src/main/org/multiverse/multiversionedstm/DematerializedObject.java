package org.multiverse.multiversionedstm;

import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;

/**
 * The result of the dematerialization of an {@link MaterializedObject}. A MaterializedObject is
 * dematerialized when it is stored (transaction is committed) and it is rematerialized when it is
 * read within a transaction.
 *
 * @author Peter Veentjer.
 */
public interface DematerializedObject {

    Handle getHandle();

    MaterializedObject rematerialize(Transaction t);
}
