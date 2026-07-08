package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class Node {
        T item;
        Node prev;
        Node next;

        private Node(T item, Node prev, Node next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    private Node sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void addFirst(T item) {
        Node oldFirst = sentinel.next;
        Node newFirst = new Node(item, sentinel, oldFirst);
        sentinel.next = newFirst;
        oldFirst.prev = newFirst;
        size += 1;
    }

    @Override
    public void addLast(T item) {
        Node oldLast = sentinel.prev;
        Node newLast = new Node(item, oldLast, sentinel);
        oldLast.next = newLast;
        sentinel.prev = newLast;
        size += 1;
    }

    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        Node oldFirst = sentinel.next;
        Node newFirst = oldFirst.next;
        T item = oldFirst.item;
        sentinel.next = newFirst;
        newFirst.prev = sentinel;
        size -= 1;
        return item;
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        Node oldLast = sentinel.prev;
        Node newLast = oldLast.prev;
        T item = oldLast.item;
        sentinel.prev = newLast;
        newLast.next = sentinel;
        size -= 1;
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }

    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        return helper(sentinel.next, index);
    }

    private T helper(Node current, int index) {
        if (index == 0) {
            return current.item;
        }
        return helper(current.next, index - 1);
    }

    @Override
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        int pos = 0;

        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            T item = get(pos);
            pos++;
            return item;
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }
}
