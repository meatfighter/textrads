package textrads.netplay;

import textrads.app.Textrads;
import textrads.input.InputEventList;
import textrads.input.InputEventSource;

public class InputQueue {

    private static final int DEFAULT_CAPACITY = 60 * Textrads.FRAMES_PER_SECOND * InputEventSource.MAX_POLLS;
    
    private final byte[] elements;
    
    private int head;
    private int tail;
    private int size;
    
    public InputQueue() {
        this(DEFAULT_CAPACITY);
    }
    
    public InputQueue(final int capacity) {
        elements = new byte[capacity];
    }
    
    public void clear() {
        head = tail = size = 0;
    }
    
    public void enqueue(final byte element) {
        if (size == elements.length) {
            return;
        }
        elements[head] = element;
        ++size;
        if (++head == elements.length) {
            head = 0;
        }
    }
    
    public void enqueue(final InputEventList eventList) {
        for (int i = 0, length = eventList.size(); i < length && size < elements.length; ++i) {
            elements[head] = eventList.get(i);
            ++size;
            if (++head == elements.length) {
                head = 0;
            }
        }
        eventList.clear();
    }
    
    public byte dequeue() {
        if (size == 0) {
            return -1;
        }
        final byte element = elements[tail];
        --size;
        if (++tail == elements.length) {
            tail = 0;
        }
        return element;
    }
    
    public void dequeue(final InputEventList eventList) {
        eventList.clear();
        while (size > 0 && !eventList.isFull()) {
            eventList.add(elements[tail]);
            --size;
            if (++tail == elements.length) {
                tail = 0;
            }
        }
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean isFull() {
        return size == elements.length;
    }
    
    public int capacity() {
        return elements.length;
    }
}
