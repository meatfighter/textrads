package textrads;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.ANSI;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

public class PlayMode implements Mode {
    
    // 0x1D, 0x1D, 0x1D
    
    public static final TextColor[] BLOCK_COLORS = {
        TextColor.Indexed.fromRGB(0x08, 0x08, 0x08), // background
        TextColor.Indexed.fromRGB(0xA3, 0x3D, 0x9A), // T
        TextColor.Indexed.fromRGB(0x4E, 0x3D, 0xA3), // J
        TextColor.Indexed.fromRGB(0xB2, 0x33, 0x3A), // Z
        TextColor.Indexed.fromRGB(0xE1, 0xBC, 0x27), // O
        TextColor.Indexed.fromRGB(0x82, 0xB2, 0x31), // S
        TextColor.Indexed.fromRGB(0xB2, 0x62, 0x31), // L
        TextColor.Indexed.fromRGB(0x31, 0xB2, 0x82), // I
        TextColor.Indexed.fromRGB(0x4A, 0x4A, 0x4A), // garbage
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
        
        drawGameModel(g, gameModel, 0, 0);
    } 
    
    private void drawGameModel(final TextGraphics g, final GameModel model, final int x, final int y) {
        
        g.setBackgroundColor(ANSI.BLACK);
        g.setForegroundColor(ANSI.WHITE_BRIGHT);
        for (int i = 0; i < 21; ++i) {
            g.putString(x, y + i, "\u2503");            
        }
        for (int i = 0; i < 20; ++i) {
            g.putString(x + i + 1, y + 21, "\u2501");
        }
        for (int i = 1; i < 16; ++i) {
            g.putString(x + 21, y + i, "\u2503");
            g.putString(x + 33, y + i, "\u2503");
        }
        for (int i = 17; i < 21; ++i) {
            g.putString(x + 21, y + i, "\u2503");
        }
        for (int i = 21; i < 33; ++i) {
            g.putString(x + i, y, "\u2501");
            g.putString(x + i, y + 16, "\u2501");
        }
        g.putString(x, y, "\u257B");
        g.putString(x, y + 21, "\u2517");
        g.putString(x + 21, y + 21, "\u251B");
        g.putString(x + 21, y, "\u250F");
        g.putString(x + 33, y, "\u2513");
        g.putString(x + 21, y + 16, "\u2523");
        g.putString(x + 33, y + 16, "\u251B");
                
        final int[][] playfield = model.getPlayfield();
        for (int i = GameModel.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = GameModel.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(x + 1 + 2 * j, y + 1 + i, "  ");
            }
        }
    }
    
    @Override
    public void dispose(final AppState appState) throws Exception {
    } 
}
