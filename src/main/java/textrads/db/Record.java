package textrads.db;

public class Record {

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
}
