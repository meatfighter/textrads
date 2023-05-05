package textrads.game;

public class Status {

    private int level;
    private int lines;
    private int score;
    private int updates;
    private int wins;
    
    public void reset() {
        level = lines = score = updates = wins = 0;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(final int level) {
        this.level = level;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(final int lines) {
        this.lines = lines;
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }

    public int getUpdates() {
        return updates;
    }

    public void setUpdates(final int updates) {
        this.updates = updates;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(final int wins) {
        this.wins = wins;
    }
}
