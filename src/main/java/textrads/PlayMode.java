package textrads;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {

    @Override
    public void init(final AppState appState) throws Exception {
    }    
    
    @Override
    public void update(final AppState appState) throws Exception {

    }

    @Override
    public void render(final AppState appState, final Screen screen, final TextGraphics g, final TerminalSize size) 
            throws Exception {        
        
        g.setBackgroundColor(ANSI.BLACK);
        g.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, size, '\u2510');
    } 
    
    @Override
    public void dispose(final AppState appState) throws Exception {
    } 
}
