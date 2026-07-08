package deque;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {
    @Test
    public void emptyArrayDequeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        assertEquals(0, ad.size());
        assertTrue(ad.isEmpty());
        assertNull(ad.removeFirst());
        assertNull(ad.removeLast());
        assertNull(ad.get(0));
        assertNull(ad.get(-1));
    }
    @Test
    public void addAndGetTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        ad.addFirst(10);
        ad.addLast(20);
        ad.addFirst(5);

        assertEquals(3, ad.size());
        assertFalse(ad.isEmpty());

        assertEquals(Integer.valueOf(5), ad.get(0));
        assertEquals(Integer.valueOf(10), ad.get(1));
        assertEquals(Integer.valueOf(20), ad.get(2));

        assertNull(ad.get(3));
        assertNull(ad.get(-1));
    }

    @Test
    public void removeTest() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();

        ad.addFirst(10);
        ad.addLast(20);
        ad.addFirst(5);

        assertEquals(Integer.valueOf(5), ad.removeFirst());
        assertEquals(Integer.valueOf(20), ad.removeLast());
        assertEquals(Integer.valueOf(10), ad.removeFirst());

        assertEquals(0, ad.size());
        assertTrue(ad.isEmpty());

        assertNull(ad.removeFirst());
        assertNull(ad.removeLast());
    }
}
