package textrads.db;

import java.io.IOException;
import java.io.InputStream;

public interface StreamDecoder<T> {
    T decode(InputStream in) throws IOException;
}
