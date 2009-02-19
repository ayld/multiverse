package org.codehaus.multiverse.multiversionedstm.utils;

/**
 * A 'collection' where items can be added fast, and a random item can be removed fast. This structure
 * is meant for performance.
 * <p/>
 * The size method is not supported, increases the amount of calculation and the size of
 * the bag. We want this baby to be fast.
 * <p/>
 * The Bag is not threadsafe.
 * <p/>
 * The Bag has no equals/hashcode support.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public final class Bag<E> {
    private Node<E> root;

    /**
     * Adds an item to this bag. Duplicates are not filtered.
     *
     * @param item the item to add.
     * @throws NullPointerException if item is null.
     */
    public void add(E item) {
        if (item == null) throw new NullPointerException();
        root = new Node<E>(item, root);
    }

    /**
     * Checks if the bag is empty or not.
     *
     * @return true if this Bag is empty, false otherwise.
     */
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "[]";

        StringBuffer sb = new StringBuffer();
        Node node = root;
        sb.append(node.value);
        while (node != null) {
            sb.append(",");
            sb.append(node.value);
            node = node.parent;
        }
        return sb.toString();
    }

    /**
     * Takes any element from the Bag.
     *
     * @return the taken element.
     * @throws IllegalStateException if the Bag is empty.
     */
    public E takeAny() {
        if (root == null)
            throw new IllegalStateException();
        Node<E> oldRoot = root;
        root = oldRoot.parent;
        return oldRoot.value;
    }

    static class Node<E> {
        final E value;
        final Node<E> parent;

        Node(E value, Node<E> parent) {
            this.value = value;
            this.parent = parent;
        }
    }
}
