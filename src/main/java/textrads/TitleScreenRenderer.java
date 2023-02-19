package textrads;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class TitleScreenRenderer {

    private static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(16);
    private static final TextColor PRESS_START_COLOR = new TextColor.Indexed(231);
    private static final TextColor TITLE_COLOR = new TextColor.Indexed(220);
    private static final TextColor COPYRIGHT_COLOR = new TextColor.Indexed(248);
    private static final TextColor LINE_COLOR = new TextColor.Indexed(231);
    
    private static final String PRESS_ENTER_STRING = "PRESS ENTER";
    private static final String COPYRIGHT_STRING = "\u00A9 2023 meatfighter.com";
    private static final String LICENSE_STRING = "This is free software licensed under GPLv3.";
    
    public void render(final TextGraphics g, final TerminalSize size, final TitleScreenState state) {

        final boolean small = GraphicsUtil.isSmallTerminal(size);
        final int titleX = BlockText.computeCenterX(BlockText.TEXTRADS, size, small);
        final int titleY = (2 * size.getRows() / 5) - (small ? 3 : 6);
        final int lineY = titleY + BlockText.computeHeight(small);        
        final int copyrightX = (size.getColumns() - LICENSE_STRING.length()) / 2;
        final int copyrightY = size.getRows() - 5;
        
        g.setBackgroundColor(BACKGROUND_COLOR);        
        g.fill(' ');

        g.setForegroundColor(COPYRIGHT_COLOR);
        g.putString(copyrightX, copyrightY, COPYRIGHT_STRING);
        g.putString(copyrightX, copyrightY + 1, LICENSE_STRING);
        
        g.setForegroundColor(PRESS_START_COLOR);
        GraphicsUtil.centerString(g, size, (lineY + copyrightY) / 2, PRESS_ENTER_STRING);
        
//        g.setForegroundColor(LINE_COLOR);
//        final int margin = small ? 2 : 4;
//        final int end = titleX + BlockText.computeWidth(BlockText.TEXTRADS, small) + margin;
//        for (int x = titleX - margin; x <= end; ++x) {
//            g.setCharacter(x, lineY, Symbols.SINGLE_LINE_HORIZONTAL);
//        }
        
        BlockText.draw(BlockText.TEXTRADS, g, titleX, titleY, TITLE_COLOR, small);        
    }
}
