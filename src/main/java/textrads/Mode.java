package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public interface Mode {
    
    static Mode PLAY = new PlayMode();
    
    void init(App app) throws Exception;
    void update(App app) throws Exception;
    void render(App app, Screen screen, TextGraphics g, TerminalSize size) throws Exception;
    void dispose(App app) throws Exception;
}
