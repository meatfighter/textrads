package textrads.ui.question;

public class UpperCaseTransformer implements TextFieldTransformer {

    @Override
    public char transform(final char c) {
        return Character.toUpperCase(c);
    }
    
}