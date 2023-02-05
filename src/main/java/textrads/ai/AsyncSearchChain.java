package textrads.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import textrads.MonoGameState;
import textrads.Textrads;

public class AsyncSearchChain {
    
    private static final long DEFAULT_TIMEOUT = Textrads.NANOS_PER_FRAME / 2;

    private final SearchChain searchChain = new SearchChain();
    private final boolean[][] playfield = new boolean[MonoGameState.PLAYFIELD_HEIGHT][MonoGameState.PLAYFIELD_WIDTH];
    private final List<Coordinate> moves = new ArrayList<>(1024);
    
    private final Thread thread = new Thread(this::loop);
    
    private int currentType;
    private int nextType;
    private float framesPerGravityDrop;
    private byte framesPerLock;
    private float framesPerMove;
    private boolean searching;
    
    private volatile boolean running = true;
    
    private final Object stateMonitor = new Object();
    private final Object searchMonitor = new Object();
    
    public void init() {
        thread.start();
    }
    
    public void search(final MonoGameState state, final float framesPerMove) {
        
        synchronized (stateMonitor) {
            final byte[][] p = state.getPlayfield();                
            for (int y = MonoGameState.PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
                for (int x = MonoGameState.PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                    playfield[y][x] = p[y][x] != MonoGameState.EMPTY_BLOCK;
                }
            }
            currentType = state.getTetrominoType();
            nextType = state.getNexts().get(0);
            framesPerGravityDrop = state.getFramesPerGravityDrop();
            framesPerLock = state.getFramesPerLock();
            this.framesPerMove = framesPerMove;
            moves.clear();
        }
        
        synchronized (searchMonitor) {
            searching = true;
            searchMonitor.notifyAll();
        }
    }
    
    public boolean isSearching() {
        synchronized (searchMonitor) {
            return searching;
        }
    }
    
    public List<Coordinate> getMoves() {
        return getMoves(DEFAULT_TIMEOUT);
    }
    
    public List<Coordinate> getMoves(final long timeoutNanos) {
        
        synchronized (searchMonitor) {
            final long startTime = System.nanoTime();
            while (searching && running) {
                final long remainingMillis = TimeUnit.NANOSECONDS.toMillis(
                        timeoutNanos - (System.nanoTime() - startTime));
                if (remainingMillis <= 0) {
                    return null;
                }
                try {
                    searchMonitor.wait(remainingMillis);
                } catch (final InterruptedException ignored) {
                }
            }
        }
        
        synchronized (stateMonitor) {
            return moves;
        }
    }
    
    private void loop() {
        while (running) {
            
            synchronized (searchMonitor) {
                if (!searching) {
                    try {
                        searchMonitor.wait();
                    } catch (final InterruptedException ignored) {                
                    }
                    continue;
                }
            }
            
            synchronized (stateMonitor) {
                searchChain.search(currentType, nextType, playfield, framesPerGravityDrop, framesPerLock, 
                        framesPerMove);
                if (searchChain.isBestFound()) {
                    searchChain.getMoves(moves);
                }
            }
            
            synchronized (searchMonitor) {
                searching = false;
                searchMonitor.notifyAll();
            }
        }
    }
    
    public void shutdown() {
        running = false;
        thread.interrupt();
    }
}
