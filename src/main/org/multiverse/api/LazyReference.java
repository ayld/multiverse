package org.multiverse.api;

/**
 * A lazy loaded reference.
 * <p/>
 * LazyReferences are very useful for dealing with object cycles (something that can occur in
 * arbitrary object graphs).
 * <p/>
 * The LazyReference is not threadsafe to use.
 *
 * @author Peter Veentjer.
 * @param <T>
 */
public interface LazyReference<T> {

    /**
     * Checks if the reference is loaded.
     *
     * @return true if the reference is loaded, false otherwise.
     */
    boolean isLoaded();

    /**
     * Returns the Handle to the reference.
     *
     * @return the handle.
     */
    Handle<T> getHandle();

    /**
     * Gets the reference. If the reference is not loaded, it will be loaded.
     *
     * @return the reference.
     * @throws org.multiverse.api.exceptions.StmException
     *
     * @throws IllegalStateException
     */
    T get();
}
