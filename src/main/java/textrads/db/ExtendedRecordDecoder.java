package textrads.db;

import java.io.DataInputStream;
import java.io.IOException;
import textrads.util.IOUtil;

public class ExtendedRecordDecoder implements StreamDecoder<ExtendedRecord> {

    @Override
    public ExtendedRecord decode(final DataInputStream in) throws IOException {
        final String initials = IOUtil.readString(in);
        final byte challenge = in.readByte();
        final short level = in.readShort();
        final int time = in.readInt();
        final int score = in.readInt();
        final long timestamp = in.readLong();
        return new ExtendedRecord(initials, challenge, level, time, score, timestamp);
    }    
}
