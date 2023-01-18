package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonoGameEventSupplier implements GameEventSupplier {
    
    private static long MAX_REPEAT_PERIOD = Textrads.FRAMES_PER_SECOND / 4;
    
    private final List<Integer> events = new ArrayList<>();
    private final Map<InputType, Long> lastPressedTimes = new HashMap<>(); 
    
    private final InputMap inputMap;
    private final Screen screen;
    
    private long updates;
    
    public MonoGameEventSupplier(final InputMap inputMap, final Screen screen) {
        this.inputMap = inputMap;
        this.screen = screen;
        
        for (final InputType inputType : InputType.values()) {
            lastPressedTimes.put(inputType, 0L);
        }
    }
    
    @Override
    public void update(final App app) {        
        events.clear();
        try {
            KeyStroke keyStroke;
            while ((keyStroke = screen.pollInput()) != null) {
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
        } catch (final Exception e) {            
            e.printStackTrace(); // TODO ENHANCE
            System.exit(0);
        }
        events.add(GameEvent.UPDATE);
        ++updates;
    }

    @Override
    public List<Integer> get() {
        return events;
    }    
}
