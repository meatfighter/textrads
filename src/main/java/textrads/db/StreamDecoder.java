package textrads.db;

import java.io.DataInputStream;
import java.io.IOException;

public interface StreamDecoder<T> {
    T decode(DataInputStream in) throws IOException;
}
