package textrads;

import com.googlecode.lanterna.graphics.TextImage;
import textrads.util.GraphicsUtil;

public interface Images {
    TextImage BIG_FBI_SEAL = GraphicsUtil.loadImage("big-fbi-seal");
    TextImage SMALL_FBI_SEAL = GraphicsUtil.loadImage("small-fbi-seal");
    TextImage BIG_EPA_SEAL = GraphicsUtil.loadImage("big-epa-seal");
    TextImage SMALL_EPA_SEAL = GraphicsUtil.loadImage("small-epa-seal");
    TextImage BIG_EARTH = GraphicsUtil.loadImage("big-earth");
    TextImage SMALL_EARTH = GraphicsUtil.loadImage("small-earth");    
}
