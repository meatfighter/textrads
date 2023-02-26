package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.BlockText;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class TitleScreenRenderer {

    private static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor PRESS_START_COLOR = Colors.WHITE;
    private static final TextColor TITLE_COLOR = Colors.GOLD;
    private static final TextColor COPYRIGHT_COLOR = new TextColor.Indexed(248);
    
    private static final String PRESS_ENTER_STRING = "PRESS ENTER";
    private static final String COPYRIGHT_STRING = "\u00A9 2023 meatfighter.com";
    private static final String LICENSE_STRING = "This is free software licensed under GPLv3.";
    
    public void render(final TextGraphics g, final TerminalSize size, final TitleScreenState state) {

        final boolean small = GraphicsUtil.isSmallTerminal(size);
        final int titleX = BlockText.computeCenterX(BlockText.TEXTRADS, size, small);
        final int titleY = (2 * size.getRows() / 5) - (small ? 3 : 6);        
        final int copyrightX = (size.getColumns() - LICENSE_STRING.length()) / 2;
        final int copyrightY = size.getRows() - 5;
        final int pressEnterY = (titleY + BlockText.computeHeight(small) + copyrightY) / 2;
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        switch (state.getState()) {
            case TITLE_FALLING: {
                final int landedLines = state.getLandedLines();
                BlockText.draw(BlockText.TEXTRADS, g, titleX, Math.round(state.getFallFraction() * titleY), TITLE_COLOR, 
                        small, 4 - landedLines, 4 - landedLines);                
                BlockText.draw(BlockText.TEXTRADS, g, titleX, titleY, TITLE_COLOR, small, 5 - landedLines, 4);
                break;
            }
                
            case PRESS_ENTER_FLASHING:
                BlockText.draw(BlockText.TEXTRADS, g, titleX, titleY, TITLE_COLOR, small);

                if (state.isFlash()) {
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, PRESS_START_COLOR);
                    GraphicsUtil.centerString(g, size, pressEnterY, PRESS_ENTER_STRING);
                }

                GraphicsUtil.setColor(g, BACKGROUND_COLOR, COPYRIGHT_COLOR);
                g.putString(copyrightX, copyrightY, COPYRIGHT_STRING);
                g.putString(copyrightX, copyrightY + 1, LICENSE_STRING);
                break;                
        }       
    }
}
