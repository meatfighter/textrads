package textrads;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ByteList {
    
    private final byte[] data;
    
    private int size;
    
    public ByteList() {
        this(InputEventSource.MAX_POLLS);
    }
    
    public ByteList(final int capacity) {
        data = new byte[capacity];
    }
    
    public void add(final byte value) {
        data[size++] = value;
    }
    
    public byte get(final int index) {
        return data[index];
    }
    
    public void set(final int index, final byte value) {
        data[index] = value;
        size = Math.min(size, index + 1);
    }
    
    public void clear() {
        size = 0;
    }
    
    public int size() {
        return size;
    }
    
    public int capacity() {
        return data.length;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void read(final DataInputStream in) throws IOException {
        final int length = in.read();
        if (length < 0 || length > InputEventSource.MAX_POLLS) {
            throw new IOException("invalid events length");
        }                
        in.readFully(data, 0, length);
        size = length;
    }

    public void write(final DataOutputStream out) throws IOException {
        out.write(size);
        out.write(data, 0, size);
    }
}
