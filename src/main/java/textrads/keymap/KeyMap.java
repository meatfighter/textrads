package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import textrads.input.InputType;

public class KeyMap implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final Key[] DEFAULT_KEYS = {
        new Key(KeyType.ArrowLeft),   // shift left
        new Key(KeyType.ArrowRight),  // shift right
        new Key(KeyType.ArrowDown),   // soft drop
        new Key('z'),                 // rotate counterclockwise
        new Key('x'),                 // rotate clockwise
        new Key(KeyType.Enter),       // pause
        new Key(KeyType.Escape),      // give up
    };
    
    private final Key[] keys;
    
    private transient final Map<KeyStroke, InputType> keyStrokeToInputTypeMap; // see readResolve
    
    public KeyMap() {        
        this(DEFAULT_KEYS);
    }
    
    public KeyMap(final Key[] keys) {
        this.keys = keys;
        final Map<KeyStroke, InputType> map = new HashMap<>();
        final InputType[] inputTypes = InputType.values();        
        for (int i = inputTypes.length - 1; i >= 0; --i) {
            map.put(keys[i].toKeyStroke(), inputTypes[i]);
        }
        keyStrokeToInputTypeMap = Collections.unmodifiableMap(map);
    }
    
    // Sets the transient final fields.
    private Object readResolve() {
        return new KeyMap(keys);
    }
    
    public InputType get(final KeyStroke keyStroke) {        
        return keyStrokeToInputTypeMap.get(keyStroke);
    }
    
    public Key[] getKeys() {
        return keys;
    }
}