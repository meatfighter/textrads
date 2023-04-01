package textrads.netplay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuRenderer;

public class NetplayRenderer {
    
    private final MenuRenderer menuRenderer = new MenuRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        switch (state.getState()) {
            case PLAY_AS:
                renderPlayAs(g, size, state.getPlayAsMenu());
                break;
        }
    }
    
    private void renderPlayAs(final TextGraphics g, final TerminalSize size, final Menu playAsMenu) {
        menuRenderer.render(g, size, playAsMenu);
    }
}
