package textrads.db;

import java.util.Comparator;

public class RecordComparator implements Comparator<Record> {

    @Override
    public int compare(final Record r1, Record r2) {
        int result = Integer.compare(r2.score, r1.score); // descending
        if (result != 0) {
            return result;
        }
        result = Short.compare(r2.level, r1.level);       // descending
        if (result != 0) {
            return result;
        }
        return Long.compare(r1.timestamp, r2.timestamp);  // ascending
    }    
}
