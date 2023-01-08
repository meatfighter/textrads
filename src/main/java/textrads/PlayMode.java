package textrads;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {
    
    public static final TextColor BACKGROUND_COLOR = TextColor.Indexed.fromRGB(0x08, 0x08, 0x08);
    public static final TextColor LINE_COLOR = TextColor.Indexed.fromRGB(0xFF, 0xFF, 0xFF);
    
    public static final TextColor T_COLOR = TextColor.Indexed.fromRGB(0xA3, 0x3D, 0x9A);
    public static final TextColor J_COLOR = TextColor.Indexed.fromRGB(0x4E, 0x3D, 0xA3);
    public static final TextColor Z_COLOR = TextColor.Indexed.fromRGB(0xB2, 0x33, 0x3A);
    public static final TextColor O_COLOR = TextColor.Indexed.fromRGB(0xE1, 0xBC, 0x27);
    public static final TextColor S_COLOR = TextColor.Indexed.fromRGB(0x82, 0xB2, 0x31);
    public static final TextColor L_COLOR = TextColor.Indexed.fromRGB(0xB2, 0x62, 0x31);
    public static final TextColor I_COLOR = TextColor.Indexed.fromRGB(0x31, 0xB2, 0x82);
    
    public static final TextColor GARBAGE_COLOR = TextColor.Indexed.fromRGB(0x4A, 0x4A, 0x4A);
    
    public static final TextColor[] BLOCK_COLORS = {
        BACKGROUND_COLOR,  // 0
        T_COLOR,           // 1
        J_COLOR,           // 2
        Z_COLOR,           // 3
        O_COLOR,           // 4
        S_COLOR,           // 5
        L_COLOR,           // 6
        I_COLOR,           // 7
        GARBAGE_COLOR,     // 8
    };
    
    private GameModel gameModel = new GameModel();

    @Override
    public void init(final AppState appState) throws Exception {
    }    
    
    @Override
    public void update(final AppState appState) throws Exception {

    }

    @Override
    public void render(final AppState appState, final Screen screen, final TextGraphics g, final TerminalSize size) 
            throws Exception {        
        
        g.setBackgroundColor(BLOCK_COLORS[0]);
        g.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, size, ' ');
        
        drawGameModel(g, gameModel, 0, 0, false);
    } 
    
    private void drawGameModel(final TextGraphics g, final GameModel model, final int x, final int y, 
            final boolean big) {
        if (big) {
            drawBigGameModel(g, model, x, y);
        } else {
            drawSmallGameModel(g, model, x, y);
        }
    }
    
    private void drawSmallGameModel(final TextGraphics g, final GameModel model, final int x, final int y) {
        
        g.setBackgroundColor(BACKGROUND_COLOR);
        g.setForegroundColor(LINE_COLOR);
        for (int i = 0; i < 21; ++i) {
            g.putString(x, y + i, "\u2503");            
        }
        for (int i = 0; i < 20; ++i) {
            g.putString(x + i + 1, y + 21, "\u2501");
        }
        for (int i = 1; i < 15; ++i) {
            g.putString(x + 21, y + i, "\u2503");
            g.putString(x + 32, y + i, "\u2503");
        }
        for (int i = 16; i < 21; ++i) {
            g.putString(x + 21, y + i, "\u2503");
        }
        for (int i = 21; i < 32; ++i) {
            g.putString(x + i, y, "\u2501");
            g.putString(x + i, y + 15, "\u2501");
        }
        g.putString(x, y, "\u257B");
        g.putString(x, y + 21, "\u2517");
        g.putString(x + 21, y + 21, "\u251B");
        g.putString(x + 21, y, "\u250F");
        g.putString(x + 32, y, "\u2513");
        g.putString(x + 21, y + 15, "\u2523");
        g.putString(x + 32, y + 15, "\u251B");
                
        final int[][] playfield = model.getPlayfield();
        for (int i = GameModel.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = GameModel.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(x + 1 + 2 * j, y + 1 + i, "  ");
            }
        }
        
        // TODO TESTING
        g.setBackgroundColor(I_COLOR);
        for (int i = 0; i < 5; ++i) {
            final int Y = 1 + 3 * i;
            g.putString(23, Y, "        ");
            g.putString(23, Y + 1, "        ");
        }
    }
    
    private void drawBigGameModel(final TextGraphics g, final GameModel model, final int x, final int y) {
        
        g.setBackgroundColor(BACKGROUND_COLOR);
        g.setForegroundColor(LINE_COLOR);
        for (int i = 0; i < 41; ++i) {
            g.putString(x, y + i, "\u2503");            
        }
        for (int i = 0; i < 40; ++i) {
            g.putString(x + i + 1, y + 41, "\u2501");
        }
        for (int i = 1; i < 29; ++i) {
            g.putString(x + 41, y + i, "\u2503");
            g.putString(x + 58, y + i, "\u2503");
        }
        for (int i = 30; i < 42; ++i) {
            g.putString(x + 41, y + i, "\u2503");
        }
        for (int i = 42; i < 58; ++i) {
            g.putString(x + i, y, "\u2501");
            g.putString(x + i, y + 29, "\u2501");
        }
        g.putString(x, y, "\u257B");
        g.putString(x, y + 41, "\u2517");
        g.putString(x + 41, y + 41, "\u251B");
        g.putString(x + 41, y, "\u250F");
        g.putString(x + 58, y, "\u2513");
        g.putString(x + 41, y + 29, "\u2523");
        g.putString(x + 58, y + 29, "\u251B");
                
        final int[][] playfield = model.getPlayfield();
        for (int i = GameModel.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = GameModel.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(x + 1 + 4 * j, y + 1 + 2 * i, "    ");
                g.putString(x + 1 + 4 * j, y + 2 + 2 * i, "    ");
            }
        }
        
        // TODO TESTING
        g.setBackgroundColor(I_COLOR);
        for (int i = 0; i < 5; ++i) {
            final int Y = 1 + 6 * i;
            g.putString(42, Y, "                ");
            g.putString(42, Y + 1, "                ");
            g.putString(42, Y + 2, "                ");
            g.putString(42, Y + 3, "                ");
        }
    }    
    
    @Override
    public void dispose(final AppState appState) throws Exception {
    } 
}
