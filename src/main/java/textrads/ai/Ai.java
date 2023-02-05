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
        
    private final boolean[][] playfield = new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    private final List<Byte> nexts = new ArrayList<>();
    private final List<Coordinate> moves = new ArrayList<>(1024);
    
    private short level;
    private short lines;
    private byte attackRows;
    
    private byte garbageX;
    private byte garbageCounter;    

    private volatile int difficulty; // TODO USE THIS
    private volatile boolean running;
    
    public void init(final MonoGameState monoGameState, final long seed, final int difficulty) {
        
        this.difficulty = difficulty;
        tetrominoRandomizer.setSeed(seed);
        garbageRandomizer.setSeed(seed);

        level = monoGameState.getLevel();
        
        updateNexts();
        final byte currentType = nexts.remove(0);        
        searchChain.search(currentType, nexts.get(0), playfield, MonoGameState.getFramesPerGravityDrop(level),
                MonoGameState.getFramesPerLock(level), getFramesPerMove());                
        if (searchChain.isBestFound()) {            
            searchChain.getMoves(moves);
            final int lev = lines / 10;
            lines += Playfield.lock(playfield, currentType, searchChain.getX(), searchChain.getY(), 
                    searchChain.getRotation());            
            if (lines / 10 != lev) {
                ++level;
            }
        } else {
            moves.clear();
        }
    }
    
    public void update() {
        // TODO CHECK ON ATTACKS
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
    
    private void updateAddingGarbage() {
        if (attackRows == 0) {
            return;
        }
        
        --attackRows;
        
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
        
        for (int y = 1; y < PLAYFIELD_HEIGHT; ++y) {
            System.arraycopy(playfield[y], 0, playfield[y - 1], 0, PLAYFIELD_WIDTH);
        }
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            playfield[PLAYFIELD_HEIGHT - 1][x] = (x == garbageX);
        }
    }    
    
    private void loop() {
        
    }
}
