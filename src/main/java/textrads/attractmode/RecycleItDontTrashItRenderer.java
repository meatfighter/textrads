package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.graphics.TextImage;
import textrads.ui.common.Images;
import textrads.util.GraphicsUtil;

public class RecycleItDontTrashItRenderer {
    
    private static final TextColor BACKGROUND_COLOR = new TextColor.Indexed(231);
    private static final TextColor TEXT_COLOR = new TextColor.Indexed(26);

    public void render(final TextGraphics g, final TerminalSize size) {

        final boolean small = GraphicsUtil.isSmallTerminal(size);
        final TextImage image = small ? Images.SMALL_EPA_SEAL : Images.BIG_EPA_SEAL;
        final TerminalSize imageSize = image.getSize();
        final int imageWidth = imageSize.getColumns();
        final int imageHeight = imageSize.getRows();
        final int recycleX = (size.getColumns() - 18) / 2;
        final int recycleY = (size.getRows() - (imageHeight + (small ? 4 : 5))) / 2;
        final int imageX = (size.getColumns() - (imageWidth + 33)) / 2;
        final int imageY = recycleY + (small ? 4 : 5);
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TEXT_COLOR);
        g.fill(' ');
        g.putString(recycleX, recycleY, "RECYCLE IT,");
        g.putString(recycleX + 3, recycleY + 2, "DON'T TRASH IT!");
        g.putString(imageX + imageWidth + 1, imageY + 3 * imageHeight / 4, "William K. Reilly, Administrator");
        GraphicsUtil.drawImage(g, size, image, imageX, imageY);
    }
}
