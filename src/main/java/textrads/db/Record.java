package textrads.db;

import java.io.Serializable;

public class Record implements Serializable, Comparable<Record> {
    
    private static final long serialVersionUID = 1L;
    
    final String initials;
    final int score;
    final short level;
    final long timestamp;
    
    public Record(final String initials, final int score, final short level) {
        this(initials, score, level, System.currentTimeMillis());
    }

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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.score;
        hash = 89 * hash + this.level;
        hash = 89 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Record other = (Record) obj;
        if (this.score != other.score) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        return this.timestamp == other.timestamp;
    }

    @Override
    public String toString() {
        return "Record{initials=" + initials + ", score=" + score + ", level=" + level 
                + ", timestamp=" + timestamp + '}';
    }
}
