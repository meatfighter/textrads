package textrads.netplay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.util.GraphicsUtil;

public class HostPortRenderer {

    public static final TextColor LABEL_COLOR = new TextColor.Indexed(117);
    public static final TextColor SEPARATOR_COLOR = Colors.WHITE;
    public static final TextColor VALUE_COLOR = new TextColor.Indexed(173);

    public void render(final TextGraphics g, final TerminalSize size, final String host, final String port, 
            final int oy) {
        
        final int ox = Math.max(0, (size.getColumns() - (6 + Math.max(host.length(), port.length()))) / 2);
        
        GraphicsUtil.setColor(g, ConnectScreenRenderer.BACKGROUND_COLOR, LABEL_COLOR);
        g.putString(ox, oy, "host");
        g.putString(ox, oy + 1, "port");
        
        GraphicsUtil.setColor(g, ConnectScreenRenderer.BACKGROUND_COLOR, SEPARATOR_COLOR);
        g.setCharacter(ox + 4, oy, ':');
        g.setCharacter(ox + 4, oy + 1, ':');
        
        GraphicsUtil.setColor(g, ConnectScreenRenderer.BACKGROUND_COLOR, VALUE_COLOR);
        g.putString(ox + 6, oy, host);
        g.putString(ox + 6, oy + 1, port);        
    }
}
