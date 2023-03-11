package textrads.ui.textfield;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class NumberValidator implements TextFieldValidator {

    private final int minValue;
    private final int maxValue;
    
    public NumberValidator(final int minValue, final int maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @Override
    public boolean evaluate(final String input) {
        if (isBlank(input)) {
            return false;
        }
        for (int i = input.length() - 1; i >= 0; --i) {
            if (Character.isWhitespace(input.charAt(i))) {
                return false;
            }
        }
        try {
            final int value = Integer.parseInt(input);
            if (value < minValue || value > maxValue) {
                return false;
            }
        } catch (final NumberFormatException ignored) {
            return false;
        }
        return true;
    }
}