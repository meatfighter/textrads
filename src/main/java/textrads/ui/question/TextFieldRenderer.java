package textrads.ui.question;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.util.GraphicsUtil;

public class TextFieldRenderer {
    
    public static final TextColor DEFAULT_MESSAGE_COLOR = new TextColor.Indexed(117);
    public static final TextColor DEFAULT_VALUE_COLOR = new TextColor.Indexed(173);
    
    private final TextColor messageColor;
    private final TextColor valueColor;
    
    public TextFieldRenderer() {
        this(DEFAULT_MESSAGE_COLOR, DEFAULT_VALUE_COLOR);
    }
    
    public TextFieldRenderer(final TextColor messageColor, final TextColor valueColor) {
        this.messageColor = messageColor;
        this.valueColor = valueColor;
    }
    
    public void render(final TextGraphics g, final TerminalSize size, final TextField textField, final int y) {
        
        final int x = (size.getColumns() - textField.getWidth()) / 2;
        
        GraphicsUtil.setColor(g, QuestionRenderer.BACKGROUND_COLOR, messageColor);
        g.putString(x, y, textField.getMessage());
        
        final int valueX = x + textField.getMessage().length() + 1;
        final String value = textField.getValue();
        GraphicsUtil.setColor(g, QuestionRenderer.BACKGROUND_COLOR, valueColor);
        g.putString(valueX, y, value);
        
        if (textField.isCursorVisible()) {
            GraphicsUtil.setColor(g, valueColor, QuestionRenderer.BACKGROUND_COLOR);
            final int cursorPosition = textField.getCursorPosition();                        
            g.setCharacter(valueX + cursorPosition, y, 
                    (cursorPosition < value.length()) ? value.charAt(cursorPosition) : ' ');
        }
    }
}
