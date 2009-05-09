package org.multiverse.instrumentation.javaagent.analysis;

/**
 * A strategy responsible for retrieving the Field for a specific fieldpath.
 * <p/>
 * Implementations don't need to be threadsafe.
 *
 * @author Peter Veentjer.
 */
public interface StmAnalyzer {

    /**
     * Retrieves the {@link StmField} for a specific field.
     *
     * @param fieldpath the internal form path to the Field, eg. com/foo/Person.age
     * @return the found Field, or null if no Field is found.
     * @throws NullPointerException     if fieldpath is null.
     * @throws IllegalArgumentException if fieldpath has an invalid syntax.
     */
    StmField findField(String fieldpath);
}
