package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class ChooserRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    
    public void render(final TextGraphics g, final TerminalSize size, final Chooser chooser) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
    }
}
