package gh2;

public class Harp extends GuitarString {
    public Harp (double frequency) {
        super(frequency);
    }

    public void tic() {
        double t0 = buffer.removeFirst();
        double t1 = buffer.get(0);
        buffer.addLast(0 - DECAY * 0.5 * (t0 + t1));
    }
}
