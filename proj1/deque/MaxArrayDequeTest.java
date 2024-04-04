package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;


/** Performs some basic linked list tests. */
public class MaxArrayDequeTest {

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void maxTest() {
        MaxArrayDeque<Integer> lld1 = new MaxArrayDeque<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });

        for (int i = 0; i < 1000; i++) {
            lld1.addLast(i);
        }
        assertEquals("Should have the same value", 999, (double) lld1.max(), 0.0);
    }
}
