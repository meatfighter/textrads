package textrads.keymap;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuRenderer;
import textrads.util.GraphicsUtil;

public class KeyMapScreenRenderer {
    
    public static final TextColor KEY_COLOR = new TextColor.Indexed(117);
    public static final TextColor SEPARATOR_COLOR = Colors.WHITE;
    public static final TextColor VALUE_COLOR = new TextColor.Indexed(173);
    
    private final MenuRenderer menuRenderer = new MenuRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final KeyMapScreenState state) {
        
        menuRenderer.render(g, size, state.getMenu());
        
        final Menu menu = state.getMenu();
        final int ox = (size.getColumns() - state.getWidth()) / 2;
        final int oy = (size.getRows() - menu.getHeight()) / 2;
                
        final String[] values = state.getValues();
        final int colonX = KeyMapScreenState.KEYS[3].length() + 1;
        for (int i = KeyMapScreenState.KEYS.length - 1; i >= 0; --i) {
            final int y = oy + i + 2;
            final String key = KeyMapScreenState.KEYS[i];
            GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, KEY_COLOR);
            g.putString(ox, y, key);
            GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, SEPARATOR_COLOR);
            g.setCharacter(ox + colonX, y, ':');
            GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, VALUE_COLOR);
            g.putString(ox + colonX + 2, y, values[i]);
        }
    }
}
