package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class ContinueExitRenderer {

    public void render(final TextGraphics g, final TerminalSize size, final ContinueExitState state, final int y) {
        
        final int x = (size.getColumns() - (21 + Menu.COLUMN_SPACER)) / 2;
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + 6, y, ']');
        g.setCharacter(x + 15 + Menu.COLUMN_SPACER, y, '[');
        g.setCharacter(x + 22 + Menu.COLUMN_SPACER, y, ']');        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
        g.putString(x + 1, y, "Enter");
        g.putString(x + 16 + Menu.COLUMN_SPACER, y, "Ctrl+C");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, 
                state.isEnterSelected() ? MenuItemRenderer.SELECTED_COLOR : MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 8, y, "Continue");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 24 + Menu.COLUMN_SPACER, y, "Exit");
    }
}
