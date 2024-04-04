package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    public class Node<T> {
        public Node<T> prev;
        public Node<T> next;
        T value;
    }

    final private Node<T> sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new Node<>();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    public void addFirst(T item) {
        final Node<T> new_node = new Node<>();
        new_node.value = item;
        final Node<T> now_first = sentinel.next;
        now_first.prev = new_node;
        new_node.next = now_first;
        new_node.prev = sentinel;
        sentinel.next = new_node;
        size += 1;
    }

    public void addLast(T item) {
        final Node<T> new_node = new Node<>();
        new_node.value = item;
        final Node<T> now_first = sentinel.prev;
        now_first.next = new_node;
        new_node.prev = now_first;
        new_node.next = sentinel;
        sentinel.prev = new_node;
        size += 1;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node<T> n = sentinel.next;

        while (n != sentinel) {
            System.out.print(n.value);
            System.out.print(' ');
            n = n.next;
        }

        System.out.println("");
    }

    public T removeFirst() {
        if (size == 0) {
            return null;
        }
        size -= 1;
        Node<T> first = sentinel.next;
        sentinel.next = first.next;
        sentinel.next.prev = sentinel;
        return first.value;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        size -= 1;
        Node<T> last = sentinel.prev;
        sentinel.prev = last.prev;
        sentinel.prev.next = sentinel;
        return last.value;
    }

    public T get(int index) {
        if (index >= size) {
            return null;
        }
        Node<T> n = sentinel.next;

        while (index > 0) {
            n = n.next;
            index -= 1;
        }

        return n.value;
    }

    T getR(Node<T> n, int i) {
        if (n == null) {
            return null;
        }
        if (i == 0) {
            return n.value;
        }
        return getR(n.next, i-1);
    }

    public T getRecursive(int index) {
        return getR(sentinel, index);
    }

    class ListIterator<T> implements Iterator<T> {
        final private Node<T> sentinel;
        private Node<T> cur;
        public ListIterator(LinkedListDeque obj) {
            sentinel = obj.sentinel;
            cur = sentinel;
        }

        public boolean hasNext() {
            return cur.next != sentinel;
        }

        public T next() {
            cur = cur.next;
            return cur.value;
        }
    }

    public Iterator<T> iterator() {
        return new ListIterator<T>(this);
    }

    public boolean equals(Object o) {
        if (o instanceof LinkedListDeque) {
            LinkedListDeque o1 = (LinkedListDeque) o;
            if (o1.size() != size) {
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
