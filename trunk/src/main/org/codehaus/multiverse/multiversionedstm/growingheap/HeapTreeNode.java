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
public final class HeapTreeNode {
    private static final int COMPARE_SPOT_ON = 0;
    private static final int COMPARE_GO_RIGHT = 1;
    private static final int COMPARE_GO_LEFT = -1;

    private final DehydratedStmObject content;
    private final HeapTreeNode left;
    private final HeapTreeNode right;

    //todo: version should be part of the content.
    private final long version;
    private final int height;

    public HeapTreeNode(DehydratedStmObject content, long version, HeapTreeNode left, HeapTreeNode right) {
        if (content == null) throw new NullPointerException();
        //todo: left en rightside content could be checked for violation
        this.version = version;
        this.content = content;
        this.left = left;
        this.right = right;
        this.height = height(left, right);
    }

    public int height() {
        return height;
    }

    private static int height(HeapTreeNode node) {
        return node == null ? 0 : node.height();
    }

    private static int height(HeapTreeNode left, HeapTreeNode right) {
        return max(height(left), height(right)) + 1;
    }

    private static int balanceFactor(HeapTreeNode left, HeapTreeNode right) {
        return height(right) - height(left);
    }

    public long getHandle() {
        return content.getHandle();
    }

    public long getVersion() {
        return version;
    }

    public DehydratedStmObject getContent() {
        return content;
    }

    public HeapTreeNode getLeft() {
        return left;
    }

    public HeapTreeNode getRight() {
        return right;
    }

    /**
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return
     */
    public HeapTreeNode singleRotateRight() {
        if (left == null)
            throw new IllegalStateException("to do a right rotate, the left field can't be null");

        HeapTreeNode q = this;
        HeapTreeNode p = q.left;
        HeapTreeNode a = p.left;
        HeapTreeNode b = p.right;
        HeapTreeNode c = q.right;

        HeapTreeNode qNew = new HeapTreeNode(q.getContent(), q.getVersion(), b, c);
        return new HeapTreeNode(p.getContent(), p.getVersion(), a, qNew);
    }

    /**
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return
     */
    public HeapTreeNode doubleRotateRight() {
        HeapTreeNode newLeft = left.singleRotateLeft();
        return new HeapTreeNode(content, version, newLeft, right).singleRotateRight();
    }

    /**
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return
     */
    public HeapTreeNode singleRotateLeft() {
        if (right == null)
            throw new IllegalStateException("to do a left rotate, the right field can't be null");

        HeapTreeNode p = this;
        HeapTreeNode q = p.right;
        HeapTreeNode a = p.left;
        HeapTreeNode b = q.left;
        HeapTreeNode c = q.right;
        HeapTreeNode pNew = new HeapTreeNode(p.getContent(), p.getVersion(), a, b);
        return new HeapTreeNode(q.getContent(), q.getVersion(), pNew, c);
    }

    /**
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return
     */
    public HeapTreeNode doubleRotateLeft() {
        HeapTreeNode newRight = right.singleRotateRight();
        return new HeapTreeNode(content, version, left, newRight).singleRotateLeft();
    }

    /**
     * Creates a new HeapTreeNode based on an old one and a change.
     * <p/>
     * todo: tree is not balanced..
     * todo: algorithm is recursive and not iterative
     * <p/>
     * http://upload.wikimedia.org/wikipedia/en/c/c4/Tree_Rebalancing.gif
     *
     * @param change
     * @param changeVersion
     * @return
     */
    public HeapTreeNode createNew(DehydratedStmObject change, long changeVersion) {
        HeapTreeNode unbalanced = createUnbalanced(change, changeVersion);
        return unbalanced.balance();

    }

    private HeapTreeNode balance() {
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

    private HeapTreeNode createUnbalanced(DehydratedStmObject change, long changeVersion) {
        switch (compare(change.getHandle())) {
            case COMPARE_SPOT_ON:
                //since the left and right trees are balanced, the new node will be balanced.
                return new HeapTreeNode(change, changeVersion, left, right);
            case COMPARE_GO_RIGHT:
                HeapTreeNode newRight;
                if (right == null)
                    newRight = new HeapTreeNode(change, changeVersion, null, null);
                else
                    newRight = right.createNew(change, changeVersion);
                return new HeapTreeNode(content, version, left, newRight);
            case COMPARE_GO_LEFT:
                HeapTreeNode newLeft;
                if (left == null)
                    newLeft = new HeapTreeNode(change, changeVersion, null, null);
                else
                    newLeft = left.createNew(change, changeVersion);
                return new HeapTreeNode(content, version, newLeft, right);
            default:
                throw new RuntimeException();
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

    public HeapTreeNode find(long key) {
        HeapTreeNode node = this;
        do {
            switch (node.compare(key)) {
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
