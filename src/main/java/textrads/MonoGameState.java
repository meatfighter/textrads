package textrads;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MonoGameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final long MAX_SCORE = 999_999_999L;
    
    private static final int[] CLEAR_POINTS = { 0, 40, 100, 300, 1200 };
    private static final int[] ATTACK_ROWS = { 0, 0, 1, 2, 4 };
    
    public static final byte DISABLED_MODE = 0;
    public static final byte TETROMINO_FALLING_MODE = 1;
    public static final byte CLEARING_LINES_MODE = 2;
    public static final byte ADDING_GARBAGE_MODE = 3;
    public static final byte GAME_OVER_MODE = 4;

    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20; 
    
    public static final int SPAWN_X = 4;
    public static final int SPAWN_Y = 0;
    public static final int SPAWN_ROTATION = 0;
    
    private static final byte EMPTY_BLOCK = 0;
    private static final byte GARBAGE_BLOCK = 8;
    
    private static final int MOVES_PER_GARBAGE_ROW = 8;
    
    private static final double LEVEL_ZERO_FRAMES_PER_DROP = 52.0;
    private static final double LEVEL_THIRTY_FRAMES_PER_DROP = 2.0;
    
    private static final double DROP_DECAY_CONSTANT 
            = Math.log(LEVEL_THIRTY_FRAMES_PER_DROP / LEVEL_ZERO_FRAMES_PER_DROP) / 30.0;
    
    private final byte[][] playfield = new byte[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Byte> nexts = new ArrayList<>();
    
    private final Random tetrominoRandomizer = new Random();
    private final Random garbageRandomizer = new Random();
    
    private final List<Byte> lineYs = new ArrayList<>();

    private final GameState gameState;     
    
    private MonoGameState opponent;

    private int score;
    private short level;
    private short lines;
    private short wins;
    private byte attackRows;
    
    private byte tetrominoType;
    private byte tetrominoRotation;
    private byte tetrominoX;
    private byte tetrominoY;
    
    private float framesPerGravityDrop;       
    private float gravityDropTimer;
    private byte framesPerLock;
    private byte lockTimer;
    private boolean dropFailed;
    
    private byte lineClearTimer;
    private byte gameOverTimer;
    
    private boolean newlySpawened;
    
    private byte garbageX;
    private byte garbageCounter;
    
    private byte mode;
    
    public MonoGameState(final GameState gameState) {
        this.gameState = gameState;
        reset();
    }
    
    public void reset() {
        
        attackRows = 0;
        score = 0;
        level = 0;
        lines = 0;
        tetrominoType = 0;
        tetrominoRotation = 0;
        tetrominoX = 0;
        tetrominoY = 0;
        framesPerGravityDrop = 0;       
        gravityDropTimer = 0;
        framesPerLock = 0;
        lockTimer = 0;
        dropFailed = false;
        lineClearTimer = 0;
        gameOverTimer = 0;
        newlySpawened = false;
        garbageX = 0;
        garbageCounter = 0;
        mode = TETROMINO_FALLING_MODE;
        
        for (int y = PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
            Arrays.fill(playfield[y], (byte) 0);
        }
        nexts.clear();
        lineYs.clear();
    }
    
    public void init() {
        attemptSpawn();
        updateFramesPerConstants();
    }
    
    private void resetLineClearTimer() {
        lineClearTimer = 50;
    }
    
    private void updateNexts() {
        if (nexts.size() < 7) {            
            for (int i = 0; i < 7; ++i) {
                nexts.add((byte) i);
            }
            Collections.shuffle(nexts.subList(nexts.size() - 7, nexts.size()), tetrominoRandomizer);
        }
    }
    
    private void updateFramesPerConstants() {
        framesPerGravityDrop = (float) (LEVEL_ZERO_FRAMES_PER_DROP * Math.exp(DROP_DECAY_CONSTANT * level));
        framesPerLock = (byte) Math.round(Math.max(32.0, framesPerGravityDrop + 4.0));
    }
    
    private void lockTetrimino() {
        for (final Offset offset : Tetromino.TETROMINOES[tetrominoType][tetrominoRotation].offsets) {
            final int by = tetrominoY + offset.y;
            if (by < 0) {
                continue;
            }
            final int bx = tetrominoX + offset.x;
            playfield[by][bx] = (byte) (tetrominoType + 1);
        }
        findLines();
        if (lineYs.isEmpty()) {
            if (attackRows > 0) {
                mode = ADDING_GARBAGE_MODE;
            } else {
                attemptSpawn();
            }
        } else {
            mode = CLEARING_LINES_MODE;
            resetLineClearTimer();
        }
    }
    
    private boolean testPosition(final int rotation, final int x, final int y) {
        if (y < 0) {
            return false;
        }
        for (final Offset offset : Tetromino.TETROMINOES[tetrominoType][rotation].offsets) {
            final int bx = x + offset.x;
            final int by = y + offset.y;            
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
            lineYs.add((byte) y);
        }
    }
    
    public void handleInputEvent(final byte event) {
        if (mode != TETROMINO_FALLING_MODE) {
            return;
        }
        switch (event) {
            case InputEvent.ROTATE_CCW_PRESSED:
            case InputEvent.ROTATE_CCW_REPEATED:    
                attemptRotateCCW();
                break;
            case InputEvent.ROTATE_CW_PRESSED:
            case InputEvent.ROTATE_CW_REPEATED:    
                attemptRotateCW();               
                break;
            case InputEvent.SHIFT_LEFT_PRESSED:
            case InputEvent.SHIFT_LEFT_REPEATED:
                attemptShiftLeft();
                break;
            case InputEvent.SHIFT_RIGHT_PRESSED:
            case InputEvent.SHIFT_RIGHT_REPEATED:    
                attemptShiftRight();
                break;
            case InputEvent.SOFT_DROP_PRESSED:
                newlySpawened = false;
                attemptSoftDrop();
                break;
            case InputEvent.SOFT_DROP_REPEATED:
                if (!newlySpawened) {
                    attemptSoftDrop();
                }
                break;
        }
    }
    
    private void attemptRotateCCW() {
        if (tetrominoType == Tetromino.O_TYPE) {
            return;
        }

        final int rotation = (tetrominoRotation == 0) ? 3 : tetrominoRotation - 1;
        if (testPosition(rotation, tetrominoX, tetrominoY)) {
            tetrominoRotation = (byte) rotation;
            return;
        }

        final Offset offset = Tetromino.CCW[tetrominoRotation];
        final int x = tetrominoX + offset.x;
        final int y = tetrominoY + offset.y;
        if (testPosition(rotation, x, y)) {
            tetrominoRotation = (byte) rotation;
            tetrominoX = (byte) x;
            tetrominoY = (byte) y;
        }
    }
    
    private void attemptRotateCW() {
        if (tetrominoType == Tetromino.O_TYPE) {
            return;
        }

        final int rotation = (tetrominoRotation == 3) ? 0 : tetrominoRotation + 1;
        if (testPosition(rotation, tetrominoX, tetrominoY)) {
            tetrominoRotation = (byte) rotation;
            return;
        }

        final Offset offset = Tetromino.CW[tetrominoRotation];
        final int x = tetrominoX + offset.x;
        final int y = tetrominoY + offset.y;
        if (testPosition(rotation, x, y)) {
            tetrominoRotation = (byte) rotation;
            tetrominoX = (byte) x;
            tetrominoY = (byte) y;
        }     
    }
    
    private void attemptShiftLeft() {
        final int x = tetrominoX - 1;
        if (testPosition(tetrominoRotation, x, tetrominoY)) {
            tetrominoX = (byte) x;
        }
    }
    
    private void attemptShiftRight() {
        final int x = tetrominoX + 1;
        if (testPosition(tetrominoRotation, x, tetrominoY)) {
            tetrominoX = (byte) x;
        }
    }
    
    private void attemptSoftDrop() {
        final int y = tetrominoY + 1;
        if (testPosition(tetrominoRotation, tetrominoX, y)) {
            tetrominoY = (byte) y;
            gravityDropTimer = framesPerGravityDrop;
            lockTimer = framesPerLock;
            incrementScore(1);
        } else {
            lockTetrimino();
        } 
    }
    
    private void incrementScore(final int value) {
        score = (int) Math.min(MAX_SCORE, ((long) score) + value);
    }
    
    private void attemptGravityDrop() {
        final int y = tetrominoY + 1;
        dropFailed = !testPosition(tetrominoRotation, tetrominoX, y);
        if (!dropFailed) {            
            dropFailed = false;
            tetrominoY = (byte) y;
            gravityDropTimer += framesPerGravityDrop;
            lockTimer = framesPerLock;
        } 
    }
    
    public void update() {
        switch (mode) {
            case TETROMINO_FALLING_MODE:
                updateFallingTetromino();
                break;
            case CLEARING_LINES_MODE:
                updateClearingLines();
                break;
            case ADDING_GARBAGE_MODE:
                updateAddingGarbage();
                break;
            case GAME_OVER_MODE:
                updateGameOver();
                break;
        }
    }
        
    private void updateFallingTetromino() {
        if (dropFailed) {
            attemptGravityDrop();            
            if (dropFailed && --lockTimer < 0) {                
                lockTetrimino();                
            }
            return;
        } 
        
        while (--gravityDropTimer <= 0 && !dropFailed) {            
            attemptGravityDrop();
        }
    }  
    
    private void updateClearingLines() {
        if (--lineClearTimer < 0) {
            clearLines();
            if (attackRows > 0) {
                mode = ADDING_GARBAGE_MODE;
            } else {
                attemptSpawn();
            }
        }
    }
    
    private void updateAddingGarbage() {
        if (attackRows == 0) {
            attemptSpawn();
            return;
        }
        
        --attackRows;
        
        if (garbageCounter == 0) {
            garbageCounter = MOVES_PER_GARBAGE_ROW;
            int x;
            do {
                x = garbageRandomizer.nextInt(PLAYFIELD_WIDTH);
            } while (x == garbageX);
            garbageX = (byte) x;
        } else {
            --garbageCounter;
        }
        
        for (int y = 1; y < PLAYFIELD_HEIGHT; ++y) {
            System.arraycopy(playfield[y], 0, playfield[y - 1], 0, PLAYFIELD_WIDTH);
        }
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            playfield[PLAYFIELD_HEIGHT - 1][x] = (x == garbageX) ? EMPTY_BLOCK : GARBAGE_BLOCK;
        }
    }
    
    private void updateGameOver() {
        if (gameOverTimer < Byte.MAX_VALUE) {
            ++gameOverTimer;
        }
    }
    
    private void clearLines() {
        if (lineYs.isEmpty()) {
            return;
        }
        incrementScore(CLEAR_POINTS[lineYs.size()] * (level + 1));
        opponent.addAttackRows(ATTACK_ROWS[lineYs.size()]);
        lines += lineYs.size(); 
        final int lev = lines / 10;
        if (level != lev) {
            level = (short) lev;
            updateFramesPerConstants();
        }
        for (final int lineY : lineYs) {
            for (int y = lineY; y > 0; --y) {
                System.arraycopy(playfield[y - 1], 0, playfield[y], 0, PLAYFIELD_WIDTH);
            }
            Arrays.fill(playfield[0], EMPTY_BLOCK);
        }
        lineYs.clear();
    }
    
    private void attemptSpawn() {
        newlySpawened = true;
        dropFailed = false;
        gravityDropTimer = framesPerGravityDrop;
        lockTimer = framesPerLock;
        updateNexts();
        tetrominoType = nexts.remove(0);
        tetrominoX = SPAWN_X;
        tetrominoY = SPAWN_Y;
        tetrominoRotation = SPAWN_ROTATION;
        gameOverTimer = 0;
        
        mode = testPosition(tetrominoRotation, tetrominoX, tetrominoY) ? TETROMINO_FALLING_MODE : GAME_OVER_MODE;
    }
    
    public void setSeed(final long seed) {
        tetrominoRandomizer.setSeed(seed);
        garbageRandomizer.setSeed(seed);
    }

    public void setOpponent(final MonoGameState opponent) {
        this.opponent = opponent;
    }

    public byte[][] getPlayfield() {
        return playfield;
    }
    
    public List<Byte> getNexts() {
        return nexts;
    }

    public int getAttackRows() {
        return attackRows;
    }
    
    public void addAttackRows(final int rows) {
        setAttackRows(attackRows + rows);
    }

    public void setAttackRows(final int rows) {
        attackRows = (byte) Math.min(PLAYFIELD_HEIGHT, rows);
    }

    public byte getTetrominoType() {
        return tetrominoType;
    }

    public byte getTetrominoRotation() {
        return tetrominoRotation;
    }

    public byte getTetrominoX() {
        return tetrominoX;
    }

    public byte getTetrominoY() {
        return tetrominoY;
    }

    public byte getLockTimer() {
        return lockTimer;
    }

    public byte getMode() {
        return mode;
    }

    public byte getLineClearTimer() {
        return lineClearTimer;
    }

    public List<Byte> getLineYs() {
        return lineYs;
    }

    public byte getGameOverTimer() {
        return gameOverTimer;
    }

    public int getScore() {
        return score;
    }

    public short getLevel() {
        return level;
    }

    public short getLines() {
        return lines;
    }

    public short getWins() {
        return wins;
    }

    public GameState getGameState() {
        return gameState;
    }
}
