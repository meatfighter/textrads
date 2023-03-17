package textrads;

import textrads.keymap.KeyMap;
import com.googlecode.lanterna.input.KeyStroke;
import java.util.HashMap;
import java.util.Map;

public final class InputEventSource {

    public static final int MAX_POLLS = 4;
    
    private static final long MAX_REPEAT_PERIOD = Textrads.FRAMES_PER_SECOND / 4;
    
    private static final Map<InputType, Long> lastPressedTimes = new HashMap<>(); 
    
    private static KeyMap keyMap;
    
    private static long updates;
    
    static {
        for (final InputType inputType : InputType.values()) {
            lastPressedTimes.put(inputType, 0L);
        }
    }
    
    public static synchronized void setKeyMap(final KeyMap inputMap) {
        InputEventSource.keyMap = inputMap;        
    }
    
    public static synchronized void poll(final InputEventList events) {
        events.clear();
        for (int i = 0; i < MAX_POLLS; ++i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            }
            final InputType inputType = keyMap.get(keyStroke);
            if (inputType == null) {
                continue;
            }
            final long last = lastPressedTimes.get(inputType);
            lastPressedTimes.put(inputType, updates);
            events.add(InputEvent.fromInputType(inputType, updates - last <= MAX_REPEAT_PERIOD));
        }
        ++updates;
    }
       
    public static synchronized void clear() {
        InputSource.clear();
    }    
}
