package org.multiverse.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.Math.max;

public final class BTree<K extends Comparable, V> implements MaterializedObject {

    private Node<K, V> root;

    public BTree() {
        this.originator = new DefaultOriginator<BTree<K, V>>();
    }

    public V put(K key, V value) {
        if (key == null)
            throw new NullPointerException();

        if (root == null) {
            root = new Node<K, V>(key, value);
            return null;
        } else {
            return root.put(key, value);
        }
    }

    public V remove(K key) {
        if (key == null)
            throw new NullPointerException();

        return root == null ? null : root.remove(key);
    }

    public boolean containsKey(K key) {
        if (key == null)
            throw new NullPointerException();

        return root.find(key) != null;
    }

    public V find(K key) {
        if (key == null)
            throw new NullPointerException();

        return root == null ? null : root.find(key);
    }

    public int height() {
        return root == null ? 0 : root.height();
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return root == null ? 0 : root.size();
    }

    public void clear() {
        root = null;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof BTree))
            return false;

        BTree<K, V> that = (BTree<K, V>) thatObj;
        if (that.root == this.root)
            return true;

        throw new RuntimeException();
    }

    //todo: equals
    //todo: hash
    //todo: toString
    //todo: asList
    //todo: iterator

    enum Direction {
        goLeft, goRight, spotOn
    }

    static class Node<K extends Comparable, V> implements MaterializedObject {

        private K key;

        private V value;
        private LazyReference<V> valueRef;

        private Node<K, V> left;
        private LazyReference<Node<K, V>> leftRef;

        private Node<K, V> right;
        private LazyReference<Node<K, V>> rightRef;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.originator = new DefaultOriginator<Node<K, V>>();
        }

        private Node<K, V> readRight() {
            if (rightRef != null) {
                right = rightRef.get();
                rightRef = null;
            }
            return right;
        }

        private Node<K, V> writeRight(Node<K, V> newRight) {
            this.right = newRight;
            this.rightRef = null;
            return right;
        }

        private Node<K, V> readLeft() {
            if (leftRef != null) {
                left = leftRef.get();
                leftRef = null;
            }
            return left;
        }

        private Node<K, V> writeLeft(Node<K, V> newLeft) {
            this.left = newLeft;
            this.rightRef = null;
            return left;
        }

        private V readValue() {
            if (valueRef != null) {
                value = valueRef.get();
                valueRef = null;
            }
            return value;
        }

        private V writeValue(V newValue) {
            this.value = newValue;
            this.valueRef = null;
            return value;
        }

        public V put(K newKey, V newValue) {
            switch (getDirection(newKey)) {
                case spotOn: {
                    V oldValue = readValue();
                    value = newValue;
                    return oldValue;
                }
                case goLeft: {
                    if (readLeft() == null) {
                        left = new Node<K, V>(newKey, newValue);
                        return null;
                    } else {
                        return left.put(newKey, newValue);
                    }
                }
                case goRight: {
                    if (readRight() == null) {
                        right = new Node<K, V>(newKey, newValue);
                        return null;
                    } else {
                        return right.put(newKey, newValue);
                    }
                }
                default:
                    throw new RuntimeException();
            }
        }

        public V remove(K key) {
            throw new RuntimeException();
        }

        public int size() {
            return 1 + size(readLeft()) + size(readRight());
        }

        private int size(Node node) {
            return node == null ? 0 : node.size();
        }

        public V find(K searchKey) {
            switch (getDirection(searchKey)) {
                case spotOn:
                    return readValue();
                case goLeft:
                    return readLeft() == null ? null : left.find(searchKey);
                case goRight:
                    return readRight() == null ? null : right.find(searchKey);
                default:
                    throw new RuntimeException();
            }
        }

        private Direction getDirection(K searchKey) {
            int compare = key.compareTo(searchKey);

            if (compare == 0)
                return Direction.spotOn;
            else if (compare > 1)
                return Direction.goLeft;
            else
                return Direction.goRight;
        }

        public int height() {
            return 1 + max(height(readLeft()), height(readRight()));
        }

        private int height(Node node) {
            return node == null ? 0 : node.height();
        }

        // ================== generated ==================

        private final Originator<Node<K, V>> originator;
        private DematerializedNode<K, V> lastDematerialized;

        public Node(DematerializedNode<K, V> dematerializedNode, Transaction t) {
            this.lastDematerialized = dematerializedNode;
            this.originator = dematerializedNode.originator;
            this.leftRef = t.readLazy(dematerializedNode.left);
            this.rightRef = t.readLazy(dematerializedNode.right);

            if (dematerializedNode.key instanceof Originator) {
                key = t.read((Originator<K>) dematerializedNode.key);
            } else {
                key = (K) dematerializedNode.key;
            }

            if (dematerializedNode.value instanceof Originator) {
                valueRef = t.readLazy((Originator<V>) dematerializedNode.value);
            } else {
                value = (V) dematerializedNode.value;
            }
        }

        @Override
        public Originator<Node<K, V>> getOriginator() {
            return originator;
        }

        @Override
        public boolean isDirty() {
            if (lastDematerialized == null)
                return true;

            if (lastDematerialized.left != MultiversionedStmUtils.getValueOrOriginator(leftRef, left))
                return true;

            if (lastDematerialized.right != MultiversionedStmUtils.getValueOrOriginator(rightRef, right))
                return true;

            if (lastDematerialized.value != MultiversionedStmUtils.getOriginator(valueRef, value))
                return true;


            if (lastDematerialized.key != MultiversionedStmUtils.getOriginator(key))
                return true;

            return false;
        }

        private MaterializedObject nextInChain;

        @Override
        public MaterializedObject getNextInChain() {
            return nextInChain;
        }

        @Override
        public void setNextInChain(MaterializedObject next) {
            this.nextInChain = next;
        }

        @Override
        public DematerializedNode<K, V> dematerialize() {
            return lastDematerialized = new DematerializedNode<K, V>(this);
        }

        @Override
        public void memberTrace(MemberTracer memberTracer) {
            if (left != null) memberTracer.onMember(left);
            if (right != null) memberTracer.onMember(right);
            if (key instanceof MaterializedObject) memberTracer.onMember((MaterializedObject) key);
            if (value instanceof MaterializedObject) memberTracer.onMember((MaterializedObject) value);
        }
    }

    //================ generated ================================

    private final Originator<BTree<K, V>> originator;
    private DematerializedBTree<K, V> lastDematerialized;

    private BTree(DematerializedBTree<K, V> dematerializedBTree, Transaction t) {
        originator = dematerializedBTree.originator;
        lastDematerialized = dematerializedBTree;
        root = t.read(dematerializedBTree.root);
    }

    @Override
    public Originator<BTree<K, V>> getOriginator() {
        return originator;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (lastDematerialized.root != MultiversionedStmUtils.getOriginator(root))
            return true;

        return false;
    }

    @Override
    public DematerializedObject dematerialize() {
        return lastDematerialized = new DematerializedBTree<K, V>(this);
    }

    @Override
    public void memberTrace(MemberTracer memberTracer) {
        if (root != null) memberTracer.onMember(root);
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        this.nextInChain = next;
    }


    public static class DematerializedBTree<K extends Comparable, V> implements DematerializedObject {
        final Originator<BTree<K, V>> originator;
        final Originator<Node<K, V>> root;

        public DematerializedBTree(BTree<K, V> bTree) {
            this.originator = bTree.originator;
            this.root = bTree.root == null ? null : bTree.root.getOriginator();
        }

        @Override
        public Originator<BTree<K, V>> getOriginator() {
            return originator;
        }

        @Override
        public BTree<K, V> rematerialize(Transaction t) {
            return new BTree<K, V>(this, t);
        }
    }

    public static class DematerializedNode<K extends Comparable, V> implements DematerializedObject {
        private final Originator<Node<K, V>> originator;
        private final Originator<Node<K, V>> left;
        private final Originator<Node<K, V>> right;
        private final Object key;
        private final Object value;

        public DematerializedNode(Node<K, V> node) {
            this.originator = node.getOriginator();
            this.left = MultiversionedStmUtils.getOriginator(node.leftRef, node.left);
            this.right = MultiversionedStmUtils.getOriginator(node.rightRef, node.right);
            this.key = MultiversionedStmUtils.getValueOrOriginator(null, node.key);
            this.value = MultiversionedStmUtils.getValueOrOriginator(node.valueRef, node.value);
        }

        @Override
        public Originator<Node<K, V>> getOriginator() {
            return originator;
        }

        @Override
        public Node<K, V> rematerialize(Transaction t) {
            return new Node<K, V>(this, t);
        }
    }
}
