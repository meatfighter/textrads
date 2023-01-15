package textrads;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MonoGameState {
    
    private static enum GameStateMode {
        TETROMINO_FALLING,
        CLEARING_LINES,
    }

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20;    
    
    private static final int EMPTY_BLOCK = 0;
    
    private static final double LEVEL_ZERO_FRAMES_PER_DROP = 52.0;
    private static final double LEVEL_TWENTY_FRAMES_PER_DROP = 2.0;
    
    private static final double DROP_DECAY_CONSTANT 
            = Math.log(LEVEL_TWENTY_FRAMES_PER_DROP / LEVEL_ZERO_FRAMES_PER_DROP) / 20.0;
    
    private final int[][] playfield = new int[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Integer> nexts = new ArrayList<>();
    private final Random random = new Random();
    
    private int attackRows;
    private int score;
    private int level;
    private int lines;
    
    private int tetrominoType = 0;
    private int tetrominoRotation = 0;
    private int tetrominoX = 5;
    private int tetrominoY = 0;
    
    private double framesPerGravityDrop;
    private double framesPerLock;        
    private double gravityDropTimer;
    private double lockTimer;
    private boolean dropFailed;
    
    private GameStateMode mode = GameStateMode.TETROMINO_FALLING;
    
    public MonoGameState() {
        updateNexts();
        updateFramesPerConstants();
        resetGravityDropTimer();
        resetLockTimer();
    }
    
    private void resetGravityDropTimer() {
        gravityDropTimer = framesPerGravityDrop;
    }
    
    private void resetLockTimer() {
        lockTimer = framesPerLock;
    }
    
    private void updateNexts() {
        if (nexts.size() < 5) {            
            for (int i = 0; i < 7; ++i) {
                nexts.add(i);
            }
            Collections.shuffle(nexts.subList(nexts.size() - 7, nexts.size()), random);
        }
    }
    
    private void incrementLevel() {
        ++level;
        updateFramesPerConstants();
    }
    
    private void updateFramesPerConstants() {
        framesPerGravityDrop = LEVEL_ZERO_FRAMES_PER_DROP * Math.exp(DROP_DECAY_CONSTANT * level);
        framesPerLock = Math.max(32.0, framesPerGravityDrop + 4.0);
    }
    
    private void lockTetrimino() {
        for (final int[] block : Tetrominoes.TETROMINOES[tetrominoType][tetrominoRotation]) {
            final int by = tetrominoY + block[1];
            if (by < 0) {
                continue;
            }
            final int bx = tetrominoX + block[0];
            playfield[by][bx] = tetrominoType + 1;
        }
    }
    
    private boolean testPosition(final int rotation, final int x, final int y) {
        if (y < 0) {
            return false;
        }
        for (final int[] block : Tetrominoes.TETROMINOES[tetrominoType][rotation]) {
            final int bx = x + block[0];
            final int by = y + block[1];            
            if (bx < 0 || bx >= PLAYFIELD_WIDTH || by >= PLAYFIELD_HEIGHT 
                    || (by >= 0 && playfield[by][bx] != EMPTY_BLOCK)) {
                return false;
            }
        }
        return true;
    }
    
    public void handleEvents(final List<GameEvent> events) {
        for (final GameEvent event : events) {
            if (event == GameEvent.UPDATE) {
                update();
            } else if (mode == GameStateMode.TETROMINO_FALLING) {
                switch (event) {
                    case ROTATE_CCW: 
                        attemptRotateCCW();
                        break;
                    case ROTATE_CW:
                        attemptRotateCW();               
                        break;
                    case SHIFT_LEFT:
                        attemptShiftLeft();
                        break;
                    case SHIFT_RIGHT:
                        attemptShiftRight();
                        break;
                    case SOFT_DROP:
                        attemptDrop();
                        break;
                }
            }
        }
    }
    
    private void attemptRotateCCW() {
        if (tetrominoType == Tetrominoes.O_TYPE) {
            return;
        }

        final int rotation = (tetrominoRotation == 0) ? 3 : tetrominoRotation - 1;
        if (testPosition(rotation, tetrominoX, tetrominoY)) {
            tetrominoRotation = rotation;
            return;
        }

        final int[] offsets = Tetrominoes.CCW[tetrominoRotation];
        final int x = tetrominoX + offsets[0];
        final int y = tetrominoY + offsets[1];
        if (testPosition(rotation, x, y)) {
            tetrominoRotation = rotation;
            tetrominoX = x;
            tetrominoY = y;
        }
    }
    
    private void attemptRotateCW() {
        if (tetrominoType == Tetrominoes.O_TYPE) {
            return;
        }

        final int rotation = (tetrominoRotation == 3) ? 0 : tetrominoRotation + 1;
        if (testPosition(rotation, tetrominoX, tetrominoY)) {
            tetrominoRotation = rotation;
            return;
        }

        final int[] offsets = Tetrominoes.CW[tetrominoRotation];
        final int x = tetrominoX + offsets[0];
        final int y = tetrominoY + offsets[1];
        if (testPosition(rotation, x, y)) {
            tetrominoRotation = rotation;
            tetrominoX = x;
            tetrominoY = y;
        }     
    }
    
    private void attemptShiftLeft() {
        final int x = tetrominoX - 1;
        if (testPosition(tetrominoRotation, x, tetrominoY)) {
            tetrominoX = x;
        }
    }
    
    private void attemptShiftRight() {
        final int x = tetrominoX + 1;
        if (testPosition(tetrominoRotation, x, tetrominoY)) {
            tetrominoX = x;
        }
    }
    
    private void attemptDrop() {
        final int y = tetrominoY + 1;
        dropFailed = !testPosition(tetrominoRotation, tetrominoX, y);
        if (!dropFailed) {            
            dropFailed = false;
            tetrominoY = y;
            resetGravityDropTimer();
            resetLockTimer();
        } 
    }
    
    private void update() {
        if (mode == GameStateMode.TETROMINO_FALLING) {
            if (dropFailed) {
                if (--lockTimer < 0) {
                    mode = GameStateMode.CLEARING_LINES;
                }
            } else if (--gravityDropTimer < 0) {
                resetGravityDropTimer();
                attemptDrop();
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
