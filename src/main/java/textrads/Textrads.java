package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import textrads.ai.Ai;
import textrads.attractmode.AttractModeRenderer;
import textrads.attractmode.AttractModeState;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;
import textrads.ui.menu.MenuRenderer;
import textrads.ui.menu.Menu;
import textrads.util.TerminalUtil;

public class Textrads {
    
    public static final int FRAMES_PER_SECOND = 60;
    
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    public static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    private final GameRenderer playRenderer = new GameRenderer();
    private final InputEventList eventList = new InputEventList();
    
    private final AttractModeState attractModeState = new AttractModeState();
    private final AttractModeRenderer attractModeRenderer = new AttractModeRenderer();
        
    private final Ai ai = new Ai();
    private float moveTimer;
    private final List<Byte> moves = new ArrayList<>(1024);
    
    private final Menu menuState = new Menu();
    private final MenuRenderer menuRenderer = new MenuRenderer();
    
    public void launch() throws Exception {
        
        InputEventSource.setInputMap(new InputMap()); // TODO LOAD INPUT MAP

        final long seed = ThreadLocalRandom.current().nextLong();
        GameStateSource.getState().init(GameState.Mode.VS_AI, seed, 10, 0, 0, true, 0, 0);     
        
        ai.init(GameStateSource.getState().getMode(), seed, 
                (short) GameStateSource.getState().getStates()[1].getLevel(), 
                0, 
                GameStateSource.getState().getStates()[1].getFloorHeight(), 
                15,
                false); // TODO
        
        try (final Terminal terminal = TerminalUtil.createTerminal();
                final Screen screen = new TerminalScreen(terminal)) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            InputSource.setScreen(screen);
            attractModeState.reset();
            
            // TODO TESTING
            final List<MenuItem> menuItems0 = new ArrayList<>();
            menuItems0.add(new MenuItem("Marathon"));
            menuItems0.add(new MenuItem("Constant Level"));
            menuItems0.add(new MenuItem());
            menuItems0.add(new MenuItem("Garbage Heap"));
            menuItems0.add(new MenuItem("Rising Garbage"));
            
            final List<MenuItem> menuItems1 = new ArrayList<>();
            menuItems1.add(new MenuItem("Three Minutes"));
            menuItems1.add(new MenuItem("Forty Lines"));
            menuItems1.add(new MenuItem());
            menuItems1.add(new MenuItem("No Rotation"));
            menuItems1.add(new MenuItem("Invisible"));
            
            final List<MenuItem> menuItems2 = new ArrayList<>();
            menuItems2.add(new MenuItem("Vs. AI", 'A'));
            menuItems2.add(new MenuItem("Vs. Human", 'H'));
            menuItems2.add(new MenuItem());
            menuItems2.add(new MenuItem());
            menuItems2.add(new MenuItem("Keymapping"));
            
            final List<MenuColumn> menuColumns = new ArrayList<>();
            menuColumns.add(new MenuColumn(menuItems0));
            menuColumns.add(new MenuColumn(menuItems1));
            menuColumns.add(new MenuColumn(menuItems2));
            
            menuState.init("Main", menuColumns);
                        
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

//        {
//            final GameState state = GameStateSource.getState();
//            InputEventSource.poll(eventList);
//            for (int i = 0; i < eventList.size(); ++i) {  
//                state.handleInputEvent(eventList.get(i), 0);
//            }
//        }
//
//        {
//            final MonoGameState state = GameStateSource.getState().getStates()[1];
//            final float framesPerMove = Ai.getFramesPerMove(15);
//            
//            if (state.isJustSpawned()) { 
//                moveTimer = framesPerMove;
//                ai.getMoves(moves, state.getLastAttackRows());
//            } 
//                        
//            --moveTimer;            
//            while (moveTimer <= 0) {
//                moveTimer += framesPerMove;
//                if (moves.isEmpty()) {
//                    state.handleInputEvent(InputEvent.SOFT_DROP_PRESSED);
//                } else {                    
//                    state.handleInputEvent(moves.remove(0));
//                }
//            }                
//        }
//                
//        GameStateSource.getState().update();

// --------------------

//        InputEventSource.clear();
//        titleScreenState.update();
//        recordsState.update();

// --------------------

//        {
//            final GameState state = GameStateSource.getState();
//            InputEventSource.poll(eventList);
//            for (int i = 0; i < eventList.size(); ++i) {
//                state.handleInputEvent(eventList.get(i), 0);
//            }
//        }
//
//        {
//            final MonoGameState state = GameStateSource.getState().getStates()[0];
//            
//            if (state.isJustSpawned()) { 
//                moveTimer = state.getFramesPerGravityDrop() / 2;
//                ai.getMoves(moves, state.getLastAttackRows());
//            } 
//                        
//            --moveTimer;            
//            while (moveTimer <= 0) {
//                moveTimer += state.getFramesPerGravityDrop() / 2;
//                if (moves.isEmpty()) {
//                    state.handleInputEvent(InputEvent.SOFT_DROP_PRESSED);
//                } else {                    
//                    state.handleInputEvent(moves.remove(0));
//                }
//            }                
//        }
//                
//        GameStateSource.getState().update();

// ----------------

      attractModeState.update();

//        menuState.update();
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
//        playRenderer.render(g, size, GameStateSource.getState());


//        recordsRender.render(g, size, recordsState);

        attractModeRenderer.render(g, size, attractModeState);

//        menuRenderer.render(g, size, menuState);
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
