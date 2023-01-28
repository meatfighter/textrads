package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public class PlayRenderer {
 
    public static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(0);

    private final MonoGameRenderer bigRenderer = new BigMonoGameRenderer();
    private final MonoGameRenderer smallRenderer = new SmallMonoGameRenderer();

    public void render(final TextGraphics g, final TerminalSize size, final MonoGameState state) {        
        
        g.setBackgroundColor(BACKGROUND_COLOR);
        g.fill(' ');
        
        MonoGameRenderer renderer = bigRenderer;
        Dimensions dims = renderer.getDimensions();
        if (dims.getWidth() > size.getColumns() || dims.getHeight() > size.getRows()) {
            renderer = smallRenderer;
            dims = renderer.getDimensions();            
        }
              
        renderer.render(g, state, (size.getColumns() - dims.getWidth()) / 2, (size.getRows() - dims.getHeight()) / 2,
                false);
    }
}
