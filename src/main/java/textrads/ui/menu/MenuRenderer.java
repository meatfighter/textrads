package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class MenuRenderer {
    
    final TextColor BACKGROUND_COLOR = Colors.BLACK;
    final TextColor ACCELERATOR_COLOR = Colors.WHITE;
    final TextColor DESCRIPTION_COLOR = Colors.GRAY;
    final TextColor BUTTON_COLOR = Colors.GOLD;
    
    public void render(final TextGraphics g, final TerminalSize size, final MenuState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BUTTON_COLOR);
        g.putString(10, 10, "[");
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, ACCELERATOR_COLOR);
        g.putString(12, 10, "J");
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BUTTON_COLOR);
        g.putString(14, 10, "]");
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, ACCELERATOR_COLOR);
        g.putString(16, 10, "J");
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, DESCRIPTION_COLOR);
        g.putString(17, 10, "oin conference");
    }
}
