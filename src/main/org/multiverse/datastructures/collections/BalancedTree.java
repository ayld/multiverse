package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.utils.TodoException;

import static java.lang.String.format;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * @author Peter Veentjer
 */
public class BalancedTree<K extends Comparable, V> {
    private int size;
    private TreeNode<K, V> root;

    public BalancedTree() {
        this.size = 0;
    }

    public V await(K key) {
        V value = get(key);
        if (value == null) {
            retry();
        }
        return value;
    }

    public V awaitAndTake(K key) {
        V value = remove(key);

        if (value == null) {
            retry();
        }

        return value;
    }

    public V remove(K key) {
        if (key == null) {
            throw new NullPointerException();
        }

        throw new TodoException();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int height() {
        return root == null ? 0 : root.height();
    }

    public V put(K key, V value) {
        if (root == null) {
            root = new TreeNode<K, V>(key, value);
            size++;
            return null;
        } else {
            V replacedValue = root.put(key, value);
            if (replacedValue == null) {
                size++;
            }
            return replacedValue;
        }
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    public V get(K key) {
        if (root == null) {
            return null;
        }

        return root.find(key);
    }

    public Iterator<K> keys() {
        return new KeyIterator<K, V>(root);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        throw new TodoException();
    }

    static class KeyIterator<K extends Comparable, V> implements Iterator<K> {
        private final Stack<TreeNode<K, V>> leftStack = new Stack<TreeNode<K, V>>();
        private final Stack<TreeNode<K, V>> rightStack = new Stack<TreeNode<K, V>>();

        private final Ref<TreeNode<K, V>> currentRef = new Ref<TreeNode<K, V>>();

        public KeyIterator(TreeNode root) {
            currentRef.set(root);

            if (root != null) {
                if (root.getLeft() != null) {
                    leftStack.push(root.getLeft());
                }

                if (root.getRight() != null) {
                    rightStack.push(root.getRight());
                }
            }
        }

        @Override
        public boolean hasNext() {
            if (!currentRef.isNull()) {
                return true;
            }

            if (!leftStack.isEmpty()) {
                TreeNode<K, V> node = leftStack.pop();
                currentRef.set(node);
                if (node.getLeft() != null) {
                    leftStack.push(node.getLeft());
                }

                if (node.getRight() != null) {
                    rightStack.push(node.getRight());
                }
                return true;
            } else if (!rightStack.isEmpty()) {
                TreeNode<K, V> node = rightStack.pop();
                currentRef.set(node);

                if (node.getLeft() != null) {
                    leftStack.push(node.getLeft());
                }

                if (node.getRight() != null) {
                    rightStack.push(node.getRight());
                }
                return true;
            } else {
                return false;
            }
        }

        @Override
        public K next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return currentRef.clear().getKey();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @AtomicObject
    static class TreeNode<K extends Comparable, V> {
        K key;
        V value;
        TreeNode left;
        TreeNode right;
        int height;

        public TreeNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.height = 1;
        }

        public int height() {
            return height;
        }

        public int getBalanceFactor() {
            int leftHeight = left == null ? 0 : left.height();
            int rightHeight = right == null ? 0 : right.height();
            return rightHeight - leftHeight;
        }

        public V find(K key) {
            TreeNode<K, V> node = this;
            do {
                int compare = key.compareTo(node.getKey());
                if (compare == 0) {
                    return node.getValue();
                } else if (compare > 0) {
                    node = node.getLeft();
                } else {
                    node = node.getRight();
                }
            } while (node != null);

            return null;
        }

        public V put(K key, V newValue) {
            TreeNode<K, V> node = this;
            do {
                int compare = key.compareTo(node.getKey());
                if (compare == 0) {
                    return node.updateValue(newValue);
                } else if (compare > 0) {
                    if (node.getLeft() == null) {
                        TreeNode<K, V> newLeft = new TreeNode(key, newValue);
                        node.setLeft(newLeft);
                        return null;
                    } else {
                        node = node.getLeft();
                    }
                } else {
                    if (node.getRight() == null) {
                        TreeNode<K, V> newRight = new TreeNode(key, newValue);
                        node.setRight(newRight);
                        return null;
                    } else {
                        node = node.getRight();
                    }
                    node = node.getRight();
                }
            } while (node != null);

            return null;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public TreeNode<K, V> getLeft() {
            return left;
        }

        public void setLeft(TreeNode<K, V> left) {
            this.left = left;
        }

        public TreeNode<K, V> getRight() {
            return right;
        }

        public void setRight(TreeNode<K, V> right) {
            this.right = right;
        }

        public V updateValue(V newValue) {
            V oldValue = value;
            this.value = newValue;
            return oldValue;
        }

        public TreeNode<K, V> singleLeftRotate() {
            throw new TodoException();
        }

        public TreeNode<K, V> doubleLeftRotate() {
            throw new TodoException();
        }

        public TreeNode<K, V> singleRightRotate() {
            throw new TodoException();
        }

        public TreeNode<K, V> doubleRightRotate() {
            throw new TodoException();
        }

        public String toString() {
            return format("(key=%s value=%s, left=%s right=%s)", key, value, left, right);
        }
    }
}
