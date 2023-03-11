package textrads.ui.textfield;

import static org.apache.commons.lang3.StringUtils.isAsciiPrintable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class InitialsValidator implements TextFieldValidator {

    @Override
    public boolean evaluate(final String input) {
        return input.length() <= 3 && isNotBlank(input) && isAsciiPrintable(input);
    }    
}
