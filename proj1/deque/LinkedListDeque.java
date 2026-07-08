package deque;

public class LinkedListDeque<T> {
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
    // 长度查询与判空
    public int size() {
        return this.size;
    }
    public  boolean isEmpty() {
        return this.size == 0;
    }
    // 首尾节点插入
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
}
