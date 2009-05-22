package org.multiverse.collections;

import org.multiverse.api.annotations.Eager;
import org.multiverse.api.annotations.SelfManaged;
import org.multiverse.api.annotations.TmEntity;

import static java.lang.Math.max;

@TmEntity
public final class BTree<K extends Comparable, V> {

    public enum BTreeDirection {
        goLeft, goRight, spotOn
    }

    @SelfManaged
    @Eager
    private BTreeNode<K, V> root;

    public V put(K key, V value) {
        if (key == null)
            throw new NullPointerException();

        if (root == null) {
            root = new BTreeNode<K, V>(key, value);
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
        if (key == null) {
            throw new NullPointerException();
        }

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


    @TmEntity
    private static final class BTreeNode<K extends Comparable, V> {

        private K key;
        private V value;
        @SelfManaged
        private BTreeNode<K, V> left;
        @SelfManaged
        private BTreeNode<K, V> right;

        BTreeNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public V put(K newKey, V newValue) {
            switch (getDirection(newKey)) {
                case spotOn: {
                    V oldValue = value;
                    value = newValue;
                    return oldValue;
                }
                case goLeft: {
                    if (left == null) {
                        left = new BTreeNode<K, V>(newKey, newValue);
                        return null;
                    } else {
                        return left.put(newKey, newValue);
                    }
                }
                case goRight: {
                    if (right == null) {
                        right = new BTreeNode<K, V>(newKey, newValue);
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
            return 1 + size(left) + size(right);
        }

        private int size(BTreeNode node) {
            return node == null ? 0 : node.size();
        }

        public V find(K searchKey) {
            switch (getDirection(searchKey)) {
                case spotOn:
                    return value;
                case goLeft:
                    return left == null ? null : left.find(searchKey);
                case goRight:
                    return right == null ? null : right.find(searchKey);
                default:
                    throw new RuntimeException();
            }
        }

        private BTreeDirection getDirection(K searchKey) {
            int compare = key.compareTo(searchKey);

            if (compare == 0)
                return BTreeDirection.spotOn;
            else if (compare > 1)
                return BTreeDirection.goLeft;
            else
                return BTreeDirection.goRight;
        }

        public int height() {
            return 1 + max(height(left), height(right));
        }

        private int height(BTreeNode node) {
            return node == null ? 0 : node.height();
        }
    }
}
