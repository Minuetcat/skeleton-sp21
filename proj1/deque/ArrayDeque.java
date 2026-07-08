package deque;

import java.lang.module.FindException;

public class ArrayDeque<T> {
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
    public  boolean isEmpty() {
        return this.size == 0;
    }
    public void addFirst(T item) {
        items[nextFirst] = item;
        nextFirst = minusOne(nextFirst);
        size += 1;
    }
    public void addLast(T item) {
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
}
