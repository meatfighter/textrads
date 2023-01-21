package textrads.netplay;

public class BytePipe {
    
    public static final int DEFAULT_CAPACITY = 1024 * 1024;
    
    private final byte[] data;
    
    private int readIndex;
    private int writeIndex;
    
    public BytePipe() {
        this(DEFAULT_CAPACITY);
    }
    
    public BytePipe(final int capacity) {
        data = new byte[capacity];
    }
       
    public boolean isEmpty() {
        return writeIndex == readIndex;
    }
    
    public int getSize() {
        return writeIndex - readIndex;
    }
       
    public int getMaxWriteLength() {
        return data.length - writeIndex;
    }
    
    public int getCapacity() {
        return data.length;
    }
    
    public byte[] getData() {
        return data;
    }

    public int getReadIndex() {
        return readIndex;
    }

    public void setReadIndex(final int readIndex) {
        this.readIndex = readIndex;
    }
    
    public void incrementReadIndex(final int value) {
        readIndex += value;
    }
    
    public int getWriteIndex() {
        return writeIndex;
    }
    
    public void setWriteIndex(final int writeIndex) {
        this.writeIndex = writeIndex;
    }
    
    public void incrementWriteIndex(final int value) {
        readIndex += value;
    }
    
    public void clear() {
        readIndex = writeIndex = 0;
    }
}
