package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;

public abstract class MonoGameRenderer {
    
    static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    static final TextColor EMPTY_COLOR = Colors.BLACK;
    static final TextColor LINE_COLOR = Colors.WHITE;
    static final TextColor FLASH_COLOR = Colors.WHITE;
    static final TextColor ATTACK_COLOR = new TextColor.Indexed(160);
    static final TextColor LABEL_COLOR = new TextColor.Indexed(248);
    static final TextColor VALUE_COLOR = Colors.WHITE;
    static final TextColor COUNTDOWN_COLOR = Colors.GOLD;
    
    static final TextColor T_COLOR = new TextColor.Indexed(133);
    static final TextColor J_COLOR = new TextColor.Indexed(61);
    static final TextColor Z_COLOR = new TextColor.Indexed(167);
    static final TextColor O_COLOR = new TextColor.Indexed(179);
    static final TextColor S_COLOR = new TextColor.Indexed(149);
    static final TextColor L_COLOR = new TextColor.Indexed(173);
    static final TextColor I_COLOR = new TextColor.Indexed(36);
    
    static final TextColor GARBAGE_COLOR = new TextColor.Indexed(238);
    static final TextColor GAME_OVER_COLOR = new TextColor.Indexed(238);
    
    static final TextColor[] BLOCK_COLORS = {
        EMPTY_COLOR,   // 0
        T_COLOR,       // 1
        J_COLOR,       // 2
        Z_COLOR,       // 3
        O_COLOR,       // 4
        S_COLOR,       // 5
        L_COLOR,       // 6
        I_COLOR,       // 7
        GARBAGE_COLOR, // 8
    };    
    
    public abstract Dimensions getDimensions();
    
    public abstract void render(TextGraphics g, TerminalSize size, MonoGameState monoGameState, 
            int x, int y, boolean showWins, PressEnterState pressEnterState);
}
