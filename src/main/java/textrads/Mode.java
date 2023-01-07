package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public interface Mode {
    
    static Mode PLAY = new PlayMode();
    
    void init() throws Exception;
    void update(Controller controller) throws Exception;
    void render(Screen screen, TextGraphics g, TerminalSize size) throws Exception;
    void dispose() throws Exception;
}
