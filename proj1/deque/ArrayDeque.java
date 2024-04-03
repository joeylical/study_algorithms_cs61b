package deque;

import java.util.Iterator;

public class ArrayDeque<T> {
    final int init_size = 8;
    T[] buffer;
    int first;
    int last;
    public ArrayDeque() {
        buffer = (T[]) new Object[init_size];
        first = init_size / 2;
        last = init_size / 2;
    }

    private void expand() {
        T[] new_array = (T[]) new Object[buffer.length * 4];
        final int new_first = (buffer.length * 4 - (last - first)) / 2;
        final int new_last = new_first + (last - first);
        System.arraycopy(buffer, first, new_array, new_first, last - first);
        buffer = new_array;
        first = new_first;
        last = new_last;
    }

    public void addFirst(T item) {
        if (last == buffer.length - 1) {
            expand();
        }

        buffer[last] = item;
        last += 1;
    }

    public void addLast(T item) {
        if(first == 0) {
            expand();
        }

        buffer[first] = item;
        first -= 1;
    }

    public boolean isEmpty() {
        return (last - first) == 0;
    }

    public int size() {
        return last - first;
    }

    public void printDeque() {
        for(int i=first;i<last;i++) {
            System.out.print(buffer[i]);
            System.out.print(' ');
        }
        System.out.println();
    }

    private void shrink() {
        T[] new_array = (T[]) new Object[buffer.length / 4];
        final int new_first = (buffer.length / 4 - (last - first)) / 2;
        final int new_last = new_first + (last - first);
        System.arraycopy(buffer, first, new_array, new_first, last - first);
        buffer = new_array;
        first = new_first;
        last = new_last;
    }

    public T removeFirst() {
        if(first == last) {
            return null;
        }
        T cur = buffer[first];
        first += 1;

        if(size() > buffer.length / 4) {
            shrink();
        }

        return cur;
    }

    public T removeLast() {
        if(first == last) {
            return null;
        }
        T cur = buffer[last-1];
        last -= 1;

        if(size() > buffer.length / 4) {
            shrink();
        }

        return cur;
    }

    public T get(int index) {
        if(index >= last - first) {
            return null;
        }

        return buffer[first + index];
    }
}
