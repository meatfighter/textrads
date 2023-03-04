package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class GameRenderer {
 
    private final MonoGameRenderer bigRenderer = new BigMonoGameRenderer();
    private final MonoGameRenderer smallRenderer = new SmallMonoGameRenderer();

    public void render(final TextGraphics g, final TerminalSize size, final GameState state) {        
        
        GraphicsUtil.setColor(g, MonoGameRenderer.BACKGROUND_COLOR, MonoGameRenderer.BACKGROUND_COLOR);
        g.fill(' ');
        
        MonoGameRenderer renderer = bigRenderer;
        Dimensions dims = renderer.getDimensions();       
        if (GraphicsUtil.isSmallTerminal(size)) {
            renderer = smallRenderer;
            dims = renderer.getDimensions();            
        }
        
        final MonoGameState[] states = state.getStates();
        if (state.getPlayers() == 1) {        
            renderer.render(g, size, states[0], (size.getColumns() - dims.getWidth()) / 2, 
                    (size.getRows() - dims.getHeight()) / 2, false);
        } else {
            final int x = (size.getColumns() - (2 * dims.getWidth() + 1)) / 2;
            final int y = (size.getRows() - dims.getHeight()) / 2;
            renderer.render(g, size, states[0], x, y, true);
            renderer.render(g, size, states[1], x + dims.getWidth() + 1, y, true);
        }
    }
}
