package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeAList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeAListConstruction();
    }

    public static void timeAListConstruction() {
        // TODO: YOUR CODE HERE
        final int[] ns = new int[] {1000, 2000, 4000, 8000, 16000, 32000, 64000, 128000};
        final AList<Integer> Ns = new AList<>();
        final AList<Double> times = new AList<>();
        final AList<Integer> opCounts = new AList<>();

        for(var i=0;i<ns.length;i++) {
            final AList<Integer> l = new AList<>();
            Stopwatch sw = new Stopwatch();
            for(var j=0;j<ns[i];j++) {
                l.addLast(j);
            }
            final double time = sw.elapsedTime();
            Ns.addLast(ns[i]);
            times.addLast(time);
            opCounts.addLast(ns[i]);
        }
        printTimingTable(Ns, times, opCounts);
    }
}
