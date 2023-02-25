package textrads.db;

import java.io.DataInputStream;
import java.io.IOException;

public class RecordDecoder implements StreamDecoder<Record> {

    @Override
    public Record decode(final DataInputStream in) throws IOException {
        final int score = in.readInt();
        final short level = in.readShort();
        final long timestamp = in.readLong();
        return new Record(score, level, timestamp);
    }    
}
