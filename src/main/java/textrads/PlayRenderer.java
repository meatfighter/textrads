package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class PlayRenderer {
 
    public static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(16);

    private final MonoGameRenderer bigRenderer = new BigMonoGameRenderer();
    private final MonoGameRenderer smallRenderer = new SmallMonoGameRenderer();

    public void render(final TextGraphics g, final TerminalSize size, final GameState state) {        
        
        g.setBackgroundColor(BACKGROUND_COLOR);
        g.fill(' ');
        
        MonoGameRenderer renderer = bigRenderer;
        Dimensions dims = renderer.getDimensions();       
        if (dims.getHeight() > size.getRows()
                || ((state.getPlayers() == 1) ? dims.getWidth() : 2 * dims.getWidth() + 1) > size.getColumns()) {
            renderer = smallRenderer;
            dims = renderer.getDimensions();            
        }
        
        if (state.getPlayers() == 1) {        
            renderer.render(g, state.getStates()[0], (size.getColumns() - dims.getWidth()) / 2, 
                    (size.getRows() - dims.getHeight()) / 2, false);
        } else {
            final int x = (size.getColumns() - (2 * dims.getWidth() + 1)) / 2;
            final int y = (size.getRows() - dims.getHeight()) / 2;
            renderer.render(g, state.getStates()[0], x, y, true);
            renderer.render(g, state.getStates()[1], x + dims.getWidth() + 1, y, true);
        }
    }
}
