package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.io.IOException;

public final class InputSource {
    
    public static final int MAX_POLLS = 1024;
    
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
        for (int i = MAX_POLLS - 1; i >= 0 && poll() != null; --i) {
        }
    }
    
    private InputSource() {        
    }
}
