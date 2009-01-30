package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;

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
public final class DefaultHeapNode implements HeapNode {

    private static int height(DefaultHeapNode node) {
        return node == null ? 0 : node.height;
    }

    private static int height(DefaultHeapNode left, DefaultHeapNode right) {
        return max(height(left), height(right)) + 1;
    }

    private static final int COMPARE_SPOT_ON = 0;
    private static final int COMPARE_GO_RIGHT = 1;
    private static final int COMPARE_GO_LEFT = -1;

    private final DehydratedStmObject content;
    private final DefaultHeapNode left;
    private final DefaultHeapNode right;

    //todo: version should be part of the content.
    private final int height;

    public DefaultHeapNode(DehydratedStmObject content, DefaultHeapNode left, DefaultHeapNode right) {
        if (content == null) throw new NullPointerException();
        //todo: left en rightside content could be checked for violation
        this.content = content;
        this.left = left;
        this.right = right;
        this.height = height(left, right);
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
        return content.getHandle();
    }

    /**
     * Returns the version of the content.
     *
     * @return the version of the content.
     */
    public long getVersion() {
        return content.getVersion();
    }

    /**
     * Returns the actual content of this HeapTreeNode. Value will never be null.
     *
     * @return the content of this HeapTreeNode.
     */
    public DehydratedStmObject getContent() {
        return content;
    }

    /**
     * Returns the left branch of this HeapTreeNode. If no left branch is set, null is returned.
     *
     * @return the left branch of this HeapTreeNode.
     */
    public DefaultHeapNode getLeft() {
        return left;
    }

    /**
     * Returns the right branch of this HeapTreeNode. If no right branch is set, null is returned.
     *
     * @return the right branch of this HeapTreeNode.
     */
    public DefaultHeapNode getRight() {
        return right;
    }

    /**
     * Does a single right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single right rotation on this HeapTreeNode.
     */
    public DefaultHeapNode singleRotateRight() {
        if (left == null)
            throw new IllegalStateException("to do a right rotate, the left field can't be null");

        DefaultHeapNode q = this;
        DefaultHeapNode p = q.left;
        DefaultHeapNode a = p.left;
        DefaultHeapNode b = p.right;
        DefaultHeapNode c = q.right;

        DefaultHeapNode qNew = new DefaultHeapNode(q.getContent(), b, c);
        return new DefaultHeapNode(p.getContent(), a, qNew);
    }

    /**
     * Does a double right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double right rotation on this HeapTreeNode.
     */
    public DefaultHeapNode doubleRotateRight() {
        DefaultHeapNode newLeft = left.singleRotateLeft();
        return new DefaultHeapNode(content, newLeft, right).singleRotateRight();
    }

    /**
     * Does a single left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single left rotation on this HeapTreeNode.
     */
    public DefaultHeapNode singleRotateLeft() {
        if (right == null)
            throw new IllegalStateException("to do a left rotate, the right field can't be null");

        DefaultHeapNode p = this;
        DefaultHeapNode q = p.right;
        DefaultHeapNode a = p.left;
        DefaultHeapNode b = q.left;
        DefaultHeapNode c = q.right;
        DefaultHeapNode pNew = new DefaultHeapNode(p.getContent(), a, b);
        return new DefaultHeapNode(q.getContent(), pNew, c);
    }

    /**
     * Does a double left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double left rotation on this HeapTreeNode.
     */
    public DefaultHeapNode doubleRotateLeft() {
        DefaultHeapNode newRight = right.singleRotateRight();
        return new DefaultHeapNode(content, left, newRight).singleRotateLeft();
    }

    /**
     * Creates a new HeapTreeNode based on an old one and a change.
     * <p/>
     * todo: algorithm is recursive and not iterative
     * <p/>
     * http://upload.wikimedia.org/wikipedia/en/c/c4/Tree_Rebalancing.gif
     *
     * @param change the content of the new heapTreeNode.
     * @return the created result.
     */
    public DefaultHeapNode createNew(DehydratedStmObject change) {
        DefaultHeapNode unbalanced = createUnbalanced(change);
        return unbalanced.balance();
    }

    private DefaultHeapNode balance() {
        int balanceFactor = balanceFactor();
        switch (balanceFactor) {
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
                throw new RuntimeException("unhandeled balanceFactor: " + balanceFactor);
        }
    }

    private DefaultHeapNode createUnbalanced(DehydratedStmObject change) {
        int compare = compare(change.getHandle());
        switch (compare) {
            case COMPARE_SPOT_ON:
                //since the left and right trees are balanced, the new node will be balanced.
                return new DefaultHeapNode(change, left, right);
            case COMPARE_GO_RIGHT:
                DefaultHeapNode newRight;
                if (right == null)
                    newRight = new DefaultHeapNode(change, null, null);
                else
                    newRight = right.createNew(change);
                return new DefaultHeapNode(content, left, newRight);
            case COMPARE_GO_LEFT:
                DefaultHeapNode newLeft;
                if (left == null)
                    newLeft = new DefaultHeapNode(change, null, null);
                else
                    newLeft = left.createNew(change);
                return new DefaultHeapNode(content, newLeft, right);
            default:
                throw new RuntimeException("unhandeled compare " + compare);
        }
    }


    /**
     * Returns the size of this HeapTreeNode. The size will always be equal or larger than 0.
     * <p/>
     * Todo: method is recursive.. could be made iterative.
     * todo: if this method is called often for the balancing, the size could also be cached.
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
        return height(right) - height(left);
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
     * Finds the HeapTreeNode with the provided handle.
     *
     * @param handle the HeapTreeNode to look for.
     * @return the found HeapTreeNode, or null of none is fonund.
     */
    public HeapNode find(long handle) {
        DefaultHeapNode node = this;
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
