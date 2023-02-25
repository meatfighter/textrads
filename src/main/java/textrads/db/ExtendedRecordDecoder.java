package textrads.db;

import java.io.DataInputStream;
import java.io.IOException;

public class ExtendedRecordDecoder implements StreamDecoder<ExtendedRecord> {

    @Override
    public ExtendedRecord decode(final DataInputStream in) throws IOException {
        final byte challenge = in.readByte();
        final short level = in.readShort();
        final int time = in.readInt();
        final int score = in.readInt();
        final long timestamp = in.readLong();
        return new ExtendedRecord(challenge, level, time, score, timestamp);
    }    
}
