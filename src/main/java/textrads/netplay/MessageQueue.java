package textrads.netplay;

import textrads.app.Textrads;

public class MessageQueue {
    
    private static final int DEFAULT_PLAYERS = 2;
    private static final int DEFAULT_CAPACITY = 60 * Textrads.FRAMES_PER_SECOND;

    private final Message[] messages;
    
    private int readIndex;
    private int writeIndex;
    private int size;
    private boolean running;
    
    public MessageQueue() {
        this(DEFAULT_PLAYERS, DEFAULT_CAPACITY);
    }
    
    public MessageQueue(final int players) {
        this(players, DEFAULT_CAPACITY);
    }
    
    public MessageQueue(final int players, final int capacity) {
        messages = new Message[capacity];
        for (int i = capacity - 1; i >= 0; --i) {
            messages[i] = new Message(players);
        }
    }
    
    public synchronized void start() {
        running = true;
        notifyAll();
    }
    
    public synchronized void stop() {
        running = false;
        notifyAll();
    }    
    
    public synchronized Message getWriteMessage() {
        return messages[writeIndex];
    }
    
    public synchronized void incrementWriteIndex() {
        if (size < messages.length) {
            if (size == 0) {
                notifyAll();
            }
            ++size;
            if (++writeIndex == messages.length) {
                writeIndex = 0;
            }            
        }
    }
    
    public synchronized void waitForMessage() {
        waitForMessage(0);
    }
    
    public synchronized void waitForMessage(final long timeout) {
        while (running && size == 0) {
            try {
                wait(timeout);
            } catch (final InterruptedException ignored) {                
            }
        }
    }
    
    public synchronized Message getReadMessage() {
        return messages[readIndex];
    }
    
    public synchronized void incrementReadIndex() {
        if (size > 0) {
            --size;
            messages[readIndex].clear();
            if (++readIndex == messages.length) {
                readIndex = 0;
            }
        }
    }    
    
    public synchronized boolean isFull() {
        return size >= messages.length;
    }
    
    public synchronized boolean isEmpty() {
        return size == 0;
    }
    
    public synchronized int size() {
        return size;
    }
    
    public synchronized int getCapacity() {
        return messages.length;
    }
}
