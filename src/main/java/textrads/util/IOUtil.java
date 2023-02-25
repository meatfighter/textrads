package textrads.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class IOUtil {
    
    private static final int MAX_BYTE_ARRAY_LENGTH = 1024 * 1024;
    
    public static void writeByteArray(final DataOutputStream out, final byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }
    
    public static byte[] readByteArray(final DataInputStream in) throws IOException {
        final int length = in.readInt();
        if (length < 0 || length > MAX_BYTE_ARRAY_LENGTH) {
            throw new IOException("invalid byte array length");
        }
        final byte[] data = new byte[in.readInt()];
        in.readFully(data);
        return data;
    }
    
    public static void writeString(final DataOutputStream out, final String str) throws IOException {
        final int length = str.length();
        out.writeInt(length);
        for (int i = 0; i < length; i++) {
            out.write((byte) str.charAt(i));
        }
    }
    
    public static String readString(final DataInputStream in) throws IOException {
        final int length = in.readInt();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) in.readByte());
        }
        return sb.toString();
    }
    
    private IOUtil() {        
    }
}
