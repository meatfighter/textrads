package textrads.db;

import java.io.Serializable;

public class Record implements Serializable, Comparable<Record> {
    
    private static final long serialVersionUID = 1L;
    
    public static final RecordMaker<Record> RECORD_MAKER = index -> {
        final char initial = (char) ('A' + index);
        return new Record(new StringBuilder().append(initial).append(initial).append(initial).toString(), 
                10_000 * (10 - index), (short) 0, 0L);
    };

    final String initials;
    final int score;
    final short level;
    final long timestamp;

    public Record(final String initials, final int score, final short level, final long timestamp) {
        this.initials = initials;
        this.score = score;
        this.level = level;
        this.timestamp = timestamp;
    }

    public String getInitials() {
        return initials;
    }

    public int getScore() {
        return score;
    }

    public short getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(final Record r) {
        int result = Integer.compare(r.score, score); // descending
        if (result != 0) {
            return result;
        }
        result = Short.compare(r.level, level);       // descending
        if (result != 0) {
            return result;
        }
        return Long.compare(timestamp, r.timestamp);  // ascending
    }
}
