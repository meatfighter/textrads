package textrads.netplay;

import java.io.IOException;
import java.io.OutputStream;

public class Buffer extends OutputStream {
    
    public static final int DEFAULT_CAPACITY = 1024 * 1024;
    
    private final byte[] data;
    
    private int readIndex;
    private int writeIndex;
    
    public Buffer() {
        this(DEFAULT_CAPACITY);
    }
    
    public Buffer(final int capacity) {
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
    
    public void shift() {
        if (readIndex == 0) {
            return;
        }
        if (readIndex == writeIndex) {
            readIndex = writeIndex = 0;
            return;
        }
        System.arraycopy(data, readIndex, data, 0, writeIndex - readIndex);
        writeIndex -= readIndex;
        readIndex = 0;        
    }

    @Override
    public void write(final int b) throws IOException {
        if (writeIndex >= data.length) {
            throw new IOException("overflow");
        }
        data[writeIndex++] = (byte) b;
    }
    
    public void write(final Buffer buffer) throws IOException {
        write(buffer, buffer.getSize());
    }
    
    public void write(final Buffer buffer, final int length) throws IOException {
        if (writeIndex + length >= data.length) {
            throw new IOException("overflow");
        }
        System.arraycopy(buffer.getData(), buffer.getReadIndex(), data, writeIndex, length);
        writeIndex += length;
    }
}
