package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> comp;
    MaxArrayDeque(Comparator<T> c) {
        comp = c;
    }

    public T max() {
        if (first == last) {
            return null;
        }

        T maxValue = buffer[first];
        for (var i = first + 1; i < last; i++) {
            if (comp.compare(buffer[i], maxValue) > 0) {
                maxValue = buffer[i];
            }
        }

        return maxValue;
    }

    T max(Comparator<T> c) {
        if (first == last) {
            return null;
        }

        T maxValue = buffer[first];
        for(var i = first + 1; i < last; i++) {
            if (c.compare(buffer[i], maxValue) > 0) {
                maxValue = buffer[i];
            }
        }

        return maxValue;
    }
}
