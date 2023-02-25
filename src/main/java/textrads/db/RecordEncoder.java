package textrads.db;

import java.io.IOException;
import java.io.DataOutputStream;
import textrads.util.IOUtil;

public class RecordEncoder implements StreamEncoder<Record> {

    @Override
    public void encode(final Record r, final DataOutputStream out) throws IOException {
        IOUtil.writeString(out, r.getInitials());
        out.writeInt(r.getScore());
        out.writeShort(r.getLevel());
        out.writeLong(r.getTimestamp());
    }    
}
