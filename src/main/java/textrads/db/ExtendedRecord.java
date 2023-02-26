package textrads.db;

import java.io.Serializable;

public class ExtendedRecord extends Record implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    final byte challenge;
    final int time;
    
    public ExtendedRecord(final String initials, final byte challenge, final short level, final int time, 
            final int score) {
        this(initials, challenge, level, time, score, System.currentTimeMillis());
    }
    
    public ExtendedRecord(final String initials, final byte challenge, final short level, final int time, 
            final int score, final long timestamp) {
        
        super(initials, score, level, timestamp);
        this.challenge = challenge;
        this.time = time;
    }

    public byte getChallenge() {
        return challenge;
    }

    public int getTime() {
        return time;
    }
    
    @Override
    public int compareTo(final Record record) {
        final ExtendedRecord r = (ExtendedRecord) record;
        int result = Byte.compare(r.challenge, challenge);    // descending
        if (result != 0) {
            return result;
        }
        result = Short.compare(r.level, level);               // descending
        if (result != 0) {
            return result;
        }        
        result = Integer.compare(time, r.time);               // ascending
        if (result != 0) {
            return result;
        }
        result = Integer.compare(r.score, score);             // descending
        if (result != 0) {
            return result;
        }
        return Long.compare(timestamp, r.timestamp);          // ascending
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 43 * hash + this.challenge;
        hash = 43 * hash + this.time;
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
        final ExtendedRecord other = (ExtendedRecord) obj;
        if (this.challenge != other.challenge) {
            return false;
        }
        if (this.score != other.score) {
            return false;
        }
        if (this.level != other.level) {
            return false;
        }
        if (this.score != other.score) {
            return false;
        }
        if (this.timestamp != other.timestamp) {
            return false;
        }
        return this.time == other.time;
    }
    
    @Override
    public String toString() {
        return "ExtendedRecord{initials=" + initials + ", challenge=" + challenge + ", level=" + level 
                + ", time=" + time + ", score=" + score + ", timestamp=" + timestamp + '}';
    }    
}
