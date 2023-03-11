package textrads.ui.question;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.InputSource;
import textrads.ui.menu.BackExitState;

public class Question {

    private static final int HEIGHT = 9;
    
    private final String title;
    private final TextField textField;
    private final int width;    
    
    private final BackExitState backExitState = new BackExitState();
    
    private boolean escPressed;
    
    public Question(final String title, final TextField textField) {
        this.title = title;
        this.textField = textField;
        
        width = Math.max(title.length(), textField.getWidth());
    }
    
    public void init(final String initialValue) {
        textField.init(initialValue);
        escPressed = false;
        InputSource.clear();
    }
    
    public void update() {        
        if (escPressed || textField.isEnterPressed()) {
            InputSource.clear();
            textField.setCursorVisible(false);
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
                backExitState.setEscSelected(true);
                textField.setCursorVisible(false);
                break;
            default:
                textField.handleInput(keyStroke);
                break;
        }
    }

    public boolean isEscPressed() {
        return escPressed;
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
