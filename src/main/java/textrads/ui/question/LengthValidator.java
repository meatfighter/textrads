package textrads.ui.question;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class LengthValidator implements TextFieldValidator {

    private final int minLength;
    private final int maxLength;
    
    public LengthValidator(final int minLength, final int maxLength) {
        this.minLength = minLength;
        this.maxLength = maxLength;
    }
    
    @Override
    public boolean evaluate(final String input) {
        return input.length() >= minLength && input.length() <= maxLength && isNotBlank(input);
    }    

    @Override
    public int getMaxLength() {
        return maxLength;
    }    
}