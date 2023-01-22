package textrads;

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
}
