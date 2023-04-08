package textrads.ui.message;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.ui.menu.BackExitRenderer;
import textrads.util.GraphicsUtil;

public class MessageScreenRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    public static final TextColor TITLE_COLOR = Colors.WHITE; 
    
    private final MessageRenderer messageRenderer = new MessageRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();    
    
    public void render(final TextGraphics g, final TerminalSize size, final MessageScreen messageScreen) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');

        final int oy = (size.getRows() - 7) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, messageScreen.getTitle());

        messageRenderer.render(g, size, messageScreen.getMessageState(), oy + 3);

        backExitRenderer.render(g, size, messageScreen.getBackExitState(), oy + 6);
    }
}
