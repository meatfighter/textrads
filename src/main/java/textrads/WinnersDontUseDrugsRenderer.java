package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import textrads.util.GraphicsUtil;

public class WinnersDontUseDrugsRenderer {
    
    private static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(25);
    private static final TextColor TEXT_COLOR = new TextColor.Indexed(221);

    public void render(final TextGraphics g, final TerminalSize size) {

        final boolean small = GraphicsUtil.isSmallTerminal(size);
        final TextImage image = small ? Images.SMALL_FBI_SEAL : Images.BIG_FBI_SEAL;
        final int imageHeight = image.getSize().getRows();
        final int imageY = (size.getRows() - (imageHeight + (small ? 4 : 5))) / 2;
        final int textY = imageY + imageHeight + (small ? 1 : 2);

        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TEXT_COLOR);
        g.fill(' '); 
        GraphicsUtil.centerString(g, size, textY, "\u201cWINNERS DON'T USE DRUGS\u201d");
        GraphicsUtil.centerString(g, size, textY + 2, "William S. Sessions, Director, FBI");
        GraphicsUtil.centerImage(g, size, image, imageY);
    }    
}
