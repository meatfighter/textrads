package textrads.db;

public class ExtendedRecord extends Record {
    
    final byte challenge;
    final int time;
    
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
}
