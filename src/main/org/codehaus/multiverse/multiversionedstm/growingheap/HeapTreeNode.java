package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;

public final class HeapTreeNode {
    private final DehydratedStmObject content;
    private final HeapTreeNode left;
    private final HeapTreeNode right;

    public HeapTreeNode(DehydratedStmObject content, HeapTreeNode left, HeapTreeNode right) {
        if (content == null) throw new NullPointerException();
        this.content = content;
        this.left = left;
        this.right = right;
    }

    public long getHandle() {
        return content.getHandle();
    }

    public long getVersion() {
        return content.getVersion();
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
    public HeapTreeNode rightRotate() {
        if (left == null)
            throw new IllegalStateException();

        HeapTreeNode q = this;
        HeapTreeNode p = q.left;
        HeapTreeNode a = p.left;
        HeapTreeNode b = p.right;
        HeapTreeNode c = q.right;

        HeapTreeNode qNew = new HeapTreeNode(q.getContent(), b, c);
        return new HeapTreeNode(p.getContent(), a, qNew);
    }

    /**
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return
     */
    public HeapTreeNode leftRotate() {
        if (right == null)
            throw new IllegalStateException();

        HeapTreeNode p = this;
        HeapTreeNode q = p.right;
        HeapTreeNode a = p.left;
        HeapTreeNode b = q.left;
        HeapTreeNode c = q.right;
        HeapTreeNode pNew = new HeapTreeNode(p.getContent(), a, b);
        return new HeapTreeNode(q.getContent(), pNew, c);
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
    public HeapTreeNode createNew(DehydratedStmObject change) {
        switch (compare(change.getHandle())) {
            case 0:
                //since the left and right trees are balanced, the new node will be balanced.
                return new HeapTreeNode(change, left, right);
            case 1:
                HeapTreeNode newRight;
                if (right == null) {
                    newRight = new HeapTreeNode(change, null, null);
                } else {
                    newRight = right.createNew(change);
                }
                HeapTreeNode x = new HeapTreeNode(content, left, newRight);
                return x;
            case -1:
                HeapTreeNode newLeft;
                if (left == null) {
                    //todo: balancing
                    newLeft = new HeapTreeNode(change, null, null);
                } else {
                    //todo: balancing
                    newLeft = left.createNew(change);
                }
                return new HeapTreeNode(content, newLeft, right);
            default:
                throw new RuntimeException();
        }
    }

    /**
     * Todo: method is recursive.. could be made iterative.
     * todo: if this method is called often for the balancing, the size could also be cached.
     *
     * @return
     */
    public int size() {
        int size = 1;
        if (right != null)
            size += right.size();
        if (left != null)
            size += left.size();
        return size;
    }

    public int getMaximumDepth() {
        int max = 1;

        if (left != null) {
            int d = left.getMaximumDepth();
            if (d > max)
                max = d;
        }

        if (right != null) {
            int d = right.getMaximumDepth();
            if (d > max)
                max = d;
        }

        return max;
    }

    private int compare(long otherHandle) {
        if (getHandle() == otherHandle) {
            return 0;
        } else if (getHandle() < otherHandle) {
            return 1;
        } else {
            return -1;
        }
    }

    public HeapTreeNode find(long key) {
        HeapTreeNode node = this;
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
