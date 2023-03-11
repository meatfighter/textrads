package textrads.ui.textfield;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.Textrads;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class TextField {
    
    private static final long CURSOR_BLINK_MILLIS = 530;
    
    private static final float FRAMES_PER_BLINK = (float) (Textrads.FRAMES_PER_SECOND * CURSOR_BLINK_MILLIS / 1000.0);

    private final String message;
    private final TextFieldValidator validator;
    private final TextFieldTransformer transformer;
    
    private final StringBuilder value = new StringBuilder();
    private float frameCounter;
    private boolean cursorVisible;
    private boolean valueAvailable;
    private int cursorPosition;
    
    public TextField(final String message) {
        this(message, null, null);
    }
    
    public TextField(final String message, final TextFieldValidator validator) {
        this(message, validator, null);
    }
    
    public TextField(final String message, final TextFieldValidator validator, final TextFieldTransformer transformer) {
        this.message = message;
        this.validator = validator;
        this.transformer = transformer;
    }
    
    public void init(final String initialValue) {
        resetCursor();
        valueAvailable = false;
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
                value.insert(cursorPosition, character);
                if (validator == null || validator.evaluate(value.toString())) {
                    ++cursorPosition;
                } else {
                    value.deleteCharAt(cursorPosition);
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
                if (validator == null || validator.evaluate(value.toString())) {
                    valueAvailable = true;
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

    public String getMessage() {
        return message;
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
    
    public String getValue() {
        return valueAvailable ? value.toString() : null;
    }
}
