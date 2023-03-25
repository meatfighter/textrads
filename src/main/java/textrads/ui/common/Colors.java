package textrads.ui.common;

import com.googlecode.lanterna.TextColor;

public abstract class Colors {
    public static final TextColor BLACK = new TextColor.Indexed(16);
    public static final TextColor GRAY = new TextColor.Indexed(248);
    public static final TextColor WHITE = new TextColor.Indexed(231);
    public static final TextColor GOLD = new TextColor.Indexed(220);
    public static final TextColor RED = new TextColor.Indexed(196);
    public static final TextColor ORANGE = new TextColor.Indexed(208);
    public static final TextColor YELLOW = new TextColor.Indexed(226);
    public static final TextColor GREEN = new TextColor.Indexed(46);
    public static final TextColor CYAN = new TextColor.Indexed(51);
    public static final TextColor BLUE = new TextColor.Indexed(21);
    
    public static final TextColor[] GRAYS = new TextColor[18];
    static {
        GRAYS[0] = BLACK;
        for (int i = 0; i < 16; ++i) {
            GRAYS[i + 1] = new TextColor.Indexed(232 + i);
        }
        GRAYS[17] = WHITE;
    }
}
