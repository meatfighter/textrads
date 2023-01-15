package textrads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MonoGameState {

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20;
    
    private final int[][] playfield = new int[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Integer> nexts = new ArrayList<>();
    private final Random random = new Random();
    
    private int attackRows;
    private int score;
    private int level;
    private int lines;
    
    private int tetrominoType = 6;
    private int tetrominoRotation = 0;
    private int tetrominoX = 5;
    private int tetrominoY = 0;
    
    public MonoGameState() {
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
    
    public void handleEvents(final List<GameEvent> events) {
        for (final GameEvent event : events) {
            switch (event) {
                case ROTATE_CCW:
                    if (tetrominoType == Tetrominoes.O_TYPE) {
                        break;
                    }
                    
                    if (tetrominoRotation == 0) {
                        tetrominoRotation = 3;
                    } else {
                        --tetrominoRotation;
                    }
                    break;
                case ROTATE_CW:
                    if (tetrominoType == Tetrominoes.O_TYPE) {
                        break;
                    }
                    
                    if (tetrominoRotation == 3) {
                        tetrominoRotation = 0;
                    } else {
                        ++tetrominoRotation;
                    }
                    break;
            }
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

    public int getAttackRows() {
        return attackRows;
    }

    public void setAttackRows(final int attackRows) {
        this.attackRows = attackRows;
    }

    public int getTetrominoType() {
        return tetrominoType;
    }

    public int getTetrominoRotation() {
        return tetrominoRotation;
    }

    public int getTetrominoX() {
        return tetrominoX;
    }

    public int getTetrominoY() {
        return tetrominoY;
    }
}
