package textrads;

import com.googlecode.lanterna.TextColor;

public class BlockPic {
    
    private final TextColor[][] colors;
    private final int width;
    private final int height;
    
    public BlockPic(final int width, final int height) {
        this.width = width;
        this.height = height;         
        colors = new TextColor[height][width];      
    }
    
    public TextColor[][] getColors() {
        return colors;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
}
