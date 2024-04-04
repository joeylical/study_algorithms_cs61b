package gh2;

import deque.Deque;

public class Harp extends GuitarString {
    public Harp (double frequency) {
        super(frequency);
    }

    public void tic() {
        Deque<Double> buffer = super.getBuffer();
        double t0 = buffer.removeFirst();
        double t1 = buffer.get(0);
        buffer.addLast(0 - super.getDecay() * 0.5 * (t0 + t1));
    }
}
