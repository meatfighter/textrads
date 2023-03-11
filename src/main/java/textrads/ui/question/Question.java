package textrads.ui.question;

import textrads.ui.menu.BackExitState;

public class Question {

    private static final int HEIGHT = 9;
    
    private final String title;
    private final TextField textField;
    private final int width;
    
    private final BackExitState backExitState = new BackExitState();
    
    public Question(final String title, final TextField textField) {
        this.title = title;
        this.textField = textField;
        
        width = Math.max(title.length(), textField.getWidth());
    }
    
    public void init(final String initialValue) {
        textField.init(initialValue);
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
