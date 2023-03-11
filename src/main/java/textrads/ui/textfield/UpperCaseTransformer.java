package textrads.ui.textfield;

public class UpperCaseTransformer implements TextFieldTransformer {

    @Override
    public char transform(final char c) {
        return Character.toUpperCase(c);
    }
    
}