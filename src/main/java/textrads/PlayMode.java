package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {
    
    public static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(0);

    
    private MonoGameState state = new MonoGameState();
    private MonoGameRenderer gameRenderer = new BigMonoGameRenderer();

    @Override
    public void init(final App app) throws Exception {        
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
        
        gameRenderer.render(g, state, 5, 5, true);
    } 
    
   
    
    @Override
    public void dispose(final App app) throws Exception {
    } 
}
