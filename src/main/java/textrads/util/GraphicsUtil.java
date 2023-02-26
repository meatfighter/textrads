package textrads.util;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.BasicTextImage;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

public final class GraphicsUtil {
    
    private static final String TEXT_IMAGE_FILENAME_EXTENSION = ".tim";
    
    private static final char RIGHT_HALF_BLOCK = '\u2590';
    
    private static final TextColor[] INDEXED_COLORS = new TextColor[256];
    private static final TextCharacter[][] BLOCK_CHARACTERS 
            = new TextCharacter[INDEXED_COLORS.length][INDEXED_COLORS.length];

    static {
        for (int i = INDEXED_COLORS.length - 1; i >= 0; --i) {
            INDEXED_COLORS[i] = new TextColor.Indexed(i);   
        }
        for (int i = INDEXED_COLORS.length - 1; i >= 0; --i) {
            for (int j = INDEXED_COLORS.length - 1; j >= 0; --j) {
                BLOCK_CHARACTERS[i][j] = TextCharacter.fromCharacter(RIGHT_HALF_BLOCK, INDEXED_COLORS[j], 
                        INDEXED_COLORS[i])[0];
            }
        }
    }
    
    public static TextImage loadImage(final String name) {
        try (final InputStream is = GraphicsUtil.class.getResourceAsStream(
                    String.format("/images/%s%s", name, TEXT_IMAGE_FILENAME_EXTENSION)); 
                final BufferedInputStream bis = new BufferedInputStream(is); 
                final DataInputStream dis = new DataInputStream(bis)) {
            final int columns = dis.readInt();
            final int rows = dis.readInt();
            final TextImage textImage = new BasicTextImage(columns, rows);            
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    final int leftColor = 0xFF & dis.read();
                    final int rightColor = 0xFF & dis.read();                    
                    textImage.setCharacterAt(j, i, BLOCK_CHARACTERS[leftColor][rightColor]);
                }
            }
            return textImage;
        } catch (final Exception ignored) {
            ignored.printStackTrace(); // TODO REMOVE
        }
        return null;
    }
    
    public static void drawImage(final TextGraphics g, final TerminalSize size, final TextImage image, final int x, 
            final int y) {
        
        TerminalSize sourceImageSize = image.getSize();
        
        // cropping specified image-subrectangle to the image itself:
        int fromRow = 0;
        int untilRow = Math.min(sourceImageSize.getRows(), image.getSize().getRows());
        int fromColumn = 0;
        int untilColumn = Math.min(sourceImageSize.getColumns(), image.getSize().getColumns());

        // top/left-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        fromRow = Math.max(0, -y);
        fromColumn = Math.max(0, -x);

        // bot/right-crop at target(TextGraphics) rectangle: (only matters, if topLeft has a negative coordinate)
        untilRow = Math.min(untilRow, size.getRows() - y);
        untilColumn = Math.min(untilColumn, size.getColumns() - x);

        if (fromRow >= untilRow || fromColumn >= untilColumn) {
            return;
        }
        for (int row = fromRow; row < untilRow; row++) {
            for (int column = fromColumn; column < untilColumn; column++) {
                g.setCharacter(column + x, row + y, image.getCharacterAt(column, row));
            }
        }
    }
    
    public static void centerImage(final TextGraphics g, final TerminalSize size, final TextImage image, final int y) {
        drawImage(g, size, image, (size.getColumns() - image.getSize().getColumns()) / 2, y);
    }

    public static void putIntRight(final TextGraphics g, final int x, final int y, final int value) {
        putStringRight(g, x, y, String.format(" %d", value));
    }
    
    public static void putStringRight(final TextGraphics g, final int x, final int y, final String value) {
        g.putString(x + 1 - value.length(), y, value);
    }
    
    public static String formatTime(int time) { 
        time /= 60;
        if (time < 60) {
            return String.format(" %d", time);
        }
        final int seconds = time % 60;
        time /= 60;
        if (time < 60) {
            return String.format(" %d:%02d", time, seconds);
        }
        final int minutes = time % 60;
        time /= 60;
        if (time < 24) {
            return String.format(" %d:%02d:%02d", time, minutes, seconds);
        }
        return String.format(" %d:%02d:%02d:%02d", time / 24, time % 24, minutes, seconds);
    }
    
    public static boolean isSmallTerminal(final TerminalSize size) {
        
        // standard size: 80 x 24
        // 2p small size: 71 x 22
        // 2p big size:  129 x 42
        
        return size.getColumns() < 129 || size.getRows() < 42;
    }
    
    public static boolean isBigTerminal(final TerminalSize size) {
        return !isSmallTerminal(size);
    }
    
    public static int centerString(final TextGraphics g, final TerminalSize size, final int y, final String str) {
        final int x = (size.getColumns() - str.length()) / 2;
        g.putString(x, y, str);
        return x;
    }
    
    // Always set background and foreground color at the same time, particularly when printing spaces. Spaces do not
    // have a visible foreground color. However, the Screen buffer maintains the foreground color of all characters,
    // including spaces. Forgetting to set the foreground color when printing spaces can inadvertantly lead to large 
    // regions of the buffer invisibly changing. That, in turn, can cause the terminal to flicker since the terminal
    // will clear to black and repaint when it receives a lot of data in a short period.
    public static void setColor(final TextGraphics g, final TextColor backgroundColor, 
            final TextColor foregroundColor) {
        
        g.setBackgroundColor(backgroundColor);
        g.setForegroundColor(foregroundColor);
    }
    
    private GraphicsUtil() {        
    }
}
