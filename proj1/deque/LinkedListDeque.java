package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    // 节点类
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
    // 定义构造函数
    private Node sentinel;
    private int size;
    public LinkedListDeque() {
        sentinel = new Node(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        size = 0;
    }
    // 长度查询
    public int size() {
        return this.size;
    }
    public void addFirst(T item) {
        Node oldFirst = sentinel.next;
        Node newFirst = new Node(item, sentinel, oldFirst);
        sentinel.next = newFirst;
        oldFirst.prev = newFirst;
        size += 1;
    }
    public void addLast(T item) {
        Node oldLast = sentinel.prev;
        Node newLast = new Node(item, oldLast, sentinel);
        oldLast.next = newLast;
        sentinel.prev = newLast;
        size += 1;
    }
    // 首尾节点插删除
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
    // 根据索引取值
    public T get(int index){
        if (index < 0 || index >= size) {
            return null;
        }
        Node current = sentinel.next;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.item;
    }
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }
    private class LinkedListDequeIterator implements Iterator<T> {
        int pos = 0;
        public boolean hasNext() {
            return pos < size;
        }
        public T next() {
            T item = get(pos);
            pos++;
            return item;
        }
    }
    public Iterator<T> iterator() {
        return new LinkedListDeque.LinkedListDequeIterator();
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
