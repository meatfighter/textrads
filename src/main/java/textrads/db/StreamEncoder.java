package textrads.db;

import java.io.DataOutputStream;
import java.io.IOException;

public interface StreamEncoder<T> {
    void encode(T object, DataOutputStream out) throws IOException;
}
