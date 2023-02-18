package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.io.IOException;

public final class InputSource {
    
    private static final int MAX_POLLS = 1024;
    
    private static Screen screen;
    
    public static synchronized void setScreen(final Screen screen) {
        InputSource.screen = screen;
    }
    
    public static synchronized KeyStroke poll() {
        try {
            return screen.pollInput();
        } catch (final IOException ignored) {            
        }
        return null;
    }
    
    public static synchronized void clear() {
        for (int i = 0; i < MAX_POLLS && poll() != null; ++i) {
        }
    }
    
    private InputSource() {        
    }
}
