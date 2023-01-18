package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import java.util.concurrent.TimeUnit;

public class Textrads {
    
    public static final int FRAMES_PER_SECOND = 60;
    
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    private static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    public void launch() throws Exception {
        
        GameEventSource.setInputMap(new InputMap()); // TODO LOAD INPUT MAP
        
        try (final Screen screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal())) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            InputSource.setScreen(screen);
                        
            final TextGraphics g = screen.newTextGraphics();
            TerminalSize size = screen.getTerminalSize();
            
            long updateTime = System.nanoTime();
            while (!app.isTerminate()) {
            
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
        
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
        
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
