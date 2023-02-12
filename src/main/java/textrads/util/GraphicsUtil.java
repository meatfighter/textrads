package textrads.util;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import textrads.BlockPic;

public final class GraphicsUtil {
    
    private static final String BLOCK_PICS_FILENAME_EXTENSION = ".bpx";
    
    private static final char RIGHT_HALF_BLOCK = '\u2590';
    
    private static final TextColor[] INDEXED_COLORS = new TextColor[256];
    
    private static final TextColor TRANSPARENT_COLOR;

    static {
        for (int i = INDEXED_COLORS.length - 1; i >= 0; --i) {
            INDEXED_COLORS[i] = new TextColor.Indexed(i);   
        }
        TRANSPARENT_COLOR = INDEXED_COLORS[16];
    }
    
    public static BlockPic loadBlockPic(final String name) {
        try (final InputStream is = GraphicsUtil.class.getResourceAsStream(
                    String.format("/images/%s%s", name, BLOCK_PICS_FILENAME_EXTENSION)); 
                final BufferedInputStream bis = new BufferedInputStream(is); 
                final DataInputStream dis = new DataInputStream(bis)) {
            final int width = dis.readInt();
            final int height = dis.readInt();
            final BlockPic blockPic = new BlockPic(width, height);
            final TextColor[][] colors = blockPic.getColors();
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    colors[y][x] = INDEXED_COLORS[0xFF & dis.read()];
                }
            }
            return blockPic;
        } catch (final Exception ignored) {
            ignored.printStackTrace(); // TODO REMOVE
        }
        return null;
    }
    
    public static void drawBlockPic(final TextGraphics g, final BlockPic pic, final int x, final int y) {
        final TextColor[][] pixels = pic.getColors();
        for (int i = pixels.length - 1; i >= 0; --i) {
            final int oy = i + y;
            final TextColor[] row = pixels[i];
            for (int j = row.length - 1; j >= 0; --j) {
                final TextColor color = row[j];                
                if (color == TRANSPARENT_COLOR) {                    
                    continue;
                }
                final int ox = j + x;
                final int cx = ox / 2;
                final TextCharacter c = g.getCharacter(cx, oy);
                if (c == null) {                   
                    continue;
                }
                TextColor leftColor;
                TextColor rightColor;
                if (c.getCharacterString().charAt(0) != RIGHT_HALF_BLOCK) {
                    leftColor = rightColor = c.getBackgroundColor();
                } else {
                    leftColor = c.getBackgroundColor();
                    rightColor = c.getForegroundColor();
                }
                if ((ox & 1) == 0) {
                    leftColor = color;
                } else {
                    rightColor = color;
                }
                g.setBackgroundColor(leftColor);
                g.setForegroundColor(rightColor);
                g.setCharacter(cx, oy, RIGHT_HALF_BLOCK);
            }
        }
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
    
    private GraphicsUtil() {        
    }
}
