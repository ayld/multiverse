package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.Math.max;

public final class ExampleBTree<K extends Comparable, V> implements MaterializedObject {

    private Node<K, V> root;

    public ExampleBTree() {
        this.handle = new DefaultMultiversionedHandle<ExampleBTree<K, V>>();
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

        if (!(thatObj instanceof ExampleBTree))
            return false;

        ExampleBTree<K, V> that = (ExampleBTree<K, V>) thatObj;
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
            this.handle = new DefaultMultiversionedHandle<Node<K, V>>();
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

        private final MultiversionedHandle<Node<K, V>> handle;
        private DematerializedNode<K, V> lastDematerialized;

        public Node(DematerializedNode<K, V> dematerializedNode, Transaction t) {
            this.lastDematerialized = dematerializedNode;
            this.handle = dematerializedNode.handle;
            this.leftRef = t.readLazy(dematerializedNode.left);
            this.rightRef = t.readLazy(dematerializedNode.right);

            if (dematerializedNode.key instanceof Handle) {
                key = t.read((Handle<K>) dematerializedNode.key);
            } else {
                key = (K) dematerializedNode.key;
            }

            if (dematerializedNode.value instanceof Handle) {
                valueRef = t.readLazy((Handle<V>) dematerializedNode.value);
            } else {
                value = (V) dematerializedNode.value;
            }
        }

        @Override
        public MultiversionedHandle<Node<K, V>> getHandle() {
            return handle;
        }

        @Override
        public boolean isDirty() {
            if (lastDematerialized == null)
                return true;

            if (lastDematerialized.left != MultiversionedStmUtils.getValueOrHandle(leftRef, left))
                return true;

            if (lastDematerialized.right != MultiversionedStmUtils.getValueOrHandle(rightRef, right))
                return true;

            if (lastDematerialized.value != MultiversionedStmUtils.getHandle(valueRef, value))
                return true;

            if (lastDematerialized.key != MultiversionedStmUtils.getHandle(key))
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
        public void walkMaterializedMembers(MemberWalker memberWalker) {
            if (left != null) memberWalker.onMember(left);
            if (right != null) memberWalker.onMember(right);
            if (key instanceof MaterializedObject) memberWalker.onMember((MaterializedObject) key);
            if (value instanceof MaterializedObject) memberWalker.onMember((MaterializedObject) value);
        }
    }

    //================ generated ================================

    private final MultiversionedHandle<ExampleBTree<K, V>> handle;
    private DematerializedBTree<K, V> lastDematerialized;

    private ExampleBTree(DematerializedBTree<K, V> dematerializedBTree, Transaction t) {
        handle = dematerializedBTree.handle;
        lastDematerialized = dematerializedBTree;
        root = t.read(dematerializedBTree.root);
    }

    @Override
    public MultiversionedHandle<ExampleBTree<K, V>> getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (lastDematerialized.root != MultiversionedStmUtils.getHandle(root))
            return true;

        return false;
    }

    @Override
    public DematerializedObject dematerialize() {
        return lastDematerialized = new DematerializedBTree<K, V>(this);
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        if (root != null) memberWalker.onMember(root);
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
        final MultiversionedHandle<ExampleBTree<K, V>> handle;
        final MultiversionedHandle<Node<K, V>> root;

        public DematerializedBTree(ExampleBTree<K, V> bTree) {
            this.handle = bTree.handle;
            this.root = bTree.root == null ? null : bTree.root.getHandle();
        }

        @Override
        public MultiversionedHandle<ExampleBTree<K, V>> getHandle() {
            return handle;
        }

        @Override
        public ExampleBTree<K, V> rematerialize(Transaction t) {
            return new ExampleBTree<K, V>(this, t);
        }
    }

    public static class DematerializedNode<K extends Comparable, V> implements DematerializedObject {
        private final MultiversionedHandle<Node<K, V>> handle;
        private final MultiversionedHandle<Node<K, V>> left;
        private final MultiversionedHandle<Node<K, V>> right;
        private final Object key;
        private final Object value;

        public DematerializedNode(Node<K, V> node) {
            this.handle = node.getHandle();
            this.left = MultiversionedStmUtils.getHandle(node.leftRef, node.left);
            this.right = MultiversionedStmUtils.getHandle(node.rightRef, node.right);
            this.key = MultiversionedStmUtils.getValueOrHandle(null, node.key);
            this.value = MultiversionedStmUtils.getValueOrHandle(node.valueRef, node.value);
        }

        @Override
        public MultiversionedHandle<Node<K, V>> getHandle() {
            return handle;
        }

        @Override
        public Node<K, V> rematerialize(Transaction t) {
            return new Node<K, V>(this, t);
        }
    }
}
