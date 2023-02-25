package textrads.db;

import java.util.Comparator;

public class ExtendedRecordComparator implements Comparator<ExtendedRecord> {

    @Override
    public int compare(final ExtendedRecord r1, final ExtendedRecord r2) {
        int result = Byte.compare(r2.challenge, r1.challenge);    // descending
        if (result != 0) {
            return result;
        }
        result = Short.compare(r2.level, r1.level);               // descending
        if (result != 0) {
            return result;
        }        
        result = Integer.compare(r1.time, r2.time);               // ascending
        if (result != 0) {
            return result;
        }
        result = Integer.compare(r2.score, r1.score);             // descending
        if (result != 0) {
            return result;
        }
        return Long.compare(r1.timestamp, r2.timestamp);          // ascending
    }    
}
