package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private final int initSize = 8;
    private T[] buffer;
    private int first;
    private int last;
    public ArrayDeque() {
        buffer = (T[]) new Object[initSize];
        first = initSize / 2;
        last = initSize / 2;
    }

    private void expand() {
        T[] newArray = (T[]) new Object[buffer.length * 4];
        final int newFirst = (buffer.length * 4 - (last - first)) / 2;
        final int newLast = newFirst + (last - first);
        System.arraycopy(buffer, first, newArray, newFirst, last - first);
        buffer = newArray;
        first = newFirst;
        last = newLast;
    }

    public void addFirst(T item) {
        if (first == 0) {
            expand();
        }

        first -= 1;
        buffer[first] = item;
    }

    public void addLast(T item) {
        if (last == buffer.length - 1) {
            expand();
        }

        buffer[last] = item;
        last += 1;
    }

    public int size() {
        return last - first;
    }

    public void printDeque() {
        for (int i = first; i < last; i++) {
            System.out.print(buffer[i]);
            System.out.print(' ');
        }
        System.out.println();
    }

    private void shrink() {
        T[] newArray = (T[]) new Object[buffer.length / 4];
        final int newFirst = (buffer.length / 4 - (last - first)) / 2;
        final int newLast = newFirst + (last - first);
        System.arraycopy(buffer, first, newArray, newFirst, last - first);
        buffer = newArray;
        first = newFirst;
        last = newLast;
    }

    public T removeFirst() {
        if (first == last) {
            return null;
        }
        T cur = buffer[first];
        first += 1;

        if (size() < buffer.length / 4) {
            shrink();
        }

        return cur;
    }

    public T removeLast() {
        if (first == last) {
            return null;
        }
        T cur = buffer[last - 1];
        last -= 1;

        if (size() < buffer.length / 4) {
            shrink();
        }

        return cur;
    }

    public T get(int index) {
        if (index >= last - first) {
            return null;
        }

        return buffer[first + index];
    }

    private class ArrayDequeIterator<T> implements Iterator<T> {
        private int cur;
        ArrayDequeIterator(ArrayDeque obj) {
            cur = first;
        }

        public boolean hasNext() {
            return last - cur > 0;
        }

        public T next() {
            final T v = (T) buffer[cur];
            cur++;
            return v;
        }
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator<T>(this);
    }

    public boolean equals(Object o) {
        if (o instanceof ArrayDeque) {
            ArrayDeque o1 = (ArrayDeque) o;
            if (o1.size() != size()) {
                return false;
            }
            Iterator i1 = o1.iterator();
            Iterator i2 = iterator();

            while (i1.hasNext()) {
                if (i1.next() != i2.next()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
