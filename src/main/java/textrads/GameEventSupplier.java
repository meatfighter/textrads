package textrads;

import java.util.List;
import java.util.function.Supplier;

public interface GameEventSupplier extends Supplier<List<Integer>> {
    void update(App app);
}
