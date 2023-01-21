package textrads.netplay;

public class ByteQueue {
    
    private BytePipe writer;
    private BytePipe reader;
    
    private boolean writerBorrowed;
    
    public ByteQueue() {
        writer = new BytePipe();
        reader = new BytePipe();
    }
    
    public ByteQueue(final int capacity) {
        writer = new BytePipe(capacity);
        reader = new BytePipe(capacity);
    }
    
    public synchronized BytePipe borrowWriter() {
        writerBorrowed = true;
        return writer;
    }
    
    public synchronized void returnWriter() {
        writerBorrowed = false;
        notifyAll();
    }
    
    public synchronized BytePipe getReader() {
        return reader;
    }
    
    public synchronized BytePipe waitForWrites() throws InterruptedException {
        
        while (writerBorrowed || writer.isEmpty()) {
            wait();
        }
        
        final BytePipe t = writer;
        writer = reader;
        reader = t;
        
        writer.clear();
        
        return reader;
    }
}
