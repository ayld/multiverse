package org.codehaus.multiverse.multiversionedheap.standard;

import static java.lang.Math.max;

/**
 * A binary node that can be placed in the GrowingMultiversionedHeap.
 * <p/>
 * Searches have a complexity of O(log n)
 * <p/>
 * HeapTreeNodes are immutable.
 * <p/>
 * http://en.wikipedia.org/wiki/AVL_tree
 *
 * @author Peter Veentjer.
 */
public final class DefaultHeapNode<B extends Block> implements HeapNode<B> {

    private static int height(DefaultHeapNode node) {
        return node == null ? 0 : node.height;
    }

    private static int height(DefaultHeapNode left, DefaultHeapNode right) {
        return max(height(left), height(right)) + 1;
    }

    private static final int COMPARE_SPOT_ON = 0;
    private static final int COMPARE_GO_RIGHT = 1;
    private static final int COMPARE_GO_LEFT = -1;

    private final B block;
    private final DefaultHeapNode<B> left;
    private final DefaultHeapNode<B> right;

    private final int height;
    private final int balanceFactor;

    public DefaultHeapNode(B block, DefaultHeapNode<B> left, DefaultHeapNode<B> right) {
        assert block != null;
        assert left == null || left.block.getInflatable().___getVersion() < block.getInflatable().___getVersion();
        assert right == null || right.block.getInflatable().___getVersion() > block.getInflatable().___getVersion();

        this.block = block;
        this.left = left;
        this.right = right;

        this.height = height(left, right);
        this.balanceFactor = height(right) - height(left);
    }


    /**
     * Returns the actual content of this HeapTreeNode. Value will never be null.
     *
     * @return the content of this HeapTreeNode.
     */
    public B getBlock() {
        return block;
    }


    /**
     * Creates a new HeapTreeNode based on an old one and a change. This algorithm is recursive
     * and not iterative.
     * <p/>
     * http://upload.wikimedia.org/wikipedia/en/c/c4/Tree_Rebalancing.gif
     *
     * @param change the content of the new heapTreeNode.
     * @return the created result.
     */
    public DefaultHeapNode<B> write(B change, long expectedVersion) {
        DefaultHeapNode<B> unbalanced = createUnbalanced(change, expectedVersion);
        return unbalanced == null ? null : unbalanced.balance();
    }

    private DefaultHeapNode<B> createUnbalanced(B change, long maximumVersion) {
        int compare = compare(change.getHandle());
        switch (compare) {
            case COMPARE_SPOT_ON: {
                long foundVersion = this.block.getInflatable().___getVersion();
                if (maximumVersion < foundVersion)
                    return null;

                //since the left and right trees are balanced, the new node will be balanced.
                return new DefaultHeapNode<B>(change, left, right);
            }
            case COMPARE_GO_RIGHT: {
                DefaultHeapNode<B> newRight;
                if (right == null) {
                    newRight = new DefaultHeapNode<B>(change, null, null);
                } else {
                    newRight = right.write(change, maximumVersion);
                    if (newRight == null)
                        return null;
                }

                return new DefaultHeapNode<B>(block, left, newRight);
            }
            case COMPARE_GO_LEFT: {
                DefaultHeapNode<B> newLeft;
                if (left == null) {
                    newLeft = new DefaultHeapNode<B>(change, null, null);
                } else {
                    newLeft = left.write(change, maximumVersion);
                    if (newLeft == null)
                        return null;
                }

                return new DefaultHeapNode<B>(block, newLeft, right);
            }
            default:
                throw new RuntimeException("unhandeled compare " + compare);
        }
    }

    private DefaultHeapNode<B> balance() {
        switch (balanceFactor()) {
            case 0:
                return this;
            case 1:
                return this;
            case -1:
                return this;
            case 2:
                //het is een right/right of een right/left case
                //is the right right heavy, or left heavy
                int rightBalanceFactor = right.balanceFactor();
                if (rightBalanceFactor == 1)
                    return this.singleRotateLeft();
                else
                    return this.doubleRotateLeft();
            case -2:
                //is the left/left  heavy, or left/right heavy
                int leftBalanceFactor = left.balanceFactor();
                if (leftBalanceFactor == -1)
                    return this.singleRotateRight();
                else
                    return this.doubleRotateRight();
            default:
                throw new RuntimeException("unhandeled balanceFactor: " + balanceFactor());
        }
    }

    /**
     * Returns the left branch of this HeapTreeNode. If no left branch is set, null is returned.
     *
     * @return the left branch of this HeapTreeNode.
     */
    public DefaultHeapNode<B> getLeft() {
        return left;
    }

    /**
     * Returns the right branch of this HeapTreeNode. If no right branch is set, null is returned.
     *
     * @return the right branch of this HeapTreeNode.
     */
    public DefaultHeapNode<B> getRight() {
        return right;
    }

    /**
     * Does a single right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single right rotation on this HeapTreeNode.
     */
    public DefaultHeapNode<B> singleRotateRight() {
        if (left == null)
            throw new IllegalStateException("to do a right rotate, the left field can't be null");

        DefaultHeapNode<B> q = this;
        DefaultHeapNode<B> p = q.left;
        DefaultHeapNode<B> a = p.left;
        DefaultHeapNode<B> b = p.right;
        DefaultHeapNode<B> c = q.right;

        DefaultHeapNode<B> qNew = new DefaultHeapNode<B>(q.getBlock(), b, c);
        return new DefaultHeapNode<B>(p.getBlock(), a, qNew);
    }

    /**
     * Does a double right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double right rotation on this HeapTreeNode.
     */
    public DefaultHeapNode<B> doubleRotateRight() {
        DefaultHeapNode<B> newLeft = left.singleRotateLeft();
        return new DefaultHeapNode<B>(block, newLeft, right).singleRotateRight();
    }

    /**
     * Does a single left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single left rotation on this HeapTreeNode.
     */
    public DefaultHeapNode<B> singleRotateLeft() {
        if (right == null)
            throw new IllegalStateException("to do a left rotate, the right field can't be null");

        DefaultHeapNode<B> p = this;
        DefaultHeapNode<B> q = p.right;
        DefaultHeapNode<B> a = p.left;
        DefaultHeapNode<B> b = q.left;
        DefaultHeapNode<B> c = q.right;
        DefaultHeapNode<B> pNew = new DefaultHeapNode<B>(p.getBlock(), a, b);
        return new DefaultHeapNode<B>(q.getBlock(), pNew, c);
    }

    /**
     * Does a double left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double left rotation on this HeapTreeNode.
     */
    public DefaultHeapNode<B> doubleRotateLeft() {
        DefaultHeapNode<B> newRight = right.singleRotateRight();
        return new DefaultHeapNode<B>(block, left, newRight).singleRotateLeft();
    }

    /**
     * Returns the height of this tree. The height is the maximum of the left and right tree increased
     * with 1.
     * <p/>
     * The value is calculated up front, so it has a O(c) complexity instead of an
     *
     * @return the height of this tree.
     */
    public int height() {
        return height;
    }

    /**
     * The handle of the content. This handle is used to do searches.
     *
     * @return the handle of the content.
     */
    public long getHandle() {
        return block.getInflatable().___getHandle();
    }

    /**
     * Returns the size of this HeapTreeNode. The size will always be equal or larger than 0.
     * This method is recursive, and not iterative.
     *
     * @return the total number of nodes.
     */
    public int size() {
        int size = 1;
        if (right != null)
            size += right.size();
        if (left != null)
            size += left.size();
        return size;
    }

    /**
     * Returns the balance factor (the height of the right minus height of the left). This can be used to
     * balance trees.
     *
     * @return the balance factory.
     */
    public int balanceFactor() {
        return balanceFactor;
    }

    private int compare(long otherHandle) {
        if (getHandle() == otherHandle) {
            return COMPARE_SPOT_ON;
        } else if (getHandle() < otherHandle) {
            return COMPARE_GO_RIGHT;
        } else {
            return COMPARE_GO_LEFT;
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This method is iterative. So no worries about tree height.
     */
    public HeapNode<B> find(long handle) {
        DefaultHeapNode<B> node = this;
        do {
            switch (node.compare(handle)) {
                case COMPARE_SPOT_ON:
                    return node;
                case COMPARE_GO_RIGHT:
                    node = node.right;
                    break;
                case COMPARE_GO_LEFT:
                    node = node.left;
                    break;
                default:
                    throw new RuntimeException("unhandled case");
            }
        } while (node != null);

        return null;
    }
}
