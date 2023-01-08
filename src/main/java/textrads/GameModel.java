package textrads;

import java.util.concurrent.ThreadLocalRandom;

public class GameModel {

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20;
    
    private final int[][] playfield = new int[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    
    public GameModel() {
        
        // TODO REMOVE
        for (int i = 0; i < PLAYFIELD_HEIGHT; ++i) {
            for (int j = 0; j < PLAYFIELD_WIDTH; ++j) {
                playfield[i][j] = ThreadLocalRandom.current().nextInt(9);
            }
        }
    }

    public int[][] getPlayfield() {
        return playfield;
    }
}
