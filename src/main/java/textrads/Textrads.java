package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import textrads.ai.Coordinate;
import textrads.ai.Playfield;
import textrads.ai.SearchChain;
import textrads.netplay.Client;
import textrads.netplay.Server;

public class Textrads {
    
    public static final int FRAMES_PER_SECOND = 60;
    
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    private static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    private final Server server = new Server();
    private final Client client = new Client();
    private final PlayRenderer playRenderer = new PlayRenderer();
    private final InputEventList eventList = new InputEventList();
    
    private final SearchChain searchChain = new SearchChain();
    private final boolean[][] playfield = new boolean[MonoGameState.PLAYFIELD_HEIGHT][MonoGameState.PLAYFIELD_WIDTH];
    private final List<Coordinate> moves = new ArrayList<>();
    private float moveTimer;
    
    public void launch() throws Exception {
        
        InputEventSource.setInputMap(new InputMap()); // TODO LOAD INPUT MAP
        
        try (final Screen screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal())) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            InputSource.setScreen(screen);
                        
            final TextGraphics g = screen.newTextGraphics();
            TerminalSize size = screen.getTerminalSize();
            
            long updateTime = System.nanoTime();
            while (!Terminator.isTerminate()) {
            
                final TerminalSize ts = screen.doResizeIfNecessary​();
                if (ts != null) {
                    size = ts;
                }
                
                int updateFrames = 0;
                while (true) {
                    update();
                    updateTime += NANOS_PER_FRAME;
                    if (updateTime > System.nanoTime()) {
                        break;
                    }
                    if (++updateFrames > MAX_FRAME_SKIPS) {
                        updateTime = System.nanoTime() + NANOS_PER_FRAME;
                        break;
                    }
                }
                
                render(g, size);
                screen.refresh();                 
                
                final long remainingTime = updateTime - System.nanoTime();
                if (remainingTime >= MIN_SLEEP_NANOS) {
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(remainingTime));
                } else {
                    while (updateTime - System.nanoTime() > 0) {                        
                    }
                }
            }            
        } 
        

    }
    
    private void update() {
//        client.update();
//        server.update();
//        
//        final GameState state = GameStateSource.getState();
//        InputEventSource.poll(eventList);
//        for (int i = 0; i < eventList.size(); ++i) {
//            state.handleInputEvent(eventList.get(i), 0);
//        }
//        state.update();

        final MonoGameState state = GameStateSource.getState().getStates()[0];
        
//        System.out.format("%f %d %f%n", state.getFramesPerGravityDrop(), state.getFramesPerLock(), 
//                state.getFramesPerGravityDrop() / 2);
        
        System.out.format("State %f %f %d%n", moveTimer, state.getGravityDropTimer(), state.getLockTimer());
        if (state.isJustSpawned()) { 
            final byte[][] p = state.getPlayfield();
            for (int y = MonoGameState.PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
                for (int x = MonoGameState.PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                    playfield[y][x] = p[y][x] != MonoGameState.EMPTY_BLOCK;
                }
            }                     
            searchChain.search(state.getTetrominoType(), state.getNexts().get(0), playfield, 
                    state.getFramesPerGravityDrop(), state.getFramesPerLock(), state.getFramesPerGravityDrop() / 2);
            if (searchChain.isBestFound()) {
                Playfield.lock(playfield, state.getTetrominoType(), searchChain.getX(), searchChain.getY(), 
                        searchChain.getRotation());
                Playfield.print(playfield);
                searchChain.getMoves(moves);
                System.out.println(moves);
            } else {
                System.out.println("--- game over ---");
                moves.clear();
            }
            moveTimer = state.getFramesPerGravityDrop() / 2;
            System.out.format("moveTimer = %f%n", moveTimer);
        } else if (!moves.isEmpty() && --moveTimer <= 0) {           
            moveTimer += state.getFramesPerGravityDrop() / 2;
            final Coordinate coordinate = moves.remove(0);
            System.out.format("Move %d %f %f, %f %f, %d %d%n", coordinate.inputEvent, moveTimer, coordinate.moveTimer, 
                    state.getGravityDropTimer(), coordinate.gravityDropTimer, state.getLockTimer(), 
                    coordinate.lockTimer);            
            //System.out.println("move: " + moves.get(0) + " " + state.getFramesPerGravityDrop() / 2);
            state.handleInputEvent(coordinate.inputEvent);
        }     
                
        state.update();
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
        playRenderer.render(g, size, GameStateSource.getState().getStates()[0]);
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
