package tester;

import static org.junit.Assert.*;
import org.junit.Test;
import student.Deque;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    private boolean eq(StudentArrayDeque<Integer> a, ArrayDequeSolution<Integer> b) {
        if (a.size() != b.size()) {
            return false;
        }

        for (int i=0; i < a.size(); i++) {
            assertNotNull(String.format("student.get(%d) should not be Null", i), a.get(i));
            assertNotNull(String.format("array.get(%d) should not be Null", i), b.get(i));
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Test
    public void eqLLDequeTest() {
        StudentArrayDeque<Integer> lld1 = new StudentArrayDeque<Integer>();
        ArrayDequeSolution<Integer> lld2 = new ArrayDequeSolution<Integer>();
        String lastOp = "creating new objects";
        assertTrue(lastOp, eq(lld1, lld2));
        for (int i = 0; i < 10000; i++) {
            final int temp = (int)Math.floor(Math.random() * 65536);
            final int op = (int) Math.floor(Math.random() * 4);
            switch (op) {
                case 0:
                    lld1.addLast(temp);
                    lld2.addLast(temp);
                    lastOp = String.format("addLast(%d)", temp);
                    assertTrue(lastOp, eq(lld1, lld2));
                    break;
                case 1:
                    lld1.addFirst(temp);
                    lld2.addFirst(temp);
                    lastOp = String.format("addFirst(%d)", temp);
                    assertTrue(lastOp, eq(lld1, lld2));
                    break;
                case 2:
                    if (lld1.size() * lld2.size() == 0) {
                        continue;
                    }
                    int a = lld1.removeLast();
                    int b = lld2.removeLast();
                    lastOp = String.format("removeLast(), student was %d, correct was %d", a, b);
                    assertEquals(lastOp, a, b);
                    assertTrue(lastOp, eq(lld1, lld2));
                    break;
                case 3:
                    if (lld1.size() * lld2.size() == 0) {
                        continue;
                    }
                    a = lld1.removeFirst();
                    b = lld2.removeFirst();
                    lastOp = String.format("removeFirst(), student was %d, correct was %d", a, b);
                    assertEquals(lastOp, a, b);
                    assertTrue(lastOp, eq(lld1, lld2));
                    break;
            }
        }

        assertTrue("Should have the same value after: " + lastOp, eq(lld1, lld2));
    }

}
