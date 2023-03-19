package textrads.game;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.ui.common.Dimensions;
import textrads.attractmode.PressEnterState;
import textrads.util.GraphicsUtil;

public class GameRenderer {
    
    private static final TextColor PRESS_START_COLOR = Colors.WHITE;
    private static final TextColor PAUSE_COLOR = new TextColor.Indexed(104);
    
    private static final String PRESS_ENTER_STRING = "PRESS ENTER";
    private static final String PAUSE_STRING = "PAUSE";
 
    private final MonoGameRenderer bigRenderer = new BigMonoGameRenderer();
    private final MonoGameRenderer smallRenderer = new SmallMonoGameRenderer();

    public void render(final TextGraphics g, final TerminalSize size, final GameState state, 
            final PressEnterState pressEnterState) {        
        
        GraphicsUtil.setColor(g, MonoGameRenderer.BACKGROUND_COLOR, MonoGameRenderer.BACKGROUND_COLOR);
        g.fill(' ');
        
        if (state.isPaused()) {
            GraphicsUtil.setColor(g, MonoGameRenderer.BACKGROUND_COLOR, PAUSE_COLOR);
            GraphicsUtil.centerString(g, size, (size.getRows() - 1) / 2, PAUSE_STRING);
            return;
        }
        
        final MonoGameRenderer renderer = GraphicsUtil.isSmallTerminal(size) ? smallRenderer : bigRenderer;
        final Dimensions dims = renderer.getDimensions();
        final int width = dims.getWidth();
        final int height = dims.getHeight() + (pressEnterState == null ? 0 : 2);
        
        final MonoGameState[] states = state.getStates();
        final int y = (size.getRows() - height) / 2;
        if (state.getPlayers() == 1) {        
            renderer.render(g, size, states[0], (size.getColumns() - width) / 2, y, false);
        } else {
            final int x = (size.getColumns() - (2 * width + 1)) / 2;            
            renderer.render(g, size, states[0], x, y, true);
            renderer.render(g, size, states[1], x + width + 1, y, true);
        }
        
        if (pressEnterState != null && pressEnterState.isFlash()) {
            GraphicsUtil.setColor(g, MonoGameRenderer.BACKGROUND_COLOR, PRESS_START_COLOR);
            GraphicsUtil.centerString(g, size, (y + height + size.getRows() - 2) / 2, PRESS_ENTER_STRING);
        }
    }
}
