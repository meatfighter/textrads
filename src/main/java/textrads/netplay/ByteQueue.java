package textrads.netplay;

public class ByteQueue {
    
    private final BytePipe[] outs = new BytePipe[2];
    
    private int writerIndex;
    
    public ByteQueue() {
        outs[0] = new BytePipe();
        outs[1] = new BytePipe();
    }
    
    public ByteQueue(final int capacity) {
        outs[0] = new BytePipe(capacity);
        outs[1] = new BytePipe(capacity);
    }
    
    public synchronized BytePipe borrowWriter() {
        return outs[writerIndex];
    }
    
    public synchronized BytePipe borrowReader() {
        return outs[1 - writerIndex];
    }
    
    public synchronized void returnReader() {
        outs[1 - writerIndex].reset();
    }
}
