package textrads;

import com.googlecode.lanterna.graphics.TextGraphics;

public interface MonoGameRenderer {    
    Dimensions getDimensions(boolean big, boolean attackBar);
    void render(TextGraphics g, MonoGameState state, int x, int y, boolean big, boolean attackBar);    
}
