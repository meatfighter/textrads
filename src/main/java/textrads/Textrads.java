package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import textrads.ai.Ai;
import textrads.netplay.Client;
import textrads.netplay.Server;
import textrads.util.GraphicsUtil;

public class Textrads {
    
    public static final int FRAMES_PER_SECOND = 60000; // TODO 60
    
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    public static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    private final Server server = new Server();
    private final Client client = new Client();
    private final PlayRenderer playRenderer = new PlayRenderer();
    private final InputEventList eventList = new InputEventList();
        
    private final Ai ai = new Ai();
    private float moveTimer;
    private List<Byte> moves = new ArrayList<>(1024);
    
    public void launch() throws Exception {
        
        InputEventSource.setInputMap(new InputMap()); // TODO LOAD INPUT MAP
        
        GameStateSource.getState().setPlayers((byte) 2); // TODO TESTING AI
        final long seed = 42;   // 18 //ThreadLocalRandom.current().nextLong();
        GameStateSource.getState().setSeed(seed);                
        ai.reset((short) GameStateSource.getState().getStates()[1].getLevel(), seed, 0); // TODO DIFFICULTY
        
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
        
        System.exit(0);
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

// --------------------

        {
            final GameState state = GameStateSource.getState();
            InputEventSource.poll(eventList);
            for (int i = 0; i < eventList.size(); ++i) {
                state.handleInputEvent(eventList.get(i), 0);
            }
        }

        {
            final MonoGameState state = GameStateSource.getState().getStates()[1];
            
            if (state.isJustSpawned()) { 
                System.out.println("just spawned: " + state.getMode());
                moveTimer = state.getFramesPerGravityDrop() / 2;
                ai.getMoves(moves, state.getLastAttackRows(), state); // TODO TESTING
            } 
                        
            --moveTimer;            
            while (moveTimer <= 0) {
                moveTimer += state.getFramesPerGravityDrop() / 2;
                if (moves.isEmpty()) {
                    state.handleInputEvent(InputEvent.SOFT_DROP_PRESSED);
                } else {                    
                    state.handleInputEvent(moves.remove(0));
                }
            }                
        }
                
        GameStateSource.getState().update();

// --------------------

        
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
        playRenderer.render(g, size, GameStateSource.getState());
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
