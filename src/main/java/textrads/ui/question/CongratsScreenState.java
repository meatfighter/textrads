package textrads.ui.question;

import com.googlecode.lanterna.graphics.TextImage;
import com.googlecode.lanterna.input.KeyStroke;
import textrads.InputSource;

public class CongratsScreenState {
    
    private final TextField textField = new TextField("Enter your initials:", new InitialsValidator(), 
            new UpperCaseTransformer());
    
    private String title;
    private TextImage smallImage;
    private TextImage bigImage;
    
    public void init(final String title, final TextImage smallImage, final TextImage bigImage, final String initials) {
        this.title = title;
        this.smallImage = smallImage;
        this.bigImage = bigImage;
        textField.init(initials);
        InputSource.clear();
    }
    
    public void update() {        
        if (textField.isEnterPressed()) {
            InputSource.clear();
            textField.setCursorVisible(false);
        } else {            
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                textField.handleInput(keyStroke);
            }
            textField.update();
        }
    }

    public String getTitle() {
        return title;
    }

    public TextImage getSmallImage() {
        return smallImage;
    }

    public TextImage getBigImage() {
        return bigImage;
    }

    public TextField getTextField() {
        return textField;
    }
    
    public boolean isEnterPressed() {
        return textField.isEnterPressed();
    }
    
    public String getInitials() {
        return textField.getValue();
    }
}
