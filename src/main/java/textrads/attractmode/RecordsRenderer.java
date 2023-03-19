package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class RecordsRenderer {
    
    private static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    private static final TextColor FIRST_COLOR = new TextColor.Indexed(33);
    private static final TextColor SECOND_COLOR = new TextColor.Indexed(160);
    private static final TextColor THIRD_COLOR = Colors.WHITE;
    private static final TextColor FOURTH_COLOR = Colors.GRAY;
    
    private static final TextColor[] RANK_COLORS = { FIRST_COLOR, SECOND_COLOR, THIRD_COLOR, FOURTH_COLOR };
    
    public void render(final TextGraphics g, final TerminalSize size, final RecordsState state) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');        
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);        
        final int oy = (size.getRows() - 23) / 2;
        GraphicsUtil.centerString(g, size, oy, state.getTitle());
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, Colors.GOLD);
        final int ox = GraphicsUtil.centerString(g, size, oy + 2, state.getHeaders());
        
        final String[] recordStrings = state.getRecordStrings();
        final int flashIndex = state.getFlashIndex();
        for (int i = recordStrings.length - 1; i >= 0; --i) {
            GraphicsUtil.setColor(g, BACKGROUND_COLOR, (i == flashIndex) 
                    ? (i == 2 ? Colors.GOLD : Colors.WHITE) : RANK_COLORS[Math.min(3, i)]);
            g.putString(ox, oy + 4 + 2 * i, recordStrings[i]);
        }
    }
}
