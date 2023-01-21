package textrads.netplay;

import java.io.IOException;
import java.io.OutputStream;

public class BytePipe extends OutputStream {
    
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
    
    @Override
    public void write(final int b) throws IOException {
        
        if (writeIndex == data.length) {
            throw new IOException("overflow");
        }
        
        data[writeIndex++] = (byte) b;
    }
    
    public boolean isEmpty() {
        return writeIndex == 0;
    }
    
    public boolean isFull() {
        return writeIndex == data.length;
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
    
    public int getWriteIndex() {
        return writeIndex;
    }
    
    public void reset() {
        readIndex = writeIndex = 0;
    }
}
