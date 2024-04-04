package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    Comparator<T> comp;
    public MaxArrayDeque(Comparator<T> c) {
        comp = c;
    }

    public T max() {
        if (first == last) {
            return null;
        }

        T max_value = buffer[first];
        for(var i = first + 1; i < last;i++) {
            if (comp.compare(buffer[i], max_value) > 0) {
                max_value = buffer[i];
            }
        }

        return max_value;
    }

    public T max(Comparator<T> c) {
        if (first == last) {
            return null;
        }

        T max_value = buffer[first];
        for(var i = first + 1; i < last;i++) {
            if (c.compare(buffer[i], max_value) > 0) {
                max_value = buffer[i];
            }
        }

        return max_value;
    }
}
