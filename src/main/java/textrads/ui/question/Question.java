package textrads.ui.question;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.input.InputSource;
import textrads.ui.menu.BackExitState;

public class Question {
    
    private static final int HEIGHT = 9;
    
    private static final int ESC_FRAMES = 3;
        
    private final TextField textField;
    private final BackExitState backExitState;
    
    private String title;
    private int width;    
    private boolean escPressed;
    private int escTimer;

    public Question(final TextField textField) {
        this(textField, new BackExitState());
    }
    
    public Question(final TextField textField, final BackExitState backExitState) {
        this.textField = textField;
        this.backExitState = backExitState;
    }
    
    public void init(final String title, final String initialValue) {
        this.title = title;
        width = Math.max(title.length(), textField.getWidth());
        textField.init(initialValue);
        backExitState.reset();
        escPressed = false;
        escTimer = ESC_FRAMES;
        InputSource.clear();
    }
    
    public void update() {        
        if (escPressed || textField.isEnterPressed()) {
            InputSource.clear();
            textField.setCursorVisible(false);
            if (escPressed && escTimer > 0) {
                --escTimer;
            }
        } else {            
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                handleInput(keyStroke);
            }
            textField.update();
        }
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                escPressed = true;
                backExitState.setSelected(true);
                textField.setCursorVisible(false);
                break;
            default:
                textField.handleInput(keyStroke);
                break;
        }
    }

    public boolean isEscPressed() {
        return (escTimer == 0) ? escPressed : false;
    }
    
    public boolean isEnterPressed() {
        return textField.isEnterPressed();
    }
    
    public String getValue() {
        return textField.getValue();
    }

    public int getWidth() {
        return width;
    }    

    public int getHeight() {
        return HEIGHT;
    }
    
    public String getTitle() {
        return title;
    }

    public TextField getTextField() {
        return textField;
    }

    public BackExitState getBackExitState() {
        return backExitState;
    }    
}
