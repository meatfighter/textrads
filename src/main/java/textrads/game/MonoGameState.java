package textrads.game;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import static java.lang.Math.max;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import textrads.input.InputEvent;
import textrads.ui.common.Offset;
import textrads.app.Tetromino;
import textrads.app.Textrads;

public class MonoGameState implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static interface Mode {
        byte COUNTDOWN = 0;
        byte SPAWN = 1;
        byte TETROMINO_FALLING = 2;
        byte CLEARING_LINES = 3;
        byte ADDING_ATTACK_GARBAGE = 4;        
        byte END = 5;
    }
    
    private static final long MAX_SCORE = 999_999_999L;
    
    private static final int FRAMES_PER_THREE_MINUTES = Textrads.FRAMES_PER_SECOND * 60 * 3;
    
    private static final int[] CLEAR_POINTS = { 0, 40, 100, 300, 1200 };
    private static final int[] ATTACK_ROWS = { 0, 0, 1, 2, 4 };
    
    public static final int PLAYFIELD_WIDTH = 10;
    public static final int PLAYFIELD_HEIGHT = 20;
    
    public static final int SPAWN_X = 4;
    public static final int SPAWN_Y = 0;
    public static final int SPAWN_ROTATION = 0;
    
    public static final byte EMPTY_BLOCK = 0;
    public static final byte GARBAGE_BLOCK = 8;
    
    public static final int MOVES_PER_GARBAGE_ROW = 8;
    
    private static final double LEVEL_ZERO_FRAMES_PER_DROP = 52.0;
    private static final double LEVEL_THIRTY_FRAMES_PER_DROP = 2.0;
    
    private static final double DROP_DECAY_CONSTANT 
            = Math.log(LEVEL_THIRTY_FRAMES_PER_DROP / LEVEL_ZERO_FRAMES_PER_DROP) / 30.0;
    private static final float[] FRAMES_PER_GRAVITY_DROP = new float[256];
    private static final byte[] FRAMES_PER_LOCK = new byte[256];
    
    public static final boolean[][] GARBAGE_LINES;
    
    static {
        for (int i = FRAMES_PER_GRAVITY_DROP.length - 1; i >= 0; --i) {
            FRAMES_PER_GRAVITY_DROP[i] = (float) (LEVEL_ZERO_FRAMES_PER_DROP * Math.exp(DROP_DECAY_CONSTANT * i));
            FRAMES_PER_LOCK[i] = (byte) Math.round(Math.max(32f, FRAMES_PER_GRAVITY_DROP[i] + 4f));
        }
        
        boolean[][] garbageLines = null;
        try (final InputStream is = MonoGameState.class.getResourceAsStream("/garbage/garbage.dat");
                final BufferedInputStream bis = new BufferedInputStream(is);
                final DataInputStream dis = new DataInputStream(bis)) {
            
            final int packedLines = dis.readInt();
            garbageLines = new boolean[3 * packedLines][10];
            for (int i = packedLines - 1, g = 0; i >= 0; --i) {
                int bits = dis.readInt();
                for (int j = 2; j >= 0; --j) {
                    final boolean[] line = garbageLines[g++];
                    for (int k = line.length - 1; k >= 0; --k) {
                        line[k] = (bits & 1) == 1;
                        bits >>= 1;
                    }
                }
            }
            
        } catch (final IOException ignored) {
        } finally {
            GARBAGE_LINES = garbageLines;
        }
    }
    
    public static float getFramesPerGravityDrop(final int level) {
        return FRAMES_PER_GRAVITY_DROP[Math.min(FRAMES_PER_GRAVITY_DROP.length - 1, level)];
    }
    
    public static byte getFramesPerLock(final int level) {
        return FRAMES_PER_LOCK[Math.min(FRAMES_PER_LOCK.length - 1, level)];
    }    
    
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
    private byte wins;
    private byte attackRows;
    private byte lastAttackRows;
    
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
    
    private boolean justSpawned;
    private boolean rejectSoftDropRepeated;
    
    private byte garbageX;
    private byte garbageCounter;
    
    private byte countdownTimer;
    private byte countdownValue;
    
    private byte mode;
    
    private byte endTimer;
    private boolean won;
    
    private byte floorHeight;
    
    private byte lockCounter;
    
    private int updates;
    
    private transient boolean localPlayer;
    
    public MonoGameState(final GameState gameState) {
        this.gameState = gameState;        
    }
    
    public void init(
            final long seed, 
            final int startingLevel,
            final int garbageHeight, 
            final int floorHeight,
            final boolean skipCountdown,
            final int wins) {
        
        attackRows = 0;
        lastAttackRows = 0;
        score = 0;
        level = (short) startingLevel;
        tetrominoType = 0;
        tetrominoRotation = 0;
        tetrominoX = 0;
        tetrominoY = 0;
        framesPerGravityDrop = getFramesPerGravityDrop(startingLevel);       
        gravityDropTimer = 0;
        framesPerLock = getFramesPerLock(startingLevel);
        lockTimer = 0;
        dropFailed = false;
        lineClearTimer = 0;
        endTimer = 0;
        won = false;
        justSpawned = false;
        rejectSoftDropRepeated = false;
        garbageX = -1;
        garbageCounter = 0;
        lockCounter = 0;
        this.wins = (byte) wins;        
        if (skipCountdown) {
            countdownTimer = 0;
            countdownValue = 0;
            mode = Mode.SPAWN;
        } else {
            countdownTimer = (byte) Textrads.FRAMES_PER_SECOND;
            countdownValue = 3;            
            mode = Mode.COUNTDOWN;
        }        
        this.floorHeight = (byte) floorHeight;
        updates = (gameState.getMode() == GameState.Mode.THREE_MINUTES) ? FRAMES_PER_THREE_MINUTES : 0; 
        
        lineYs.clear();
        
        tetrominoRandomizer.setSeed(seed);
        garbageRandomizer.setSeed(seed);
        nexts.clear();
        updateNexts();        
                
        for (int y = PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
            Arrays.fill(playfield[y], (byte) 0);
        }
        
        switch (gameState.getMode()) {
            case GameState.Mode.GARBAGE_HEAP:
                lines = 25;
                createGarbageHeap(garbageHeight);
                break;
            case GameState.Mode.FORTY_LINES:
                lines = 40;
                break;
            default:
                lines = 0;
                break;
        }
    }
    
    private void createGarbageHeap(final int garbageHeight) {
        for (int y = PLAYFIELD_HEIGHT - garbageHeight; y < PLAYFIELD_HEIGHT; ++y) {
            final byte[] playfieldRow = playfield[y];
            final boolean[] garbageRow = GARBAGE_LINES[garbageRandomizer.nextInt(GARBAGE_LINES.length)];
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                if (garbageRow[x]) {
                    playfieldRow[x] = GARBAGE_BLOCK;
                }
            }
        } 
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
    
    private void lockTetrimino() {
        ++lockCounter;
        lastAttackRows = 0;
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
            conditionallyRaiseGarbage();
            if (attackRows > 0) {                
                mode = Mode.ADDING_ATTACK_GARBAGE;
            } else {
                mode = Mode.SPAWN;
            }
        } else {
            mode = Mode.CLEARING_LINES;
            resetLineClearTimer();
        }
    }
    
    private void conditionallyRaiseGarbage() {
        
        if (gameState.getMode() != GameState.Mode.RISING_GARBAGE) {
            return;
        }
        
        final int threshold;
        switch ((lines % 60) / 10) {
            case 0:
                threshold = 6;
                break;
            case 2:
                threshold = 16;
                break;
            case 4:
                threshold = 12;
                break;
            default:
                lockCounter = 0;
                return;
        }
        if (lockCounter < threshold) {
            return;
        }

        lockCounter = 0;
        for (int y = 1; y < PLAYFIELD_HEIGHT; ++y) {
            System.arraycopy(playfield[y], 0, playfield[y - 1], 0, PLAYFIELD_WIDTH);
        }
        final boolean[] garbageRow = GARBAGE_LINES[garbageRandomizer.nextInt(GARBAGE_LINES.length)];
        final byte[] playfieldRow = playfield[PLAYFIELD_HEIGHT - 1];
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            playfieldRow[x] = garbageRow[x] ? GARBAGE_BLOCK : EMPTY_BLOCK;
        }
    }
    
    private boolean testPosition(final int rotation, final int x, final int y) {
        if (y < 0) {
            return false;
        }
        final Tetromino tetromino = Tetromino.TETROMINOES[tetrominoType][rotation];
        if (!tetromino.validPosition[floorHeight][y + 2][x + 2]) {
            return false;
        }
        for (final Offset offset : tetromino.offsets) {
            final int by = y + offset.y;            
            if (by >= 0 && playfield[by][x + offset.x] != EMPTY_BLOCK) {
                return false;
            }
        }
        return true;
    }
    
    private void findLines() {
        final Tetromino tetromino = Tetromino.TETROMINOES[tetrominoType][tetrominoRotation];
        final int maxY = tetrominoY + tetromino.maxOffsetY;
        outer: for (int y = max(0, tetrominoY + tetromino.minOffsetY); y <= maxY; ++y) {
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                if (playfield[y][x] == EMPTY_BLOCK) {
                    continue outer;
                }
            }
            lineYs.add((byte) y);
        }
    }
    
    public void handleInputEvent(final byte event) {
        if (mode != Mode.TETROMINO_FALLING) {
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
                rejectSoftDropRepeated = false;
                attemptSoftDrop();
                break;
            case InputEvent.SOFT_DROP_REPEATED:
                if (!rejectSoftDropRepeated) {
                    attemptSoftDrop();
                }
                break;
        }
    }
    
    private void attemptRotateCCW() {
        if (tetrominoType == Tetromino.O_TYPE || gameState.getMode() == GameState.Mode.NO_ROTATION) {
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
        if (tetrominoType == Tetromino.O_TYPE || gameState.getMode() == GameState.Mode.NO_ROTATION) {
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
            dropFailed = false;
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
        justSpawned = false;
        if (!(mode == Mode.COUNTDOWN || mode == Mode.END)) {
            if (gameState.getMode() == GameState.Mode.THREE_MINUTES) {
                if (--updates == 0) {
                    setWon(true);
                }
            } else {
                ++updates;
            }
        }
        switch (mode) {
            case Mode.COUNTDOWN:
                updateCountdown();
                break;
            case Mode.SPAWN:
                attemptSpawn();
                break;
            case Mode.TETROMINO_FALLING:
                updateFallingTetromino();
                break;
            case Mode.CLEARING_LINES:
                updateClearingLines();
                break;
            case Mode.ADDING_ATTACK_GARBAGE:
                updateAddingAttackGarbage();
                break;
            case Mode.END:
                updateEnd();
                break;
        }
    }
    
    private void updateCountdown() {
        if (--countdownTimer <= 0) {
            countdownTimer = (byte) Textrads.FRAMES_PER_SECOND;
            if (--countdownValue < 0) {
                mode = Mode.SPAWN;
            } 
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
        
        --gravityDropTimer;
        
        while (!dropFailed && gravityDropTimer <= 0) {
            attemptGravityDrop();
        }
    }  
    
    private void updateClearingLines() {
        if (--lineClearTimer < 0) {
            clearLines();
            if (lines == 0 && (gameState.getMode() == GameState.Mode.GARBAGE_HEAP 
                    || gameState.getMode() == GameState.Mode.FORTY_LINES)) {
                setWon(true);
                return;
            }
            conditionallyRaiseGarbage();
            if (attackRows > 0) {
                mode = Mode.ADDING_ATTACK_GARBAGE;
            } else {
                mode = Mode.SPAWN;
            }
        }
    }
    
    private void updateAddingAttackGarbage() {
        if (attackRows == 0) {            
            mode = Mode.SPAWN;
            return;
        }
        
        --attackRows;
        ++lastAttackRows;
        
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
    
    private void updateEnd() {
        if (endTimer < Byte.MAX_VALUE) {
            ++endTimer;
        }
    }
    
    private void clearLines() {
        if (lineYs.isEmpty()) {
            return;
        }
        incrementScore(CLEAR_POINTS[lineYs.size()] * (level + 1));
        opponent.addAttackRows(ATTACK_ROWS[lineYs.size()]);
        
        final byte gameMode = gameState.getMode();
        if (gameMode == GameState.Mode.GARBAGE_HEAP || gameMode == GameState.Mode.FORTY_LINES) {
            lines -= lineYs.size();
            if (lines <= 0) {
                lines = 0;                
            }
        } else {
            lines += lineYs.size();
        }
        
        if (!(gameMode == GameState.Mode.CONSTANT_LEVEL 
                || gameMode == GameState.Mode.GARBAGE_HEAP 
                || gameMode == GameState.Mode.FORTY_LINES)) {
            final short minLevel = (short) (lines / 10);
            if (minLevel > level) {
                level = minLevel;
                framesPerGravityDrop = getFramesPerGravityDrop(level);
                framesPerLock = getFramesPerLock(level);
            }
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
        rejectSoftDropRepeated = true;
        dropFailed = false;
        gravityDropTimer = framesPerGravityDrop;
        lockTimer = framesPerLock;
        updateNexts();
        tetrominoType = nexts.remove(0);
        tetrominoX = SPAWN_X;
        tetrominoY = SPAWN_Y;
        tetrominoRotation = SPAWN_ROTATION;
        endTimer = 0; 
        justSpawned = testPosition(tetrominoRotation, tetrominoX, tetrominoY);
        if (justSpawned) {
            mode = Mode.TETROMINO_FALLING;
        } else {
            setWon(false);
            opponent.setWon(true);
        }
    }
    
    public boolean isPausible() {
        switch (mode) {
            case Mode.SPAWN:
            case Mode.TETROMINO_FALLING:
            case Mode.CLEARING_LINES:
            case Mode.ADDING_ATTACK_GARBAGE:            
                return true;
            default:
                return false;
        }
    }

    public MonoGameState getOpponent() {
        return opponent;
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

    public byte getLastAttackRows() {
        return lastAttackRows;
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

    public float getGravityDropTimer() {
        return gravityDropTimer;
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

    public byte getEndTimer() {
        return endTimer;
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

    public byte getWins() {
        return wins;
    }

    public GameState getGameState() {
        return gameState;
    }

    public float getFramesPerGravityDrop() {
        return framesPerGravityDrop;
    }

    public byte getFramesPerLock() {
        return framesPerLock;
    }

    public boolean isJustSpawned() {
        return justSpawned;
    }
    
    public int getCountdownValue() {
        return countdownValue;
    }

    public int getUpdates() {
        return updates;
    }

    public byte getFloorHeight() {
        return floorHeight;
    }
    
    public void setWon(final boolean won) {
        this.won = won;
        if (won) {
            ++wins;
        }
        mode = Mode.END;
    }    

    public boolean isWon() {
        return won;
    }
    
    public boolean isEnd() {
        return mode == Mode.END && (won || endTimer >= 110);
    }

    public boolean isLocalPlayer() {
        return localPlayer;
    }

    public void setLocalPlayer(final boolean localPlayer) {
        this.localPlayer = localPlayer;
    }
}
