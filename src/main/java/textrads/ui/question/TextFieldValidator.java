package textrads.ui.question;

public interface TextFieldValidator {
    boolean evaluate(String input);
    int getMaxLength();
}
