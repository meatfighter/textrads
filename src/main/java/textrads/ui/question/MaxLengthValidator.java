package textrads.ui.question;

public class MaxLengthValidator implements TextFieldValidator {

    private final int maxLength;
    
    public MaxLengthValidator(final int maxLength) {
        this.maxLength = maxLength;
    }
    
    @Override
    public boolean evaluate(final String input) {
        return input.length() <= maxLength;
    }    

    @Override
    public int getMaxLength() {
        return maxLength;
    }    
}