package textrads.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import textrads.MonoGameState;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;

// TODO THIS WILL REPLACE AsyncSearchChain

public class Ai {
    
    private static final class Solution {
        final boolean[][] playfield = new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
        final List<Coordinate> moves = new ArrayList<>(1024);
        short level;
        short lines;
    }
    
    private final Thread thread = new Thread(this::loop);
    
    private final Random tetrominoRandomizer = new Random();
    private final Random garbageRandomizer = new Random();
    private final SearchChain searchChain = new SearchChain();
        
    private final List<Byte> nexts = new ArrayList<>();
    private final List<Byte> garbageXs = new ArrayList<>();
        
    private byte garbageX = -1;
    private byte garbageCounter;
    
    private boolean[][] currentPlayfield = new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private volatile short currentLevel;
    private short currentLines;
    
    private final Solution[] solutions = new Solution[PLAYFIELD_HEIGHT + 1];

    private final Object searchMonitor = new Object();
    private int requestedAttackRows;
    private boolean searching;
    
    private volatile int difficulty; // TODO USE THIS
    
    public Ai() {
        for (int i = solutions.length - 1; i >= 0; --i) {
            solutions[i] = new Solution();
        }
    }
    
    public void init(final short level, final long seed, final int difficulty) {
        
        this.difficulty = difficulty;
        tetrominoRandomizer.setSeed(seed);
        garbageRandomizer.setSeed(seed);

        currentLevel = level;
        
        updateGarbageXs();
        updateNexts();
        

    }
    
    public void getMoves(final List<Coordinate> moves, final int attackRows) {
        
        synchronized (searchMonitor) {
            requestedAttackRows = attackRows;
            while (searching) {                                                
                try {
                    searchMonitor.wait();
                } catch (final InterruptedException e) {                    
                }
            }
            requestedAttackRows = -1;
        }
        
        final Solution solution = solutions[attackRows];
        currentLevel = solution.level;
        currentLines = solution.lines;
        moves.clear();
        moves.addAll(solution.moves);
        Playfield.copy(solution.playfield, currentPlayfield);
        nexts.remove(0);
        updateNexts();
        if (attackRows > 0) {
            for (int i = 0; i < attackRows; ++i) {
                garbageXs.remove(0);
            }
            updateGarbageXs();
        }
    }
    
    private void loop() {        
        while (true) {
            synchronized (searchMonitor) {
                while (!searching) {
                    try {
                        searchMonitor.wait();
                    } catch (final InterruptedException e) {                    
                    }                    
                }
            }
            
            for (int attackRows = 0; attackRows <= PLAYFIELD_HEIGHT; ++attackRows) {                
                synchronized (searchMonitor) {                    
                    if (requestedAttackRows >= 0) {
                        if (attackRows < requestedAttackRows) {
                            break;
                        }
                        attackRows = requestedAttackRows;
                    }
                }                
                computeMoves(attackRows);
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
        
        searchChain.search(nexts.get(0), nexts.get(1), solution.playfield, 
                MonoGameState.getFramesPerGravityDrop(solution.level),
                MonoGameState.getFramesPerLock(solution.level), getFramesPerMove(solution.level));                
        if (searchChain.isBestFound()) {            
            searchChain.getMoves(solution.moves);
            final int lev = solution.lines / 10;
            solution.lines += Playfield.lock(solution.playfield, nexts.get(0), searchChain.getX(), searchChain.getY(), 
                    searchChain.getRotation());            
            if (solution.lines / 10 != lev) {
                ++solution.level;
            }
        }       
    }
    
    private void addGarbage(final boolean[][] playfield, final int attackRows) {
        
        if (attackRows == 0) {
            return;
        }
        
        final int shift = PLAYFIELD_HEIGHT - attackRows;
        for (int s = shift; s < PLAYFIELD_HEIGHT; ++s) {
            System.arraycopy(playfield[s], 0, playfield[s - shift], 0, PLAYFIELD_WIDTH);
        }        
        for (int i = 0; i < attackRows; ++i) {
            final int y = shift + i;
            final int gx = garbageXs.get(i);
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                playfield[y][x] = (x != gx);
            }
        }
    }
    
    private float getFramesPerMove(final int level) { // TODO ENHANCE
        return MonoGameState.getFramesPerGravityDrop(level) / 2;
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
            if (garbageCounter == 0) {
                garbageCounter = MonoGameState.MOVES_PER_GARBAGE_ROW;
                int x;
                do {
                    x = garbageRandomizer.nextInt(PLAYFIELD_WIDTH);
                } while (x == garbageX);
                garbageXs.add((byte) x);
            } else {
                --garbageCounter;
            }
        }
    }
}
