package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GameEventSource {
    
    private static final long MAX_REPEAT_PERIOD = Textrads.FRAMES_PER_SECOND / 4;
    private static final int MAX_POLLS = 32;
    
    private static final List<Integer> events = new ArrayList<>();
    private static final Map<InputType, Long> lastPressedTimes = new HashMap<>(); 
    
    private static InputMap inputMap;
    
    private static long updates;
    
    static {
        for (final InputType inputType : InputType.values()) {
            lastPressedTimes.put(inputType, 0L);
        }
    }
    
    public static synchronized void setInputMap(final InputMap inputMap) {
        GameEventSource.inputMap = inputMap;        
    }
    
    public static synchronized void update() {                
        for (int i = 0; i < MAX_POLLS; ++i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            }
            final InputType inputType = inputMap.get(keyStroke);
            if (inputType == null) {
                continue;
            }
            final long last = lastPressedTimes.get(inputType);
            lastPressedTimes.put(inputType, updates);
            events.add(GameEvent.fromInputType(inputType, updates - last <= MAX_REPEAT_PERIOD));
        }
        events.add(GameEvent.UPDATE);
        ++updates;
    }
       
    public static synchronized void clear() {
        InputSource.clear();
        events.clear();
    }

    public static synchronized Integer poll() {
        return events.isEmpty() ? null : events.remove(0);
    }    
}
