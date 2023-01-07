package textrads;

import java.util.function.Supplier;

public final class InputSource {
    
    private static Supplier<InputType> inputTypeSupplier;
    
    public static void setInputTypeSupplier(final Supplier<InputType> inputTypeSupplier) {
        InputSource.inputTypeSupplier = inputTypeSupplier;
    }
    
    public static Supplier<InputType> getInputTypeSupplier() {
        return inputTypeSupplier;
    }
}
