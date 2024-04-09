package flik;

import static flik.Flik.isSameNumber;
import static org.junit.Assert.*;
import org.junit.Test;

public class FlikTest {
    @Test
    public void testIsSameNumber() {
        Integer a = 0, b = 0;
        for (int i = 0; i < 1000; i++) {
            Integer temp = (int) (Math.random() * 100) - 50;
            a += temp;
            b += temp;

            assertTrue(isSameNumber(a, a));
            assertTrue(isSameNumber(a, b));
        }
    }

    @Test
    public void testGeneral() {
        assertTrue(isSameNumber(1, 1));
        assertFalse(isSameNumber(1, 2));
        assertFalse(isSameNumber(1, null));
        assertFalse(isSameNumber(null, 1));
    }
}
