package org.codehaus.multiverse.util.balancedtree;

public class HeapTreeNode<V> {
    private final long handle;
    private final V value;
    private final HeapTreeNode<V> left;
    private final HeapTreeNode<V> right;

    public HeapTreeNode(long handle, V value, HeapTreeNode<V> left, HeapTreeNode<V> right) {
        this.handle = handle;
        this.value = value;
        this.left = left;
        this.right = right;
    }

    public long getHandle() {
        return handle;
    }

    public V getValue() {
        return value;
    }

    public HeapTreeNode<V> getLeft() {
        return left;
    }

    public HeapTreeNode<V> getRight() {
        return right;
    }

    private int compare(long key) {
        if (handle == key) {
            return 0;
        } else if (handle < key) {
            return 1;
        } else {
            return -1;
        }
    }

    public HeapTreeNode find(long key) {
        HeapTreeNode<V> node = this;
        do {
            switch (node.compare(key)) {
                case 0:
                    return node;
                case 1:
                    node = node.right;
                    break;
                case -1:
                    node = node.left;
                    break;
                default:
                    throw new RuntimeException("unhandled case");
            }
        } while (node != null);

        return null;
    }
}
