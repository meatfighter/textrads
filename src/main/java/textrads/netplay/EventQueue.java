package textrads.netplay;

import textrads.Textrads;

public class EventQueue {
    
    private static final int DEFAULT_PLAYERS = 2;
    private static final int DEFAULT_CAPACITY = 60 * Textrads.FRAMES_PER_SECOND;

    private final EventQueueElement[] elements;
    
    private int readIndex;
    private int writeIndex;
    private int size;
    
    public EventQueue() {
        this(DEFAULT_PLAYERS, DEFAULT_CAPACITY);
    }
    
    public EventQueue(final int players) {
        this(players, DEFAULT_CAPACITY);
    }
    
    public EventQueue(final int players, final int capacity) {
        elements = new EventQueueElement[capacity];
        for (int i = capacity - 1; i >= 0; --i) {
            elements[i] = new EventQueueElement(players);
        }
    }
    
    public synchronized EventQueueElement getWriteElement() {
        return elements[writeIndex];
    }
    
    public synchronized void incrementWriteIndex() {
        if (size < elements.length) {
            if (size == 0) {
                notifyAll();
            }
            ++size;
            if (++writeIndex == elements.length) {
                writeIndex = 0;
            }            
        }
    }
    
    public synchronized void waitForData() throws InterruptedException {
        waitForData(0);
    }
    
    public synchronized void waitForData(final long timeout) throws InterruptedException {
        while (size == 0) {
            wait(timeout);
        }
    }
    
    public synchronized EventQueueElement getReadElement() {
        return elements[readIndex];
    }
    
    public synchronized void incrementReadIndex() {
        if (size > 0) {
            --size;
            if (++readIndex == elements.length) {
                readIndex = 0;
            }
        }
    }    
    
    public synchronized boolean isFull() {
        return size >= elements.length;
    }
    
    public synchronized boolean isEmpty() {
        return size == 0;
    }
    
    public synchronized int size() {
        return size;
    }
    
    public synchronized int getCapacity() {
        return elements.length;
    }
}
