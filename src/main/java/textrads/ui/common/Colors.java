package textrads.ui.common;

import com.googlecode.lanterna.TextColor;

public abstract class Colors {
    public static final TextColor BLACK = new TextColor.Indexed(16);
    public static final TextColor GRAY = new TextColor.Indexed(248);
    public static final TextColor WHITE = new TextColor.Indexed(231);
    public static final TextColor GOLD = new TextColor.Indexed(220);
    
    public static final TextColor[] SUCCESS;
    static {
        final byte[] indices = { -96, -59, -58, -56, -55, -91, 93, 57, 21, 21, 20, 27, 32, 45, 51, 50, 48, 47, 46, 46, 
            46, 82, 118, -66, -30, -36, -48, -54, -60, -60, };
        SUCCESS = new TextColor[indices.length];
        for (int i = indices.length - 1; i >= 0; --i) {
            SUCCESS[i] = new TextColor.Indexed(0xFF & indices[i]);
        }
    }
}
