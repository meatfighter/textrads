package textrads.ui.question;

import static org.apache.commons.lang3.StringUtils.isAsciiPrintable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class InitialsValidator implements TextFieldValidator {

    private static final int MAX_LENGTH = 3;
    
    @Override
    public boolean evaluate(final String input) {
        return input.length() <= MAX_LENGTH && isNotBlank(input) && isAsciiPrintable(input);
    }    

    @Override
    public int getMaxLength() {
        return MAX_LENGTH;
    }
}
