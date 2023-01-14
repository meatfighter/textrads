package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.concurrent.TimeUnit;

public class Textrads {
    
    private static final int FRAMES_PER_SECOND = 60;
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    private static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    public void launch() throws Exception {
        
        final AppState appState = new AppState();
        Mode mode = Mode.PLAY;
        mode.init(appState);
        appState.setMode(mode);
        
        try (final Screen screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal())) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            final TextGraphics textGraphics = screen.newTextGraphics();
            TerminalSize terminalSize = screen.getTerminalSize();
            
            long updateTime = System.nanoTime();
            while (!appState.isTerminate()) {
                
                if (screen.pollInput() != null) {
                    break; // TODO REMOVE THIS
                }
                
                if (terminalSize == null) {
                    final TerminalSize size = screen.doResizeIfNecessary​();
                    if (size != null) {
                        terminalSize = size;
                    }
                }
                if (appState.getMode() != mode) {
                    mode.dispose(appState);
                    mode = appState.getMode();
                    mode.init(appState);
                }
                int updateFrames = 0;
                while (true) {
                    mode.update(appState);
                    updateTime += NANOS_PER_FRAME;
                    if (updateTime > System.nanoTime()) {
                        break;
                    }
                    if (++updateFrames > MAX_FRAME_SKIPS) {
                        updateTime = System.nanoTime() + NANOS_PER_FRAME;
                        break;
                    }
                }
                
                mode.render(appState, screen, textGraphics, terminalSize);
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
    

    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
