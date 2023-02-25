package textrads.db;

import java.io.DataInputStream;
import java.io.IOException;
import textrads.util.IOUtil;

public class RecordDecoder implements StreamDecoder<Record> {

    @Override
    public Record decode(final DataInputStream in) throws IOException {
        final String initials = IOUtil.readString(in);
        final int score = in.readInt();
        final short level = in.readShort();
        final long timestamp = in.readLong();
        return new Record(initials, score, level, timestamp);
    }    
}
