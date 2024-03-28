package deque;

public class LinkedListDeque<T> {
    public class Node<T> {
        public Node<T> prev;
        public Node<T> next;
        T value;
    }

    private Node<T> sential;
    private int size;

    public LinkedListDeque() {
        sential = new Node<>();
        size = 0;
    }

    public add(T v) {
        final Node<T> node = new Node<>();
    }
}
