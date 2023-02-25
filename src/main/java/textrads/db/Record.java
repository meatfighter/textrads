package textrads.db;

public class Record {

    final int score;
    final short level;
    final long timestamp;

    public Record(final int score, final short level, final long timestamp) {
        this.score = score;
        this.level = level;
        this.timestamp = timestamp;
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
