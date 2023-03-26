package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class BackExitRenderer {

    public void render(final TextGraphics g, final TerminalSize size, final BackExitState state, final int y) {
        if (state.isEscapeEnabled()) {
            renderBackAndExit(g, size, state, y);
        } else {
            renderExit(g, size, state, y);
        }
    }
    
    private void renderBackAndExit(final TextGraphics g, final TerminalSize size, final BackExitState state, 
            final int y) {
        
        final int x = (size.getColumns() - (21 + Menu.COLUMN_SPACER)) / 2;
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + 4, y, ']');
        g.setCharacter(x + 9 + Menu.COLUMN_SPACER, y, '[');
        g.setCharacter(x + 16 + Menu.COLUMN_SPACER, y, ']');        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
        g.putString(x + 1, y, "Esc");
        g.putString(x + 10 + Menu.COLUMN_SPACER, y, "Ctrl+C");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, 
                state.isEscSelected() ? MenuItemRenderer.SELECTED_COLOR : MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 6, y, "Back");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 18 + Menu.COLUMN_SPACER, y, "Exit");
    }
    
    private void renderExit(final TextGraphics g, final TerminalSize size, final BackExitState state, final int y) {
        
        final int x = (size.getColumns() - 13) / 2;
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + 7, y, ']');       
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
        g.putString(x + 1, y, "Ctrl+C");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + 9, y, "Exit");
    }    
}
