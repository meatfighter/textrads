package textrads.keymap;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class PressKeyScreenRenderer {
    
    private static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor PRESS_COLOR = Colors.WHITE;
    private static final TextColor NAME_COLOR = KeyMapScreenRenderer.KEY_COLOR;
    private static final TextColor HIGHLIGHT_COLOR = Colors.WHITE;
    
    private static final String PRESS_MESSAGE = "Press";
    
    public void render(final TextGraphics g, final TerminalSize size, final PressKeyScreenState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');        
        
        final String name = state.getName();
        
        final int ox = (size.getColumns() - PRESS_MESSAGE.length() - name.length() - 1) / 2;
        final int oy = (size.getRows() - 1) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, PRESS_COLOR);
        g.putString(ox, oy, PRESS_MESSAGE);
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, state.isHighlight() ? HIGHLIGHT_COLOR : NAME_COLOR);
        g.putString(ox + PRESS_MESSAGE.length() + 1, oy, state.getName());
    }
}
