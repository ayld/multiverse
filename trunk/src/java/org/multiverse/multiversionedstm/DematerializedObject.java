package org.multiverse.multiversionedstm;

import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;

public interface DematerializedObject {

    Originator getOriginator();

    MaterializedObject rematerialize(Transaction t);
}
