package textrads.db;

import java.io.DataOutputStream;
import java.io.IOException;
import textrads.util.IOUtil;

public class ExtendedRecordEncoder implements StreamEncoder<ExtendedRecord> {

    @Override
    public void encode(final ExtendedRecord r, final DataOutputStream out) throws IOException {
        IOUtil.writeString(out, r.getInitials());
        out.writeByte(r.getChallenge());
        out.writeShort(r.getLevel());
        out.writeInt(r.getTime());
        out.writeInt(r.getScore());
        out.writeLong(r.getTimestamp());
    }    
}