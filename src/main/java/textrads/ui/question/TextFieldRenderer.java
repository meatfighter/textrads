package textrads.ui.question;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class TextFieldRenderer {
    
    public static final TextColor TEXT_COLOR = Colors.GRAY;
    
    public void render(final TextGraphics g, final TerminalSize size, final TextField textField, final int y) {
        
        final int x = (size.getColumns() - textField.getWidth()) / 2;
        
        GraphicsUtil.setColor(g, QuestionRenderer.BACKGROUND_COLOR, TEXT_COLOR);
        g.putString(x, y, textField.getMessage());
        
        final int valueX = x + textField.getMessage().length() + 1;
        final String value = textField.getValue();
        g.putString(valueX, y, value);
        
        if (textField.isCursorVisible()) {
            GraphicsUtil.setColor(g, TEXT_COLOR, QuestionRenderer.BACKGROUND_COLOR);
            final int cursorPosition = textField.getCursorPosition();                        
            g.setCharacter(valueX + cursorPosition, y, 
                    (cursorPosition < value.length()) ? value.charAt(cursorPosition) : ' ');
        }
    }
}
