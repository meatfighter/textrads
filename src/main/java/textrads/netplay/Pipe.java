package textrads.netplay;

public class Pipe {
    
    private Buffer writer;
    private Buffer reader;
    
    private boolean writerBorrowed;
    
    public Pipe() {
        writer = new Buffer();
        reader = new Buffer();
    }
    
    public Pipe(final int capacity) {
        writer = new Buffer(capacity);
        reader = new Buffer(capacity);
    }
    
    public synchronized Buffer borrowWriter() throws InterruptedException {        
        while (writerBorrowed) {
            wait();
        }        
        writerBorrowed = true;
        return writer;
    }
    
    public synchronized void returnWriter() {
        writerBorrowed = false;
        notifyAll();
    }
    
    public synchronized Buffer getReader() throws InterruptedException {
        
        if (reader.isEmpty()) {        
            
            while (writerBorrowed || writer.isEmpty()) {
                wait();
            }
            
            final Buffer t = writer;
            writer = reader;
            reader = t;
            
            writer.clear();
        }
        
        return reader;
    }
}
