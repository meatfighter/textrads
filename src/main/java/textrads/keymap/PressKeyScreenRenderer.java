package textrads.keymap;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class PressKeyScreenRenderer {
    
    private static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor PRESS_COLOR = Colors.WHITE;
    private static final TextColor NAME_COLOR = KeyMapScreenRenderer.ACTION_COLOR;
    private static final TextColor HIGHLIGHT_COLOR = Colors.WHITE;
    private static final TextColor DEFAULT_COLOR = Colors.GRAY;
    
    private static final String PRESS_MESSAGE = "Press";
    private static final String DEFAULT_MESSAGE = "( default:";
    
    public void render(final TextGraphics g, final TerminalSize size, final PressKeyScreenState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');        
        
        final String name = state.getAction();
        
        final int pressX = (size.getColumns() - PRESS_MESSAGE.length() - name.length() - 1) / 2;
        final int pressY = (size.getRows() - 3) / 2;
        final int defaultX = (size.getColumns() - DEFAULT_MESSAGE.length() - state.getDefaultKey().length() - 3) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, PRESS_COLOR);
        g.putString(pressX, pressY, PRESS_MESSAGE);
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, state.isKeyPressed() ? HIGHLIGHT_COLOR : NAME_COLOR);
        g.putString(pressX + PRESS_MESSAGE.length() + 1, pressY, state.getAction());
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, DEFAULT_COLOR);
        g.putString(defaultX, pressY + 2, DEFAULT_MESSAGE);
        g.putString(defaultX + DEFAULT_MESSAGE.length() + 1, pressY + 2, state.getDefaultKey());
        g.setCharacter(defaultX + DEFAULT_MESSAGE.length() + state.getDefaultKey().length() + 2, pressY + 2, ')');
    }
}
