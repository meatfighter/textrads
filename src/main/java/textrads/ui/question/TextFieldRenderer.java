package textrads.ui.question;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.Colors;
import textrads.util.GraphicsUtil;

public class TextFieldRenderer {
    
    public static final TextColor DEFAULT_TEXT_COLOR = Colors.GRAY;
    
    private final TextColor textColor;
    
    public TextFieldRenderer() {
        this(DEFAULT_TEXT_COLOR);
    }
    
    public TextFieldRenderer(final TextColor textColor) {
        this.textColor = textColor;
    }
    
    public void render(final TextGraphics g, final TerminalSize size, final TextField textField, final int y) {
        
        final int x = (size.getColumns() - textField.getWidth()) / 2;
        
        GraphicsUtil.setColor(g, QuestionRenderer.BACKGROUND_COLOR, textColor);
        g.putString(x, y, textField.getMessage());
        
        final int valueX = x + textField.getMessage().length() + 1;
        final String value = textField.getValue();
        g.putString(valueX, y, value);
        
        if (textField.isCursorVisible()) {
            GraphicsUtil.setColor(g, textColor, QuestionRenderer.BACKGROUND_COLOR);
            final int cursorPosition = textField.getCursorPosition();                        
            g.setCharacter(valueX + cursorPosition, y, 
                    (cursorPosition < value.length()) ? value.charAt(cursorPosition) : ' ');
        }
    }
}
