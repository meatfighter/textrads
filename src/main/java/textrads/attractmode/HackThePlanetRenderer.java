package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import textrads.ui.common.Images;
import textrads.util.GraphicsUtil;

public class HackThePlanetRenderer {
    
    private static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(16);
    private static final TextColor TEXT_COLOR = new TextColor.Indexed(231);

    public void render(final TextGraphics g, final TerminalSize size) {

        final boolean small = GraphicsUtil.isSmallTerminal(size);
        final TextImage image = small ? Images.SMALL_EARTH : Images.BIG_EARTH;
        final int imageHeight = image.getSize().getRows();
        final int imageY = (size.getRows() - (imageHeight + (small ? 4 : 5))) / 2;
        final int textY = imageY + imageHeight + (small ? 1 : 2);

        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TEXT_COLOR);
        g.fill(' '); 
        GraphicsUtil.centerString(g, size, textY, "\u201cHACK THE PLANET\u201d");
        GraphicsUtil.centerString(g, size, textY + 2, "Dade \u201cZero Cool\u201d Murphy, Hackers");
        GraphicsUtil.centerImage(g, size, image, imageY);
    }    
}
