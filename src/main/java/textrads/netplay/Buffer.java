package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Buffer {
    
    public static final int DEFAULT_CAPACITY = 1024 * 1024;
    
    private final OutputStream out = new OutputStream() {
        
        @Override
        public void write(final int b) throws IOException {
            if (writeIndex >= data.length) {
                throw new IOException("overflow");
            }
            data[writeIndex++] = (byte) b;
        }        
    };
    
    private final InputStream in = new InputStream() {
        
        private int markIndex;
        
        @Override
        public int read() throws IOException {
            if (readIndex >= writeIndex) {
                return -1;
            }
            return data[readIndex++];
        }    
        
        @Override
        public int available() throws IOException {
            return writeIndex - readIndex;
        }
        
        @Override
        public void mark(final int readlimit) {
            markIndex = readIndex;
        }
        
        @Override
        public void reset() throws IOException {
            readIndex = markIndex;
        }
        
        @Override
        public boolean markSupported() {
            return true;
        }
    };    
    
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
        return readIndex >= writeIndex;
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

    public void write(final Buffer buffer, final int offset, final int length) throws IOException {
        if (writeIndex + length >= data.length) {
            throw new IOException("overflow");
        }
        System.arraycopy(buffer.getData(), offset, data, writeIndex, length);
        writeIndex += length;
    }
    
    public InputStream getInputStream() {
        return in;
    }
    
    public OutputStream getOutputStream() {
        return out;
    }
}
