package textrads;

import com.googlecode.lanterna.TextColor;

public class BlockImage {
    
    private final TextColor[][] pixels;
    private final int width;
    private final int height;
    
    public BlockImage(final int width, final int height) {
        this.width = width;
        this.height = height;         
        pixels = new TextColor[height][width];      
    }
    
    public TextColor[][] getPixels() {
        return pixels;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
