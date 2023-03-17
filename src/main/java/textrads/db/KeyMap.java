package textrads.db;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import textrads.InputType;

public class KeyMap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final KeyStroke[] DEFAULT_KEY_STROKES = {
        new KeyStroke(KeyType.ArrowLeft),          // shift left
        new KeyStroke(KeyType.ArrowRight),         // shift right
        new KeyStroke(KeyType.ArrowDown),          // soft drop
        new KeyStroke('z', false, false, false),   // rotate counterclockwise
        new KeyStroke('x', false, false, false),   // rotate clockwise
        new KeyStroke(KeyType.Enter),              // pause
    };
    
    private final Map<KeyStroke, InputType> keyStrokeToInputTypeMap;
    
    public KeyMap() {        
        this(DEFAULT_KEY_STROKES);
    }
    
    public KeyMap(final KeyStroke[] keyStrokes) {
        final Map<KeyStroke, InputType> map = new HashMap<>();
        final InputType[] inputTypes = InputType.values();        
        for (int i = inputTypes.length - 1; i >= 0; --i) {
            map.put(keyStrokes[i], inputTypes[i]);
        }
        keyStrokeToInputTypeMap = Collections.unmodifiableMap(map);
    }
    
    public InputType get(final KeyStroke keyStroke) {        
        return keyStrokeToInputTypeMap.get(keyStroke);
    }
}