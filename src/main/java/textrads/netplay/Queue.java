package textrads.netplay;

import textrads.app.Textrads;

public class Queue {
    
    private static final int DEFAULT_PLAYERS = 2;
    private static final int DEFAULT_CAPACITY = 60 * Textrads.FRAMES_PER_SECOND;

    private final Element[] elements;
    
    private int readIndex;
    private int writeIndex;
    private int size;
    
    public Queue() {
        this(DEFAULT_PLAYERS, DEFAULT_CAPACITY);
    }
    
    public Queue(final int players) {
        this(players, DEFAULT_CAPACITY);
    }
    
    public Queue(final int players, final int capacity) {
        elements = new Element[capacity];
        for (int i = capacity - 1; i >= 0; --i) {
            elements[i] = new Element(players);
        }
    }
    
    public synchronized Element getWriteElement() {
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
    
    public synchronized Element getReadElement() {
        return elements[readIndex];
    }
    
    public synchronized void incrementReadIndex() {
        if (size > 0) {
            --size;
            elements[readIndex].clear();
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
