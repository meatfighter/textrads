package textrads.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import textrads.play.GameState;
import textrads.play.MonoGameState;
import static textrads.play.MonoGameState.GARBAGE_LINES;
import static textrads.play.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.play.MonoGameState.PLAYFIELD_WIDTH;

public class Ai {
    
    private static final class Solution {
        final boolean[][] playfield = new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
        final List<Byte> moves = new ArrayList<>(1024);
        short level;
        short lines;
    }
    
    private static final double DIFFICULTY_ZERO_FRAMES_PER_MOVE = 52.0;
    private static final double DIFFICULTY_THIRTY_FRAMES_PER_MOVE = 2.0;
    
    private static final double MOVE_DECAY_CONSTANT 
            = Math.log(DIFFICULTY_THIRTY_FRAMES_PER_MOVE / DIFFICULTY_ZERO_FRAMES_PER_MOVE) / 30.0;
    private static final float[] FRAMES_PER_MOVE = new float[256];

    static {
        for (int i = FRAMES_PER_MOVE.length - 1; i >= 0; --i) {
            FRAMES_PER_MOVE[i] = (float) (DIFFICULTY_ZERO_FRAMES_PER_MOVE * Math.exp(MOVE_DECAY_CONSTANT * i));
        }
    }
    
    public static float getFramesPerMove(final int difficulty) {
        return FRAMES_PER_MOVE[Math.min(FRAMES_PER_MOVE.length - 1, difficulty)];
    }    
    
    private final Thread thread = new Thread(this::loop);
    
    private final Random tetrominoRandomizer = new Random();
    private final Random garbageRandomizer = new Random();
    private final SearchChain searchChain = new SearchChain();
        
    private final List<Byte> nexts = new ArrayList<>();
    private final List<Byte> garbageXs = new ArrayList<>();
        
    private byte garbageCounter;
    
    private final List<Coordinate> moves = new ArrayList<>(1024);
    private final boolean[][] currentPlayfield = new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private short currentLevel;
    private short currentLines;
    private byte lockCounter;
    
    private final Solution[] solutions = new Solution[PLAYFIELD_HEIGHT + 1];

    private final Object solutionsMonitor = new Object();
    private final Object searchMonitor = new Object();
    private int requestedAttackRows;
    private int maxAttackRows;
    private boolean searching;
    
    private byte gameMode;
    private int floorHeight;
    private float framesPerMove;
    private boolean findBestMove;
    
    public Ai() {
        for (int i = solutions.length - 1; i >= 0; --i) {
            solutions[i] = new Solution();
        }
    }
    
    public void init(
            final byte gameMode,
            final long seed, 
            final int startingLevel, 
            final int garbageHeight, 
            final int floorHeight,            
            final int difficulty,
            final boolean findBestMove) {
        
        synchronized (searchMonitor) {
            if (thread.getState() == Thread.State.NEW) {
                thread.start();
            }
            requestedAttackRows = 0;
            maxAttackRows = (gameMode == GameState.Mode.VS_AI) ? PLAYFIELD_HEIGHT : 0;
            while (searching) {                                                
                try {
                    searchMonitor.wait();
                } catch (final InterruptedException e) {                    
                }
            } 
        }
        
        synchronized (solutionsMonitor) {
            this.gameMode = gameMode;            
            this.floorHeight = floorHeight;
            framesPerMove = getFramesPerMove(difficulty);
            this.findBestMove = findBestMove;
            currentLevel = (short) startingLevel;
            garbageCounter = 0;
            lockCounter = 0;            
            tetrominoRandomizer.setSeed(seed);
            garbageRandomizer.setSeed(seed);
            garbageXs.clear();
            nexts.clear();
            updateNexts();
            moves.clear();
            Playfield.clearPlayfield(currentPlayfield);
            
            switch (gameMode) {
                case GameState.Mode.GARBAGE_HEAP:
                    currentLines = 25;
                    createGarbageHeap(garbageHeight);
                    break;
                case GameState.Mode.FORTY_LINES:
                    currentLines = 40;
                    break;
                case GameState.Mode.VS_AI:
                    currentLines = 0;                                            
                    updateGarbageXs();                    
                    break;
                default:
                    currentLines = 0;
                    break;
            }
        } 
        
        synchronized (searchMonitor) {
            requestedAttackRows = -1;
            searching = true;
            searchMonitor.notifyAll();
        }        
    }
    
    private void createGarbageHeap(final int garbageHeight) {
        for (int y = PLAYFIELD_HEIGHT - garbageHeight; y < PLAYFIELD_HEIGHT; ++y) {
            System.arraycopy(GARBAGE_LINES[garbageRandomizer.nextInt(GARBAGE_LINES.length)], 0, currentPlayfield[y], 0, 
                    PLAYFIELD_WIDTH);
        } 
    }    
    
    public void getMoves(final List<Byte> moves, final int attackRows) {
        
        synchronized (searchMonitor) {
            requestedAttackRows = attackRows;
            while (searching) {                                                
                try {
                    searchMonitor.wait();
                } catch (final InterruptedException e) {                    
                }
            } 
        }
        
        synchronized (solutionsMonitor) {
            final Solution solution = solutions[attackRows];
            currentLevel = solution.level;
            currentLines = solution.lines;
            moves.clear();
            moves.addAll(solution.moves);
            Playfield.copy(solution.playfield, currentPlayfield);
            nexts.remove(0);
            updateNexts();
            if (gameMode == GameState.Mode.RISING_GARBAGE) {
                conditionallyRaiseGarbage();
            } else if (attackRows > 0) {
                for (int i = 0; i < attackRows; ++i) {
                    garbageXs.remove(0);
                }
                updateGarbageXs();
            }
        }
        
        synchronized (searchMonitor) {
            requestedAttackRows = -1;
            searching = true;
            searchMonitor.notifyAll();
        }
    }
    
    private void loop() {        
        while (true) {
            
            final int maxAttack;
            synchronized (searchMonitor) {
                while (!searching) {
                    try {
                        searchMonitor.wait();
                    } catch (final InterruptedException e) {                    
                    }                    
                }
                maxAttack = maxAttackRows;
            }
            
            for (int attackRows = 0; attackRows <= maxAttack; ++attackRows) {                
                synchronized (searchMonitor) {                    
                    if (requestedAttackRows >= 0) {
                        if (attackRows > requestedAttackRows) {
                            break;
                        }
                        attackRows = requestedAttackRows;
                    }
                }     
                synchronized (solutionsMonitor) {
                    computeMoves(attackRows);
                }
            }

            synchronized (searchMonitor) {
                searching = false;
                searchMonitor.notifyAll();
            }
        }
    }    
    
    private void computeMoves(final int attackRows) {
        final Solution solution = solutions[attackRows];        
        solution.level = currentLevel;
        solution.lines = currentLines;
        solution.moves.clear();
        Playfield.copy(currentPlayfield, solution.playfield);
        addGarbage(solution.playfield, attackRows);
        
        searchChain.search(nexts.get(0), nexts.get(1), gameMode, solution.playfield, floorHeight,
                MonoGameState.getFramesPerGravityDrop(solution.level),
                MonoGameState.getFramesPerLock(solution.level), framesPerMove); 
        
        final boolean found;
        final int x;
        final int y;
        final int rotation;
        final int dropFailed;
        if (findBestMove) {
            found = searchChain.isBestFound();
            x = searchChain.getBestX();
            y = searchChain.getBestY();
            rotation = searchChain.getBestRotation();
            dropFailed = searchChain.getBestDropFailed();
        } else {
            found = searchChain.isSecondBestFound();
            x = searchChain.getSecondBestX();
            y = searchChain.getSecondBestY();
            rotation = searchChain.getSecondBestRotation();
            dropFailed = searchChain.getSecondBestDropFailed();
        }        
        
        if (found) {                        
            searchChain.getMoves(moves, found, x, y, rotation, dropFailed);
            for (final Coordinate move : moves) {
                solution.moves.add(move.inputEvent);
            } 
            final int lines = Playfield.lock(solution.playfield, nexts.get(0), x, y, rotation);
            if (gameMode == GameState.Mode.GARBAGE_HEAP || gameMode == GameState.Mode.FORTY_LINES) {
                solution.lines -= lines;
            } else {
                solution.lines += lines;
            }
            if (!(gameMode == GameState.Mode.CONSTANT_LEVEL 
                    || gameMode == GameState.Mode.GARBAGE_HEAP 
                    || gameMode == GameState.Mode.FORTY_LINES)) {
                final short minLevel = (short) (solution.lines / 10);
                if (minLevel > solution.level) {
                    solution.level = minLevel;
                }
            }
        }      
    }
    
    private void addGarbage(final boolean[][] playfield, final int attackRows) {
        
        if (attackRows == 0) {
            return;
        }
        
        final int shift = PLAYFIELD_HEIGHT - attackRows;
        for (int i = 0; i < shift; ++i) {
            System.arraycopy(playfield[i + attackRows], 0, playfield[i], 0, PLAYFIELD_WIDTH);
        }        
        for (int i = 0; i < attackRows; ++i) {
            final int gx = garbageXs.get(i);
            final boolean[] row = playfield[shift + i];            
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                row[x] = (x != gx);
            }
        }
    }
    
    private void updateNexts() {
        if (nexts.size() < 7) {            
            for (int i = 0; i < 7; ++i) {
                nexts.add((byte) i);
            }
            Collections.shuffle(nexts.subList(nexts.size() - 7, nexts.size()), tetrominoRandomizer);
        }
    } 
    
    private void updateGarbageXs() {
        while (garbageXs.size() < 20) {
            byte garbageX = garbageXs.isEmpty() ? -1 : garbageXs.get(garbageXs.size() - 1);
            if (garbageCounter == 0) {
                garbageCounter = MonoGameState.MOVES_PER_GARBAGE_ROW;
                int x;
                do {
                    x = garbageRandomizer.nextInt(PLAYFIELD_WIDTH);
                } while (x == garbageX);
                garbageX = (byte) x;                
            } else {
                --garbageCounter;
            }
            garbageXs.add(garbageX);
        }
    }
    
    private void conditionallyRaiseGarbage() {
        ++lockCounter;
        final int threshold;
        switch ((currentLines % 60) / 10) {
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
            System.arraycopy(currentPlayfield[y], 0, currentPlayfield[y - 1], 0, PLAYFIELD_WIDTH);
        }
        System.arraycopy(GARBAGE_LINES[garbageRandomizer.nextInt(GARBAGE_LINES.length)], 0, 
                currentPlayfield[PLAYFIELD_HEIGHT - 1], 0, PLAYFIELD_WIDTH);
    }
}
