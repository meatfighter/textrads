package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class BackExitRenderer {

    public void render(final TextGraphics g, final TerminalSize size, final int y) {
        
        final int x = (size.getColumns() - (21 + Menu.COLUMN_SPACER)) / 2;
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + 4, y, ']');
        g.setCharacter(x + 9 + Menu.COLUMN_SPACER, y, '[');
        g.setCharacter(x + 16 + Menu.COLUMN_SPACER, y, ']');        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
        g.putString(x + 1, y, "Esc");
        g.putString(x + 10 + Menu.COLUMN_SPACER, y, "Ctrl+C");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 6, y, "Back");
        g.putString(x + 18 + Menu.COLUMN_SPACER, y, "Exit");
    }
}
