package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class MenuItemRenderer {
    
    public static final TextColor ACCELERATOR_COLOR = Colors.WHITE;
    public static final TextColor DESCRIPTION_COLOR = Colors.GRAY;
    public static final TextColor BUTTON_COLOR = Colors.GOLD;

    public void render(final TextGraphics g, final TerminalSize size, final MenuItem menuItem, final int x, 
            final int y) {
        
        if (menuItem.isSpacer()) {
            return;
        }
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + 2, y, ']');
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, DESCRIPTION_COLOR);
        final String description = menuItem.getDescription();
        g.putString(x + 4, y, description);
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, ACCELERATOR_COLOR);
        g.setCharacter(x + 1, y, menuItem.getUpperCaseAccelerator());
        final int highlightIndex = menuItem.getHighlightIndex();
        if (highlightIndex >= 0 && highlightIndex < description.length()) {
            g.setCharacter(x + 4 + highlightIndex, y, description.charAt(highlightIndex));
        }
    }
}
