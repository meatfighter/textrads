package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonoGameEventSupplier implements GameEventSupplier {
    
    private static final long REPEAT_MARGIN = 3;
    
    private static class KeyPressedTimes {
        
        private long older;
        private long newer;

        public long getOlder() {
            return older;
        }

        public void setOlder(final long older) {
            this.older = older;
        }

        public long getNewer() {
            return newer;
        }

        public void setNewer(final long newer) {
            this.newer = newer;
        }
    }
    
    private final List<GameEvent> events = new ArrayList<>();
    private final Map<InputType, KeyPressedTimes> keyPressedTimes = new HashMap<>(); 
    
    private final InputMap inputMap;
    private final Screen screen;
    
    private long updates;
    
    public MonoGameEventSupplier(final InputMap inputMap, final Screen screen) {
        this.inputMap = inputMap;
        this.screen = screen;
        
        for (final InputType inputType : InputType.values()) {
            keyPressedTimes.put(inputType, new KeyPressedTimes());
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
                
                final KeyPressedTimes times = keyPressedTimes.get(inputType);
                final long duration = updates - times.getNewer();
                final long previousDuration = times.getNewer() - times.getOlder();
                times.setOlder(times.getNewer());
                times.setNewer(updates);
                                
                events.add(GameEvent.fromInputType(inputType, duration < previousDuration + REPEAT_MARGIN));
            }
        } catch (final Exception e) {            
            e.printStackTrace(); // TODO ENHANCE
            System.exit(0);
        }
        events.add(GameEvent.UPDATE);
        ++updates;
    }

    @Override
    public List<GameEvent> get() {
        return events;
    }    
}
