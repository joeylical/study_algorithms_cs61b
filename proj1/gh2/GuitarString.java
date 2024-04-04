package gh2;

import deque.ArrayDeque;
import deque.Deque;

//Note: This file will not compile until you complete the Deque implementations
public class GuitarString {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public GuitarString(double frequency) {
        buffer = new ArrayDeque<>();

        final int len = (int) Math.round(SR / frequency);

        for (int i = 0; i < len; i++) {
            buffer.addFirst(0.0);
        }
    }


    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        final int len = buffer.size();

        for (int i = 0; i < len; i++) {
            buffer.removeLast();
            buffer.addFirst(Math.random() - 0.5);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double t0 = buffer.removeFirst();
        double t1 = buffer.get(0);
        buffer.addLast(DECAY * 0.5 * (t0 + t1));
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        return buffer.get(0);
    }

    public Deque<Double> getBuffer() {
        return buffer;
    }

    public double getDecay() { return DECAY; }
}
