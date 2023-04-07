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
    private final boolean centered;
    
    public TextFieldRenderer() {
        this(false, DEFAULT_MESSAGE_COLOR, DEFAULT_VALUE_COLOR);
    }
    
    public TextFieldRenderer(final boolean centered) {
        this(centered, DEFAULT_MESSAGE_COLOR, DEFAULT_VALUE_COLOR);
    }
    
    public TextFieldRenderer(final boolean centered, final TextColor messageColor, final TextColor valueColor) {
        this.centered = centered;
        this.messageColor = messageColor;
        this.valueColor = valueColor;
    }
    
    public void render(final TextGraphics g, final TerminalSize size, final TextField textField, final int y) {
        
        final String message = textField.getMessage();
        
        final int x;
        if (centered) {
            if (message != null) {
                x = (size.getColumns() - (message.length() + 1 + textField.getValue().length())) / 2;
            } else {
                x = (size.getColumns() - textField.getValue().length()) / 2;
            }
        } else {
            x = (size.getColumns() - textField.getWidth()) / 2;
        }
                
        final int valueX;
        if (message != null) {        
            GraphicsUtil.setColor(g, QuestionRenderer.BACKGROUND_COLOR, messageColor);
            g.putString(x, y, message);
            valueX = x + message.length() + 1;
        } else {
            valueX = x;
        }
        
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
