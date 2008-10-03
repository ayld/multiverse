package org.codehaus.stm.multiversionedstm2;

public interface Citizen {

    boolean isDirty();

    /**
     * Returns the DehydratedCitizen this Citizen is based on. If this Citizen is fresh, null will be
     * returned.
     *
     * @return
     */
    DehydratedCitizen getInitial();

    /**
     * Dehydrates this Citizen.
     * 
     * @return the DehydratedCitizen
     */
    DehydratedCitizen dehydrate();
}
