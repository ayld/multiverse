package org.multiverse.stms.alpha.writeset;

import org.multiverse.api.Tranlocal;
import org.multiverse.stms.alpha.AlphaAtomicObject;

/**
 * An immutable set containing all the Tranlocals that need to be persisted when the transaction
 * commits. It is not a java.util.Set instance to reduce any (object creation) overhead. It is just
 * a single linked list.
 *
 * @author Peter Veentjer.
 */
public final class WriteSet {
    public final WriteSet next;
    public final Tranlocal<AlphaAtomicObject> tranlocal;
    public final int size;

    public WriteSet(WriteSet next, Tranlocal<AlphaAtomicObject> tranlocal) {
        this.next = next;
        this.tranlocal = tranlocal;
        this.size = next == null ? 1 : next.size + 1;
    }

    /**
     * Creates a WriteSet based on an array of tranlocals.
     *
     * @param tranlocals the Tranlocal to store in the WriteSet.
     * @return the created writeset.
     */
    public static WriteSet create(Tranlocal<AlphaAtomicObject>... tranlocals) {
        WriteSet writeSet = null;
        for (int k = tranlocals.length - 1; k >= 0; k--) {
            Tranlocal object = tranlocals[k];
            writeSet = new WriteSet(writeSet, object);
        }
        return writeSet;
    }
}