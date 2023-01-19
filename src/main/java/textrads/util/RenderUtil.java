package textrads.util;

import com.googlecode.lanterna.graphics.TextGraphics;

public final class RenderUtil {

    public static void putIntRight(final TextGraphics g, final int x, final int y, final int value) {
        final String str = String.format(" %d", value);
        g.putString(x + 1 - str.length(), y, str);
    }
    
    private RenderUtil() {        
    }
}
