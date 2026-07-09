package bstmap;

import java.security.Key;
import java.util.Set;
import java.util.Iterator;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private class BSTNode {
        K key;
        V value;
        BSTNode left;
        BSTNode right;
        BSTNode (K key, V value) {
            this.key = key;
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private BSTNode root;
    private int size;

    public BSTMap() {
        root = null;
        size = 0;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public boolean containsKey(K key) {
        return containsHelper(root, key);
    }

    private boolean containsHelper(BSTNode node, K key) {
        if (node == null) {
            return false;
        }
        if (key.compareTo(node.key) > 0) {
            return containsHelper(node.right, key);
        } else if (key.compareTo(node.key) < 0) {
            return containsHelper(node.left, key);
        } else {
            return true;
        }
    }

    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(BSTNode node, K key) {
        if (node == null) {
            return null;
        }
        if (key.compareTo(node.key) > 0) {
            return getHelper(node.right, key);
        } else if (key.compareTo(node.key) < 0) {
            return getHelper(node.left, key);
        } else {
            return node.value;
        }
    }

    public int size() {
        return size;
    }

    public void put(K key, V value) {
        root = putHelper(root, key, value);
    }

    private BSTNode putHelper(BSTNode node, K key, V value) {
        if (node == null) {
            size++;
            return new BSTNode(key, value);
        }
        if (key.compareTo(node.key) > 0) {
            node.right = putHelper(node.right, key, value);
        } else if (key.compareTo(node.key) < 0) {
            node.left = putHelper(node.left, key, value);
        } else {
            node.value = value;
        }
        return node;
    }

    public void printInOrder() {
        printHelper(root);
    }

    private void printHelper(BSTNode node) {
        if (node == null) {
            return;
        }
        if (node.left != null) {
            printHelper(node.left);
        }
        System.out.println(node.key);
        if (node.right != null) {
            printHelper(node.right);
        }
    }

    public Set<K> keySet() {
        throw new UnsupportedOperationException();
    }

    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}