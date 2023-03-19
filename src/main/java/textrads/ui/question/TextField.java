package textrads.ui.question;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.app.Textrads;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TextField {
    
    private static final long CURSOR_BLINK_MILLIS = 530;
    
    private static final float FRAMES_PER_BLINK = (float) (Textrads.FRAMES_PER_SECOND * CURSOR_BLINK_MILLIS / 1000.0);

    private final String message;
    private final TextFieldValidator validator;
    private final TextFieldTransformer transformer;
    private final int width;
    
    private final StringBuilder value = new StringBuilder();
    private float frameCounter;
    private boolean cursorVisible;
    private boolean enterPressed;
    private int cursorPosition;
    
    public TextField(final String message, final TextFieldValidator validator) {
        this(message, validator, null);
    }
    
    public TextField(final String message, final TextFieldValidator validator, final TextFieldTransformer transformer) {
        this.message = message;
        this.validator = validator;
        this.transformer = transformer;
        width = message.length() + 1 + validator.getMaxLength();
    }
    
    public void init(final String initialValue) {
        resetCursor();
        enterPressed = false;
        value.setLength(0);
        if (isNotBlank(initialValue)) {
            value.append(initialValue);
        }
        cursorPosition = value.length();
    }
    
    private void resetCursor() {
        cursorVisible = true;
        frameCounter = 0f;
    }
    
    public void update() {
        if (frameCounter++ >= FRAMES_PER_BLINK) {
            frameCounter -= FRAMES_PER_BLINK;
            cursorVisible = !cursorVisible;
        } 
    }
       
    public void handleInput(final KeyStroke keyStroke) {
        
        switch (keyStroke.getKeyType()) {
            case ArrowLeft:
                if (cursorPosition > 0) {
                    --cursorPosition;                    
                }
                resetCursor();
                break;
            case ArrowRight:
                if (cursorPosition < value.length()) {
                    ++cursorPosition;                    
                }
                resetCursor();
                break;
            case Backspace:
                if (cursorPosition > 0 && --cursorPosition < value.length()) {
                    value.deleteCharAt(cursorPosition);
                }
                resetCursor();
                break;
            case Character: {
                Character character = keyStroke.getCharacter();
                if (character == null) {
                    break;
                }
                if (transformer != null) {
                    character = transformer.transform(character);
                }
                if (cursorPosition < value.length()) {
                    final char backup = value.charAt(cursorPosition);
                    value.setCharAt(cursorPosition, character);                
                    if (validator.evaluate(value.toString())) {
                        ++cursorPosition;
                    } else {
                        value.setCharAt(cursorPosition, backup);
                    }
                } else {
                    value.insert(cursorPosition, character);                
                    if (validator.evaluate(value.toString())) {
                        ++cursorPosition;
                    } else {
                        value.deleteCharAt(cursorPosition);
                    }
                }
                resetCursor();
                break;
            }
            case Delete:
                if (cursorPosition < value.length()) {
                    value.deleteCharAt(cursorPosition);
                }
                resetCursor();
                break;
            case Enter:
                if (validator.evaluate(value.toString())) {
                    enterPressed = true;
                }
                resetCursor();
                break;
            case End:
                cursorPosition = value.length();
                resetCursor();
                break;
            case Home:
                cursorPosition = 0;
                resetCursor();
                break;
        }
    }

    public int getWidth() {
        return width;
    }

    public String getMessage() {
        return message;
    }
    
    public void setCursorVisible(final boolean cursorVisible) {
        this.cursorVisible = cursorVisible;
    }

    public boolean isCursorVisible() {
        return cursorVisible && !enterPressed;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public boolean isEnterPressed() {
        return enterPressed;
    }
    
    public String getValue() {
        return value.toString();
    }
}
