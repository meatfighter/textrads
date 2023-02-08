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
    
    private final Thread thread = new Thread(this::loop);
    
    private final Random tetrominoRandomizer = new Random();
    private final Random garbageRandomizer = new Random();
    private final SearchChain searchChain = new SearchChain();
        
    private final boolean[][][] playfields = new boolean[2][PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Byte> nexts = new ArrayList<>();
    private final List<Byte> garbageXs = new ArrayList<>();
    
    private final List<Coordinate>[] moves = new ArrayList[PLAYFIELD_HEIGHT + 1];
    
    private short level;
    private short lines;
    private byte attackRows;
    
    private byte garbageX = -1;
    private byte garbageCounter;    

    private volatile int difficulty; // TODO USE THIS
    private volatile boolean running;
    
    public Ai() {
        for (int i = moves.length - 1; i >= 0; --i) {
            moves[i] = new ArrayList<>(1024);
        }
    }
    
    public void init(final MonoGameState monoGameState, final long seed, final int difficulty) {
        
        this.difficulty = difficulty;
        tetrominoRandomizer.setSeed(seed);
        garbageRandomizer.setSeed(seed);

        level = monoGameState.getLevel();
        
        updateGarbageXs();
        updateNexts();
        

    }
    
    private void computeMoves() {
        
        for (int attackRows = 0; attackRows <= PLAYFIELD_HEIGHT; ++attackRows) {
            
        }
    }
    
    private void computeMoves(final int attackRows) {
        
        Playfield.copy(playfields[0], playfields[1]);
        addGarbage(playfields[1], attackRows);
        
        // TODO NEED TO RETAIN IS_BEST_FOUND, X, Y, ROTATION, and MOVES
        
        searchChain.search(nexts.get(0), nexts.get(1), playfields[1], MonoGameState.getFramesPerGravityDrop(level),
                MonoGameState.getFramesPerLock(level), getFramesPerMove());                
        if (searchChain.isBestFound()) {            
            searchChain.getMoves(moves[attackRows]);
            final int lev = lines / 10;
            final int lns = Playfield.lock(playfields[1], nexts.get(0), searchChain.getX(), searchChain.getY(), 
                    searchChain.getRotation());            
            if ((lines + lns) / 10 != lev) {
                ++level;
            }
        } else {
            moves[attackRows].clear();
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
    
    private float getFramesPerMove() {
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
    
    private void loop() {
        
    }
}
