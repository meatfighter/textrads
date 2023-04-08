package textrads.ui.menu;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class BackExitRenderer {

    public void render(final TextGraphics g, final TerminalSize size, final BackExitState state, final int y) {
        if (state.isBackEnabled()) {
            renderBackAndExit(g, size, state, y);
        } else {
            renderExit(g, size, state, y);
        }
    }
    
    private void renderBackAndExit(final TextGraphics g, final TerminalSize size, final BackExitState state, 
            final int y) {
        
        final String backKeyName = state.getBackKeyName();
        final String backLabel = state.getBackLabel();
        final int x = (size.getColumns() - (backKeyName.length() + backLabel.length() + Menu.COLUMN_SPACER + 16)) / 2;
        final int xx = x + backKeyName.length() + backLabel.length() + Menu.COLUMN_SPACER + 3;
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
        g.setCharacter(x, y, '[');
        g.setCharacter(x + backKeyName.length() + 1, y, ']');
        
        g.setCharacter(xx, y, '[');
        g.setCharacter(xx + 7, y, ']');
        
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
        g.putString(x + 1, y, backKeyName);
                
        g.putString(xx + 1, y, "Ctrl+C");
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, 
                state.isSelected() ? MenuItemRenderer.SELECTED_COLOR : MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(x + backKeyName.length() + 3, y, backLabel);
        GraphicsUtil.setColor(g, MenuRenderer.BACKGROUND_COLOR, MenuItemRenderer.DESCRIPTION_COLOR);
        g.putString(xx + 9, y, "Exit");
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
