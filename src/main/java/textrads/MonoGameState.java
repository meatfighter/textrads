package textrads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MonoGameState {
    
    public static enum GameStateMode {
        TETROMINO_FALLING,
        CLEARING_LINES,
    }

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20; 
    
    private static final int SPAWN_X = 5;
    private static final int SPAWN_Y = 0;
    private static final int SPAWN_ROTATION = 0;
    
    private static final int EMPTY_BLOCK = 0;
    
    private static final double LEVEL_ZERO_FRAMES_PER_DROP = 52.0;
    private static final double LEVEL_TWENTY_FRAMES_PER_DROP = 2.0;
    
    private static final double DROP_DECAY_CONSTANT 
            = Math.log(LEVEL_TWENTY_FRAMES_PER_DROP / LEVEL_ZERO_FRAMES_PER_DROP) / 20.0;
    
    private final int[][] playfield = new int[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Integer> nexts = new ArrayList<>();
    private final Random random = new Random();
    
    private final List<Integer> lineYs = new ArrayList<>();
    
    private int attackRows;
    private int score;
    private int level = 10;
    private int lines;
    
    private int tetrominoType;
    private int tetrominoRotation;
    private int tetrominoX;
    private int tetrominoY;
    
    private double framesPerGravityDrop;
    private double framesPerLock;        
    private double gravityDropTimer;
    private double lockTimer;
    private boolean dropFailed;
    
    private int lineClearTimer;
    
    private boolean softDropPressed;
    private boolean softDropReleased = true;
    private boolean justLocked;
    private int softDropNotPressedFrames;
    private int lastSoftDropNotPressedFrames;
    
    private GameStateMode mode = GameStateMode.TETROMINO_FALLING;
    
    public MonoGameState() {
        spawn();
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
    
    private void resetLineClearTimer() {
        lineClearTimer = 50;
    }
    
    private void updateNexts() {
        if (nexts.size() < 7) {            
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
        justLocked = true;
        for (final int[] block : Tetrominoes.TETROMINOES[tetrominoType][tetrominoRotation]) {
            final int by = tetrominoY + block[1];
            if (by < 0) {
                continue;
            }
            final int bx = tetrominoX + block[0];
            playfield[by][bx] = tetrominoType + 1;
        }
        findLines();
        if (lineYs.isEmpty()) {
            spawn();
        } else {
            mode = GameStateMode.CLEARING_LINES;
            resetLineClearTimer();
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
    
    private void findLines() {
        final int maxY = Math.min(PLAYFIELD_HEIGHT - 1, tetrominoY + 1);
        final int minY = Math.max(0, tetrominoY - 2);
        outer: for (int y = minY; y <= maxY; ++y) {
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                if (playfield[y][x] == EMPTY_BLOCK) {
                    continue outer;
                }
            }
            lineYs.add(y);
        }
    }
    
    public void handleEvents(final List<GameEvent> events) {
        for (final GameEvent event : events) {
            if (event == GameEvent.SOFT_DROP) {
                softDropPressed = true;                
                if (!justLocked && mode == GameStateMode.TETROMINO_FALLING) {
                    attemptSoftDrop();
                }
            } else if (event == GameEvent.UPDATE) {
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
    
    private void attemptSoftDrop() {
        final int y = tetrominoY + 1;
        if (testPosition(tetrominoRotation, tetrominoX, y)) {
            tetrominoY = y;
            resetGravityDropTimer();
            resetLockTimer();
        } else {
            lockTetrimino();
        } 
    }
    
    private void attemptGravityDrop() {
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
        if (justLocked && softDropReleased) {
            justLocked = false;
        }
        if (softDropPressed) {
            softDropPressed = false;
            lastSoftDropNotPressedFrames = softDropNotPressedFrames;
            softDropNotPressedFrames = 0;            
        } else {
            softDropReleased = ++softDropNotPressedFrames > lastSoftDropNotPressedFrames + 3;
        }
        switch (mode) {
            case TETROMINO_FALLING:
                updateFallingTetromino();
                break;
            case CLEARING_LINES:
                updateClearingLines();
                break;
        }
    }
        
    private void updateFallingTetromino() {
        if (dropFailed) {
            if (--lockTimer < 0) {                
                lockTetrimino();                
            }
        } else if (--gravityDropTimer < 0) {
            resetGravityDropTimer();
            attemptGravityDrop();
        }
    }  
    
    private void updateClearingLines() {
        if (--lineClearTimer < 0) {
            clearLines();
            spawn();
        }
    }
    
    private void clearLines() {
        for (final int lineY : lineYs) {
            for (int y = lineY; y > 0; --y) {
                System.arraycopy(playfield[y - 1], 0, playfield[y], 0, PLAYFIELD_WIDTH);
            }
            Arrays.fill(playfield[0], EMPTY_BLOCK);
        }
        lineYs.clear();
    }
    
    private void spawn() {
        mode = GameStateMode.TETROMINO_FALLING;
        dropFailed = false;
        resetGravityDropTimer();
        resetLockTimer();
        updateNexts();
        tetrominoType = nexts.remove(0);
        tetrominoX = SPAWN_X;
        tetrominoY = SPAWN_Y;
        tetrominoRotation = SPAWN_ROTATION;
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

    public double getLockTimer() {
        return lockTimer;
    }

    public GameStateMode getMode() {
        return mode;
    }

    public int getLineClearTimer() {
        return lineClearTimer;
    }

    public List<Integer> getLineYs() {
        return lineYs;
    }
}
