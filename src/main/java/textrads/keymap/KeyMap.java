package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import textrads.InputType;

public class KeyMap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final KeyDescription[] DEFAULT_KEY_DESCRIPTIONS = {
        new KeyDescription(KeyType.ArrowLeft),   // shift left
        new KeyDescription(KeyType.ArrowRight),  // shift right
        new KeyDescription(KeyType.ArrowDown),   // soft drop
        new KeyDescription('z'),                 // rotate counterclockwise
        new KeyDescription('x'),                 // rotate clockwise
        new KeyDescription(KeyType.Enter),       // pause
    };
    
    private final KeyDescription[] keyDescriptions;
    
    private transient final Map<KeyStroke, InputType> keyStrokeToInputTypeMap; // see readResolve
    
    public KeyMap() {        
        this(DEFAULT_KEY_DESCRIPTIONS);
    }
    
    public KeyMap(final KeyDescription[] keyDescriptions) {
        this.keyDescriptions = keyDescriptions;
        final Map<KeyStroke, InputType> map = new LinkedHashMap<>();
        final InputType[] inputTypes = InputType.values();        
        for (int i = inputTypes.length - 1; i >= 0; --i) {
            map.put(keyDescriptions[i].toKeyStroke(), inputTypes[i]);
        }
        keyStrokeToInputTypeMap = Collections.unmodifiableMap(map);
    }
    
    // Sets the transient final fields.
    private Object readResolve() {
        return new KeyMap(keyDescriptions);
    }
    
    public InputType get(final KeyStroke keyStroke) {        
        return keyStrokeToInputTypeMap.get(keyStroke);
    }
    
    public KeyDescription[] getKeyDescriptions() {
        return keyDescriptions;
    }
}