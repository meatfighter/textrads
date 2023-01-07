package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public interface Mode {
    boolean update(Controller controller);
    void render(Screen screen, TextGraphics g, TerminalSize size);
}
