package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.util.latches.Latch;

import static java.lang.Math.max;
import java.util.Stack;

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
public final class HeapNode {

    private static int height(HeapNode node) {
        return node == null ? 0 : node.height;
    }

    private static int height(HeapNode left, HeapNode right) {
        return max(height(left), height(right)) + 1;
    }

    private static final int COMPARE_SPOT_ON = 0;
    private static final int COMPARE_GO_RIGHT = 1;
    private static final int COMPARE_GO_LEFT = -1;

    private final Block block;
    private final HeapNode left;
    private final HeapNode right;

    private final int height;
    private final int balanceFactor;

    public HeapNode(Block block) {
        assert block != null;

        this.left = null;
        this.right = null;
        this.height = 1;
        this.balanceFactor = 0;
        this.block = block;
    }

    public HeapNode(Block block, HeapNode left, HeapNode right) {
        assert block != null;
        assert left == null || left.block.getDeflated().___getVersion() < block.getDeflated().___getVersion();
        assert right == null || right.block.getDeflated().___getVersion() > block.getDeflated().___getVersion();

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
    public Block getBlock() {
        return block;
    }

    /**
     * Adds a new Listener to the HeapNode. The HeapNode is not altered, but a new HeapNode is
     * returned instead. If the HeapNode containing the value doesn't exist, null is returned.
     * <p/>
     *
     * @param handle
     * @param listener the Latch to register as listener
     * @return the created HeapNode.
     */
    public HeapNode createNewForAddingListener(long handle, Latch listener) {
        int compare = compare(handle);

        switch (compare) {
            case COMPARE_SPOT_ON: {
                Block newBlock = block.createNewForAddingListener(listener);

                //ok, there was a failure, lets return null to indicate this
                if (newBlock == null)
                    return null;

                //if there is no change we can return the original node
                if (newBlock == block)
                    return this;

                //since the left and right trees are balanced, the new node will be balanced.
                return new HeapNode(newBlock, left, right);
            }
            case COMPARE_GO_RIGHT: {
                if (right == null)
                    return null;

                HeapNode newRight = right.createNewForAddingListener(handle, listener);
                if (newRight == null)
                    return null;

                if (newRight == this)
                    return this;

                return new HeapNode(block, left, newRight);
            }
            case COMPARE_GO_LEFT: {
                if (left == null)
                    return null;

                HeapNode newLeft = left.createNewForAddingListener(handle, listener);
                if (newLeft == null)
                    return null;

                if (newLeft == this)
                    return this;

                return new HeapNode(block, newLeft, right);
            }
            default:
                throw new RuntimeException("unhandeled compare " + compare);
        }
    }

    /**
     * Sets the LockMode. The HeapNode is not altered, but a new HeapNode is returned instead.
     *
     * @param owner
     * @param lockMode
     * @param handle
     * @return null on failure
     */
    public HeapNode createNewForUpdatingLockMode(TransactionId owner, LockMode lockMode, long handle) {
        int compare = compare(handle);
        switch (compare) {
            case COMPARE_SPOT_ON: {
                Block newBlock = block.createNewForUpdatingLock(owner, lockMode);

                //ok, there was a failure, lets return null to indicate this
                if (newBlock == null)
                    return null;

                //if there is no change we can return the original node
                if (newBlock == block)
                    return this;

                //since the left and right trees are balanced, the new node will be balanced.
                //todo: ugly cast
                return new HeapNode(newBlock, left, right);
            }
            case COMPARE_GO_RIGHT: {
                HeapNode newRight;
                /*
                if (right == null) {
                    //todo: error
                    newRight = new HeapNode (change, null, null);
                } else {
                    newRight = right.createNewForWrite(change, maximumVersion);
                    //if (newRight == null)
                    //    return null;
                }

                return new HeapNode (block, left, newRight);*/
            }
            case COMPARE_GO_LEFT: {
                HeapNode newLeft;
                /*
                if (left == null) {
                    //todo: error
                    newLeft = new HeapNode (change, null, null);
                } else {
                    newLeft = left.createNewForWrite(change, maximumVersion);
                    //if (newLeft == null)
                    //    return null;
                }

                return new HeapNode (block, newLeft, right);*/
            }
            default:
                throw new RuntimeException("unhandeled compare " + compare);
        }
    }

    /**
     * Creates a new HeapTreeNode based on an old one and a change.
     * <p/>
     * http://upload.wikimedia.org/wikipedia/en/c/c4/Tree_Rebalancing.gif
     *
     * @param deflated the content of the new heapTreeNode.
     * @return the created result.
     */
    public HeapNode createNewForWrite(Deflated deflated, long startOfTransactionVersion, Stack<ListenerNode> listeners) {
        HeapNode unbalanced = createNewForWriteUnbalanced(deflated, startOfTransactionVersion, listeners);
        return unbalanced == null ? null : unbalanced.balance();
    }

    private HeapNode createNewForWriteUnbalanced(Deflated deflated, long startOfTransactionVersion,
                                                 Stack<ListenerNode> listeners) {
        int compare = compare(deflated.___getHandle());

        switch (compare) {
            case COMPARE_SPOT_ON: {
                Block newBlock = block.createNewForUpdate(deflated, startOfTransactionVersion, listeners);

                if (newBlock == null)
                    return null;

                return new HeapNode(newBlock, left, right);
            }
            case COMPARE_GO_RIGHT: {
                HeapNode newRight;
                if (right == null) {
                    newRight = new HeapNode(new Block(deflated));
                } else {
                    newRight = right.createNewForWrite(deflated, startOfTransactionVersion, listeners);
                    if (newRight == null)
                        return null;
                }

                return new HeapNode(block, left, newRight);
            }
            case COMPARE_GO_LEFT: {
                HeapNode newLeft;
                if (left == null) {
                    newLeft = new HeapNode(new Block(deflated));
                } else {
                    newLeft = left.createNewForWrite(deflated, startOfTransactionVersion, listeners);
                    if (newLeft == null)
                        return null;
                }

                return new HeapNode(block, newLeft, right);
            }
            default:
                throw new RuntimeException("unhandeled compare " + compare);
        }
    }

    private HeapNode balance() {
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
    public HeapNode getLeft() {
        return left;
    }

    /**
     * Returns the right branch of this HeapTreeNode. If no right branch is set, null is returned.
     *
     * @return the right branch of this HeapTreeNode.
     */
    public HeapNode getRight() {
        return right;
    }

    /**
     * Does a single right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single right rotation on this HeapTreeNode.
     */
    public HeapNode singleRotateRight() {
        if (left == null)
            throw new IllegalStateException("to do a right rotate, the left field can't be null");

        HeapNode q = this;
        HeapNode p = q.left;
        HeapNode a = p.left;
        HeapNode b = p.right;
        HeapNode c = q.right;

        HeapNode qNew = new HeapNode(q.getBlock(), b, c);
        return new HeapNode(p.getBlock(), a, qNew);
    }

    /**
     * Does a double right rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double right rotation on this HeapTreeNode.
     */
    public HeapNode doubleRotateRight() {
        HeapNode newLeft = left.singleRotateLeft();
        return new HeapNode(block, newLeft, right).singleRotateRight();
    }

    /**
     * Does a single left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the single left rotation on this HeapTreeNode.
     */
    public HeapNode singleRotateLeft() {
        if (right == null)
            throw new IllegalStateException("to do a left rotate, the right field can't be null");

        HeapNode p = this;
        HeapNode q = p.right;
        HeapNode a = p.left;
        HeapNode b = q.left;
        HeapNode c = q.right;
        HeapNode pNew = new HeapNode(p.getBlock(), a, b);
        return new HeapNode(q.getBlock(), pNew, c);
    }

    /**
     * Does a double left rotation on this HeapTreeNode.
     * <p/>
     * http://en.wikipedia.org/wiki/Tree_rotation
     *
     * @return the result of the double left rotation on this HeapTreeNode.
     */
    public HeapNode doubleRotateLeft() {
        HeapNode newRight = right.singleRotateRight();
        return new HeapNode(block, left, newRight).singleRotateLeft();
    }

    /**
     * Returns the height of this tree. The height is the maximum of the left and right tree increased
     * with 1.
     * <p/>
     * The value is calculated up front, so it has a O(c) complexity.
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
        return block.getDeflated().___getHandle();
    }

    /**
     * Returns the size of this HeapTreeNode. The size will always be equal or larger than 0.
     * This method is recursive, and not iterative, so watch out.
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

    private int compare(long thatHandle) {
        if (getHandle() == thatHandle) {
            return COMPARE_SPOT_ON;
        } else if (this.getHandle() < thatHandle) {
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
    public HeapNode find(long handle) {
        HeapNode node = this;
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
