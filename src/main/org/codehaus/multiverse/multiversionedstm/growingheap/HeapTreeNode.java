package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;

import static java.lang.Math.max;

/**
 * A binary node that can be placed in the GrowingMultiversionedHeap.
 * <p/>
 * Searchs have a complexity of O(log n)
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
    private int height;

    public HeapTreeNode(DehydratedStmObject content, long version, HeapTreeNode left, HeapTreeNode right) {
        if (content == null) throw new NullPointerException();
        //todo: left en rightside content could be checked for violation
        this.version = version;
        this.content = content;
        this.left = left;
        this.right = right;
        this.height = max(height(left), height(right)) + 1;
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

    public HeapTreeNode doubleRotateRight() {
        throw new RuntimeException();
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

    public HeapTreeNode doubleRotateLeft() {
        throw new RuntimeException();
    }

    /**
     * Creates a new HeapTreeNode based on an old one and a change.
     * <p/>
     * todo: tree is not balanced..
     * todo: algorithm is recursive and not iterative
     *
     * @param change
     * @return
     */
    public HeapTreeNode createNew(DehydratedStmObject change, long changeVersion) {
        HeapTreeNode result;
        switch (compare(change.getHandle())) {
            case COMPARE_SPOT_ON:
                //since the left and right trees are balanced, the new node will be balanced.
                return new HeapTreeNode(change, changeVersion, left, right);
            case COMPARE_GO_RIGHT:
                //the node will be added to the right side.. Is the child going to take care
                //of it, or should it be attached to this node directly.
                if (right == null) {
                    //this node is taking care of the addition.
                    //adding it to the right would not lead to a large unbalance.
                    HeapTreeNode newRight = new HeapTreeNode(change, changeVersion, null, null);
                    return new HeapTreeNode(content, changeVersion, left, newRight);
                } else {
                    //the child is taking care of the addition
                    //it could be that the newTree leads to an unbalanced tree.
                    HeapTreeNode newRight = right.createNew(change, changeVersion);
                    int rightHeight = newRight.height();
                    int leftHeight = height(left);
                    int balanceFactor = rightHeight - leftHeight;
                    if (balanceFactor == 0 || balanceFactor == 1)
                        return new HeapTreeNode(content, changeVersion, left, newRight);

                    HeapTreeNode newLeft = new HeapTreeNode(this.content, this.getVersion(), this.left, newRight.getLeft());
                    return new HeapTreeNode(newRight.getContent(), newRight.getVersion(), newLeft, newRight.getRight());
                }
            case COMPARE_GO_LEFT:
                if (left == null) {
                    HeapTreeNode newLeft = new HeapTreeNode(change, changeVersion, null, null);
                    return new HeapTreeNode(content, changeVersion, newLeft, right);
                } else {
                    //the child is taking care of the addition
                    //it could be that the newTree leads to an unbalanced tree.
                    HeapTreeNode newLeft = left.createNew(change, changeVersion);
                    int leftHeight = newLeft.height();
                    int rightHeight = height(right);
                    int balanceFactor = rightHeight - leftHeight;
                    if (balanceFactor == 0 || balanceFactor == -1)
                        return new HeapTreeNode(content, changeVersion, newLeft, right);

                    HeapTreeNode newRight = new HeapTreeNode(this.content, this.getVersion(), newLeft.getLeft(), this.right);
                    return new HeapTreeNode(newLeft.getContent(), newLeft.getVersion(), newLeft.getRight(), newRight);
                }
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

    public int height() {
        return height;
    }

    private static int height(HeapTreeNode node) {
        return node == null ? 0 : node.height();
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
