package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.Screen;
import java.util.ArrayList;
import java.util.List;

public class MonoGameEventSupplier implements GameEventSupplier {
    
    private final List<GameEvent> events = new ArrayList<>();
    
    private final InputMap inputMap;
    private final Screen screen;
    
    public MonoGameEventSupplier(final InputMap inputMap, final Screen screen) {
        this.inputMap = inputMap;
        this.screen = screen;
    }
    
    @Override
    public void update(final App app) {
        events.clear();
        try {
            KeyStroke keyStroke = null;
            while ((keyStroke = screen.pollInput()) != null) {
                final InputType inputType = inputMap.get(keyStroke);
                if (inputType == InputType.QUIT) {
                    app.setTerminate(true); // TODO ENHANCE
                    return;
                }
                final GameEvent event = GameEvent.fromInputType(inputType);
                if (event != null) {
                    events.add(event);
                }
            }
        } catch (final Exception e) {            
            e.printStackTrace(); // TODO ENHANCE
            System.exit(0);
        }
        events.add(GameEvent.UPDATE);
    }

    @Override
    public List<GameEvent> get() {
        return events;
    }    
}
