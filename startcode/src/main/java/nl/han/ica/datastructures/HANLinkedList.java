package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T> {

    private Node<T> headerNode;
    private int size = 0;
    private int modCount = 0;

    @Override
    public void addFirst(T value) {
        headerNode = new Node<>(value, headerNode);
        ++this.size;
    }

    @Override
    public void clear() {
        headerNode = null;
        this.size = 0;
        ++this.modCount;
    }

    @Override
    public void insert(int index, T value) {
        Node<T> newNode = new Node<>(value);
        Node<T> current = headerNode;

        if (index > this.size) {
            throw new IndexOutOfBoundsException();
        }

        if (current != null) {
            for (int i = 0; i < index - 1 && current.getNext() != null; ++i) {
                current = current.getNext();
            }
        }
        // set the new node's next-node reference to this node's next-node reference
        newNode.setNext(current != null ? current.getNext() : null);
        // now set this node's next-node reference to the new node
        if (current != null) {
            current.setNext(newNode);
        }
        ++this.size;
        ++this.modCount;
    }

    @Override
    public void delete(int index) {
        if (index < 0 || index > this.size) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> current = headerNode;
        if (current != null) {
            for (int i = 0; i < index; ++i) {
                if (current.getNext() == null) {
                    throw new IndexOutOfBoundsException();
                }
                current.setNext(current.getNext().getNext());
            }
            --this.size;
            ++this.modCount;
        }
    }

    @Override
    public T get(int pos) {
        return this.getNode(pos, 0, this.size - 1).getData();
    }

    @Override
    public void removeFirst() {
        if (headerNode.getData() == null) {
            throw new IndexOutOfBoundsException();
        }
        headerNode = headerNode.getNext();
        --this.size;
        ++this.modCount;
    }

    @Override
    public T getFirst() {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException();
        }
        return this.get(0);
    }

    @Override
    public int getSize() {
        return this.size;
    }

    public boolean isEmpty() {
        return this.size == 0;
    }

    private Node<T> getNode(int index, int low, int high) {
        Node<T> node;

        if (index < low || index > high) {
            throw new IndexOutOfBoundsException();
        }
        node = headerNode;
        for (int i = 0; i < index; ++i) {
            node = node.getNext();
        }
        return node;
    }

    @Override
    public Iterator<T> iterator() {
        return null;
    }

    public int getModCount() {
        return modCount;
    }

    public void setModCount(int modCount) {
        this.modCount = modCount;
    }

    private static class Node<T> {

        private T data;
        private Node<T> next;

        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }

        public Node(T data) {
            this.data = data;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }
    }
}
