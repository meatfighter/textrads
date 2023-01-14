package textrads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MonoGameState {

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20;
    
    private final int[][] playfield = new int[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Integer> nexts = new ArrayList<>();
    private final Random random = new Random();
    
    public MonoGameState() {
        
        // TODO REMOVE
        for (int i = 0; i < PLAYFIELD_HEIGHT; ++i) {
            for (int j = 0; j < PLAYFIELD_WIDTH; ++j) {
                playfield[i][j] = ThreadLocalRandom.current().nextInt(9);
            }
        }
        updateNexts();
    }
    
    private void updateNexts() {
        if (nexts.size() < 5) {            
            for (int i = 0; i < 7; ++i) {
                nexts.add(i);
            }
            Collections.shuffle(nexts.subList(nexts.size() - 7, nexts.size()), random);
        }
    }
    
    public void setSeed(final long seed) {
        random.setSeed(seed);
    }

    public int[][] getPlayfield() {
        return playfield;
    }
    
    public List<Integer> getNexts() {
        return nexts;
    }
}
