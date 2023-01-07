package textrads;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.InputProvider;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {

    private int rotation;
    
    
    @Override
    public void init() throws Exception {
    }    
    
    @Override
    public void update(final Controller controller, final InputProvider inputProvider) throws Exception {
        while (true) {
            final KeyStroke keyStroke = inputProvider.pollInput();
            if (keyStroke == null) {
                break;
            }
            controller.setTerminate(true);
            break;
        }
    }

    @Override
    public void render(final Screen screen, final TextGraphics g, final TerminalSize size) throws Exception {        
        g.setBackgroundColor(ANSI.BLACK);
        g.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, size, ' ');
    }    
}
