package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> comp;
    public MaxArrayDeque(Comparator<T> c) {
        comp = c;
    }

    public T max() {
        return max(comp);
    }

    public T max(Comparator<T> c) {
        if (size() == 0) {
            return null;
        }

        T maxValue = get(0);
        for (var i = 1; i < size(); i++) {
            if (c.compare(get(i), maxValue) > 0) {
                maxValue = get(i);
            }
        }

        return maxValue;
    }
}
