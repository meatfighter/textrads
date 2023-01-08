package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public interface Mode {
    
    static Mode PLAY = new PlayMode();
    
    void init(AppState appState) throws Exception;
    void update(AppState appState) throws Exception;
    void render(AppState appState, Screen screen, TextGraphics g, TerminalSize size) throws Exception;
    void dispose(AppState appState) throws Exception;
}
