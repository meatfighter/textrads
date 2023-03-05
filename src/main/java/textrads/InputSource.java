package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import java.io.IOException;

public final class InputSource {
    
    public static final int MAX_POLLS = 1024;
    
    private static Screen screen;
    
    public static synchronized void setScreen(final Screen screen) {
        InputSource.screen = screen;
    }
    
    public static synchronized KeyStroke poll() {
        KeyStroke keyStroke = null;
        try {
            keyStroke = screen.pollInput();           
        } catch (final IOException ignored) {            
        }
        if (keyStroke == null) {
            return null;
        }
        if (keyStroke.getKeyType() == KeyType.Character && keyStroke.isCtrlDown()) {
            final char c = keyStroke.getCharacter();
            if (c == 'c' || c == 'C') {
                Terminator.setTerminate(true);
            }
        }
        return keyStroke;
    }
    
    public static synchronized void clear() {
        for (int i = MAX_POLLS - 1; i >= 0 && poll() != null; --i) {
        }
    }
    
    private InputSource() {        
    }
}
