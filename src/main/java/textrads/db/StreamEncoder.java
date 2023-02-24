package textrads.db;

import java.io.IOException;
import java.io.OutputStream;

public interface StreamEncoder<T> {
    void encode(T object, OutputStream out) throws IOException;
}
