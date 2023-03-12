package textrads.ui.question;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class CongratsScreenRenderer {
    
    private static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    private static final TextColor INITIALS_COLOR = Colors.GOLD;
    
    private final TextFieldRenderer textFieldRenderer = new TextFieldRenderer(INITIALS_COLOR);
    
    public void render(final TextGraphics g, final TerminalSize size, final CongratsScreenState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' '); 
        
        final TextImage image = GraphicsUtil.isSmallTerminal(size) ? state.getSmallImage() : state.getBigImage();        
        final TerminalSize imageSize = image.getSize();
        final int ox = (size.getColumns() - imageSize.getColumns()) / 2;
        final int oy = (size.getRows() - (imageSize.getRows() + 4)) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, state.getTitle());
        
        textFieldRenderer.render(g, size, state.getTextField(), oy + 2);
        
        GraphicsUtil.centerImage(g, size, image, oy + 4);
    }
}
