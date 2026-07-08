package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private T[] items;
    private int size;
    private int nextFirst;
    private int nextLast;
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }
    private int minusOne(int index) {
        if (index==0){
            return items.length - 1;
        }
        return index - 1;
    }
    private int plusOne(int index) {
        return (index + 1) % items.length;
    }
    public int size() {
        return this.size;
    }
    public void addFirst(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = minusOne(nextFirst);
        size += 1;
    }
    public void addLast(T item) {
        if (size == items.length) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = plusOne(nextLast);
        size += 1;
    }
    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        nextFirst = plusOne(nextFirst);
        T item = items[nextFirst];
        items[nextFirst] = null;
        size -= 1;
        if (items.length >= 16 && size * 4 < items.length) {
            resize(items.length / 2);
        }
        return item;
    }
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        nextLast = minusOne(nextLast);
        T item = items[nextLast];
        items[nextLast] = null;
        size -= 1;
        if (items.length >= 16 && size * 4 < items.length) {
            resize(items.length / 2);
        }
        return item;
    }
    public T get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        int firstIndex = plusOne(nextFirst);
        int realIndex = (firstIndex + index) % items.length;
        return items[realIndex];
    }
    private void resize(int capacity) {
        T[] newItems = (T[]) new Object[capacity];
        for (int i = 0; i < size; i++) {
            T item = get(i);
            newItems[i] = item;
        }
        nextFirst = capacity - 1;
        nextLast = size;
        items = newItems;
    }
    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(get(i) + " ");
        }
        System.out.println();
    }
    private class ArrayDequeIterator implements Iterator<T> {
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
        return new ArrayDequeIterator();
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
