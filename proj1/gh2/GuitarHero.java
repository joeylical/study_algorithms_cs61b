package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;


public class GuitarHero {
    public static final double CONCERT_A = 440.0;
//    public static final String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
    public static final String KEYBOARD = "1234567qwertyuasdfghjzxcvbnm";

    private static Harp[] strings;

    public static void main(String[] args) {
        // equal temperament
        // strings = new Harp[keyboard.length()];
        // for (int i=0; i < keyboard.length(); i++) {
        //      final double freq = 440 *  Math.pow(2, (i - 24) / 12.0);
        //      strings[i] = new Harp(freq);
        // }

        // Pythagorean tuning
        // from wikipedia: https://en.wikipedia.org/wiki/Pythagorean_tuning
        final double[] freqs = {1, 9 / 8.0, 81 / 64.0, 4 / 3.0, 3 / 2.0, 27 / 16.0, 243 / 128.0};
        strings = new Harp[KEYBOARD.length()];
        for (int i = 0; i < KEYBOARD.length(); i++) {
            final double freq = 110 * Math.pow(2, i / freqs.length) * freqs[i % freqs.length];
            strings[i] = new Harp(freq);
        }

        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                final int chor = KEYBOARD.indexOf(key);
                if (chor >= 0) {
                    strings[chor].pluck();
                }
            }

            /* compute the superposition of samples */
            double sample = 0.0;
            for (int i = 0; i < strings.length; i++) {
                sample += strings[i].sample();
            }

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            for (int i = 0; i < strings.length; i++) {
                strings[i].tic();
            }
        }
    }
}
