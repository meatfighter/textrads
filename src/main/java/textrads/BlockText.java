package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public final class BlockText {
    
    public static final int GO = 0;
    public static final int ONE = 1;
    public static final int TWO = 2;
    public static final int THREE = 3;
    public static final int TEXTRADS = 4;
    
    private static final String[] STRINGS = {        
        "XXX XXX X",
        "X   X X X",
        "X   X X X",
        "X X X X  ",
        "XXX XXX X",

        "X",
        "X",
        "X",
        "X",
        "X",        
               
        "XXX",
        "  X",
        "XXX",
        "X  ",
        "XXX",
        
        "XXX",
        "  X",
        " XX",
        "  X",
        "XXX",
        
        "XXX XXX X X XXX XX   X  XX   XX",
        " X  X   X X  X  X X X X X X X  ",
        " X  XX   X   X  XX  XXX X X  X ",
        " X  X   X X  X  X X X X X X   X",
        " X  XXX X X  X  X X X X XX  XX ",   
    };
    
    private static boolean[][][] BLOCKS = new boolean[STRINGS.length / 5][5][];
    
    static {
        for (int i = BLOCKS.length - 1; i >= 0; --i) {
            final int offset = 5 * i;
            for (int y = 4; y >= 0; --y) {
                final String str = STRINGS[offset + y];
                final boolean[] row = BLOCKS[i][y] = new boolean[str.length()];
                for (int x = row.length - 1; x >= 0; --x) {
                    row[x] = str.charAt(x) != ' ';
                }
            }
        }
    }
    
    public static void drawCentered(final int index, final TextGraphics g, final TerminalSize size, TextColor color, 
            final boolean small) {
        draw(index, g, computeCenterX(index, size, small), computeCenterY(size, small), color, small, 0, 4);
    }
    
    public static void drawCentered(final int index, final TextGraphics g, final TerminalSize size, TextColor color, 
            final boolean small, final int startY, final int endY) {
        draw(index, g, computeCenterX(index, size, small), computeCenterY(size, small), color, small, startY, endY);        
    }    
    
    public static void drawCentered(final int index, final TextGraphics g, final TerminalSize size, final int y, 
            TextColor color, final boolean small) {
        draw(index, g, computeCenterX(index, size, small), y, color, small, 0, 4);
    }
    
    public static void drawCentered(final int index, final TextGraphics g, final TerminalSize size, final int y, 
            TextColor color, final boolean small, final int startY, final int endY) {
        draw(index, g, computeCenterX(index, size, small), y, color, small, startY, endY);        
    }
    
    public static int computeHeight(final boolean small) {
        return small ? 5 : 10;
    }
    
    public static int computeWidth(final int index, final boolean small) {
        return BLOCKS[index][0].length * (small ? 2 : 4);
    }
    
    public static int computeCenterX(final int index, final TerminalSize size, final boolean small) {
        return (size.getColumns() - computeWidth(index, small)) / 2;
    }
    
    public static int computeCenterY(final TerminalSize size, final boolean small) {
        return (size.getRows() - computeHeight(small)) / 2;
    }    
    
    public static void draw(final int index, final TextGraphics g, final int x, final int y, TextColor color, 
            final boolean small) {
        draw(index, g, x, y, color, small, 0, 4);
    }
    
    public static void draw(final int index, final TextGraphics g, final int x, final int y, TextColor color, 
            final boolean small, final int startY, final int endY) {
        
        GraphicsUtil.setColor(g, color, Colors.BLACK);
        
        final boolean[][] blocks = BLOCKS[index];
        if (small) {
            for (int i = endY; i >= startY; --i) {
                final int Y = y + i;
                final boolean[] row = blocks[i];
                for (int j = row.length - 1; j >= 0; --j) {
                    if (row[j]) {                        
                        g.putString(x + 2 * j, Y, "  ");
                    }
                }
            }
        } else {
            for (int i = endY; i >= startY; --i) {
                final int Y = y + 2 * i;
                final boolean[] row = blocks[i];
                for (int j = row.length - 1; j >= 0; --j) {
                    if (row[j]) {                        
                        final int X = x + 4 * j;
                        g.putString(X, Y, "    ");
                        g.putString(X, Y + 1, "    ");
                    }
                }
            }
        }
    }
    
    private BlockText() {       
    }
}
