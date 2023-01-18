package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GameEventSupplier implements Supplier<Integer> {
    
    private static final long MAX_REPEAT_PERIOD = Textrads.FRAMES_PER_SECOND / 4;
    
    private final List<Integer> events = new ArrayList<>();
    private final Map<InputType, Long> lastPressedTimes = new HashMap<>(); 
    
    private final InputMap inputMap;
    
    private long updates;
    
    public GameEventSupplier(final InputMap inputMap) {
        this.inputMap = inputMap;
        
        for (final InputType inputType : InputType.values()) {
            lastPressedTimes.put(inputType, 0L);
        }
    }
    
    public void update(final App app) {        
        KeyStroke keyStroke;
        while ((keyStroke = InputSource.poll()) != null) {
            final InputType inputType = inputMap.get(keyStroke);
            if (inputType == null) {
                continue;
            }

            if (inputType == InputType.QUIT) {
                app.setTerminate(true); // TODO ENHANCE
                return;
            }

            final long last = lastPressedTimes.get(inputType);
            lastPressedTimes.put(inputType, updates);

            events.add(GameEvent.fromInputType(inputType, updates - last <= MAX_REPEAT_PERIOD));
        }
        events.add(GameEvent.UPDATE);
        ++updates;
    }
       
    public void clear() {
        InputSource.clear();
        events.clear();
    }

    @Override
    public Integer get() {
        return events.isEmpty() ? null : events.remove(0);
    }    
}
