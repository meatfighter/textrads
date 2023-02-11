package textrads.util;

import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import textrads.BlockImage;

public final class GraphicsUtil {
    
    private static final char LOWER_HALF_BLOCK = '\u2584';
    
    private static final TextColor[] INDEXED_COLORS = new TextColor[256];
    
    private static final TextColor TRANSPARENT_COLOR;

    static {
        for (int i = INDEXED_COLORS.length - 1; i >= 0; --i) {
            INDEXED_COLORS[i] = new TextColor.Indexed(i);   
        }
        TRANSPARENT_COLOR = INDEXED_COLORS[16];
    }
    
    public static BlockImage readBlockImage(final String filename) {
        try (final InputStream is = GraphicsUtil.class.getResourceAsStream("/images/" + filename); 
                final BufferedInputStream bis = new BufferedInputStream(is)) {
            return readBlockImage(bis);
        } catch (final Exception ignored) {
            ignored.printStackTrace(); // TODO REMOVE
        }
        return null;
    }

    public static BlockImage readBlockImage(final InputStream in) throws IOException {
        
        final BufferedImage bufferedImage = ImageIO.read(in);
        if (bufferedImage.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
            throw new IllegalArgumentException("Image is not indexed byte type.");
        }
        final WritableRaster raster = bufferedImage.getRaster();
        if (raster.getNumBands() != 1) {
            throw new IllegalArgumentException("Image contains more than one band.");
        }
        
        final BlockImage blockImage = new BlockImage(bufferedImage.getWidth(), bufferedImage.getHeight());
        final TextColor[][] pixels = blockImage.getPixels();
        final int[] sample = new int[1];
        for (int y = bufferedImage.getHeight() - 1; y >= 0; --y) {
            for (int x = bufferedImage.getWidth() - 1; x >= 0; --x) {
                raster.getPixel(x, y, sample);
                pixels[y][x] = INDEXED_COLORS[sample[0]];
            }
        }
        
        return blockImage;
    }   
    
    public static void drawBlockImage(final TextGraphics g, final BlockImage image, final int x, final int y) {
        final TextColor[][] pixels = image.getPixels();
        for (int i = image.getHeight() - 1; i >= 0; --i) {
            final int oy = i + y;
            final int cy = oy / 2;
            final boolean setUpper = (oy & 1) == 0;
            for (int j = image.getWidth() - 1; j >= 0; --j) {
                final TextColor color = pixels[i][j];
                if (color == TRANSPARENT_COLOR) {
                    continue;
                }
                final int ox = j + x;                
                final TextCharacter c = g.getCharacter(ox, cy);
                if (c == null) {
                    continue;
                }
                TextColor upperColor;
                TextColor lowerColor;
                if (c.getCharacterString().charAt(0) != LOWER_HALF_BLOCK) {
                    upperColor = lowerColor = c.getBackgroundColor();
                } else {
                    upperColor = c.getBackgroundColor();
                    lowerColor = c.getForegroundColor();
                }
                if (setUpper) {
                    upperColor = color;
                } else {
                    lowerColor = color;
                }
                g.setBackgroundColor(upperColor);
                g.setForegroundColor(lowerColor);
                g.setCharacter(ox, cy, LOWER_HALF_BLOCK);
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
