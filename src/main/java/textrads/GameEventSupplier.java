package textrads;

import java.util.List;
import java.util.function.Supplier;

public interface GameEventSupplier extends Supplier<List<GameEvent>> {
    void update(App app);
}
