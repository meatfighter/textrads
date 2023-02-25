package textrads.db;

import java.io.IOException;
import java.io.DataOutputStream;

public class RecordStreamEncoder implements StreamEncoder<Record> {

    @Override
    public void encode(final Record r, final DataOutputStream out) throws IOException {
        out.writeInt(r.getScore());
        out.writeShort(r.getLevel());
        out.writeLong(r.getTimestamp());
    }    
}
