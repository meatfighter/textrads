package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.io.IOException;

public class InputSource {
    
    private final Screen screen;
    
    public InputSource(final Screen screen) {
        this.screen = screen;
    }
    
    public KeyStroke poll() {
        try {
            return screen.pollInput();
        } catch (final IOException ignored) {            
        }
        return null;
    }
    
    public void clear() {
        while (poll() != null) {            
        }
    }
}
