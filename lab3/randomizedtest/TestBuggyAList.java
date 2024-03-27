package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    void testList(AListNoResizing<Integer> a, BuggyAList<Integer> b) {
        assertEquals(a.size(), b.size());
        for(var i=0;i<a.size();i++) {
            assertEquals(a.get(i), b.get(i));
        }
    }

//    @Test
//    public void randomizedTest() {
//        AListNoResizing<Integer> L = new AListNoResizing<>();
//
//        int N = 500;
//        for (int i = 0; i < N; i += 1) {
//            int operationNumber = StdRandom.uniform(0, 2);
//            if (operationNumber == 0) {
//                // addLast
//                int randVal = StdRandom.uniform(0, 100);
//                L.addLast(randVal);
//                System.out.println("addLast(" + randVal + ")");
//            } else if (operationNumber == 1) {
//                // size
//                int size = L.size();
//                System.out.println("size: " + size);
//            }
//        }
//    }
    @Test
    public void test() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> l = new BuggyAList();
        testList(L, l);
        for(var i=0;i<500;i++) {
            int op = StdRandom.uniform(0, 2);
            int x = StdRandom.uniform(0, 100);
            if ( op == 0) {
                //add
                x = Integer.min(x, 1000 - L.size());
                for(var j=0;j<x;j++) {
                    int n = StdRandom.uniform(-1000, 1000);
                    L.addLast(n);
                    l.addLast(n);
                }
            } else {
                //delete
                x = Integer.min(x, L.size());
                for(var j=0;j<x;j++) {
                    L.removeLast();
                    l.removeLast();
                }
            }
            testList(L, l);
        }
    }
}
