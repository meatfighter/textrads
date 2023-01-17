package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {
    
    public static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(0);

    private final MonoGameRenderer bigRenderer = new BigMonoGameRenderer();
    private final MonoGameRenderer smallRenderer = new SmallMonoGameRenderer();
    
    private MonoGameState state = new MonoGameState();

    @Override
    public void init(final App app) throws Exception { 
        state.init(); // TODO ENHANCE
    }    
    
    @Override
    public void update(final App app) throws Exception {
        
        // TODO ENHANCE
        app.getEventSuppliers()[0].update(app);
        state.handleEvents(app.getEventSuppliers()[0].get());
    }

    public int index = 0;
    
    @Override
    public void render(final App app, final Screen screen, final TextGraphics g, final TerminalSize size) 
            throws Exception {        
        
        g.setBackgroundColor(BACKGROUND_COLOR);
        g.fill(' ');
        
        final boolean showAttackBar = true;
        MonoGameRenderer renderer = bigRenderer;
        Dimensions dims = renderer.getDimensions(showAttackBar);
        if (dims.getWidth() > size.getColumns() || dims.getHeight() > size.getRows()) {
            renderer = smallRenderer;
            dims = renderer.getDimensions(showAttackBar);            
        }
              
        renderer.render(g, state, (size.getColumns() - dims.getWidth()) / 2, (size.getRows() - dims.getHeight()) / 2, 
                true);
    } 
    
   
    
    @Override
    public void dispose(final App app) throws Exception {
    } 
}
