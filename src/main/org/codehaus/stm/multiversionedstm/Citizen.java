package org.codehaus.stm.multiversionedstm;

import java.util.Iterator;

/**
 * Implementaties van Traced kunnen heel hard gekoppeld zijn aan specifieke transactie implementaties.
 * wellicht is tracedobject alleen maar transactie implementatie specifiek en valt het helemaal niet in een
 * generiek mechanisme samen te vatten.
 */
public interface Citizen {

    void ___onAttach(MultiversionedStm.MultiversionedTransaction multiversionedTransaction);

    MultiversionedStm.MultiversionedTransaction ___getTransaction();

    Iterator<Citizen> ___findNewlyborns();

    /**
     * Returns the pointer of this Transactionized object in the stm. If the object is still transient, 0 is returned.
     *
     * @return
     */
    long ___getPointer();

    HydratedCitizen ___hydrate();

    boolean ___isDirty();

    void ___setPointer(long ptr);    
}
