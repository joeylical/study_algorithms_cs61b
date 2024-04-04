package gh2;

import deque.Deque;

public class Drums extends GuitarString {
    public Drums(double frequency) {
        super(frequency);
    }

    public void tic() {
        Deque<Double> buffer = super.getBuffer();
        double t0 = buffer.removeFirst();
        double t1 = buffer.get(0);
        if (Math.random() >= 0.5) {
            buffer.addLast(0 - 1 * 0.5 * (t0 + t1));
        } else {
            buffer.addLast(1 * 0.5 * (t0 + t1));
        }
    }
}
