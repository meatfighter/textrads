package textrads.ui.message;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class MessageRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    public static final TextColor INFORM_COLOR = new TextColor.Indexed(223);    
    public static final TextColor WAITING_COLOR = new TextColor.Indexed(158);
    public static final TextColor ERROR_COLOR = new TextColor.Indexed(197);
    
    private static final String[] DOTS_STRINGS = new String[MessageState.MAX_DOTS + 1];
    static {
        for (int i = 0; i <= MessageState.MAX_DOTS; ++i) {
            final StringBuilder sb = new StringBuilder();
            for (int j = 0; j < i; ++j) {
                sb.append('.');
            }
            DOTS_STRINGS[i] = sb.toString();
        }
    }    
    
    public void render(final TextGraphics g, final TerminalSize size, final MessageState state, final int oy) {
        
        switch (state.getMessageType()) {
            case INFORM:
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, INFORM_COLOR);
                GraphicsUtil.centerString(g, size, oy, state.getMessage());
                break;
            case WAITING: {
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, WAITING_COLOR);
                g.putString(GraphicsUtil.centerString(g, size, oy, state.getMessage(), MessageState.MAX_DOTS) 
                        + state.getMessage().length(), oy, DOTS_STRINGS[state.getDots()]);
                break;
            }
            case ERROR:
                GraphicsUtil.setColor(g, BACKGROUND_COLOR, ERROR_COLOR);
                GraphicsUtil.centerString(g, size, oy, state.getMessage());
                break;
        }        
    }
}
