package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class InputMap {
    
    private static final String VALUE_ALT = "Alt";
    private static final String VALUE_CTRL = "Ctrl";
     
    private final Map<Character, InputType> charToInputTypeMap = new ConcurrentHashMap<>();
    private final Map<KeyType, InputType> keyTypeToInputTypeMap = new ConcurrentHashMap<>();

    private volatile InputType altInputType;
    private volatile InputType ctrlInputType;
    
    public InputMap() {        
    }
    
    public InputMap(final Properties props) {
        set(props);
    }
    
    public InputType get(final KeyStroke keyStroke) {        
        if (keyStroke.isAltDown()) {
            return altInputType;
        }
        if (keyStroke.isCtrlDown()) {
            return ctrlInputType;
        }
        if (keyStroke.getKeyType() == KeyType.Character) {
            return charToInputTypeMap.get(Character.toLowerCase(keyStroke.getCharacter()));
        }
        return keyTypeToInputTypeMap.get(keyStroke.getKeyType());
    }
    
    public void put(final KeyStroke keyStroke, final InputType inputType) {
        if (altInputType == inputType) {
            altInputType = null;
        }
        if (ctrlInputType == inputType) {
            ctrlInputType = null;
        }
        charToInputTypeMap.entrySet().removeIf(e -> e.getValue() == inputType);
        keyTypeToInputTypeMap.entrySet().removeIf(e -> e.getValue() == inputType);
        
        if (keyStroke.isAltDown()) {
            altInputType = inputType;
        } else if (keyStroke.isCtrlDown()) {
            ctrlInputType = inputType;
        } else if (keyStroke.getKeyType() == KeyType.Character) {
            charToInputTypeMap.put(Character.toLowerCase(keyStroke.getCharacter()), inputType);
        } else {
            keyTypeToInputTypeMap.put(keyStroke.getKeyType(), inputType);
        }        
    }
    
    public void set(final Properties props) {
        altInputType = null;
        ctrlInputType = null;
        charToInputTypeMap.clear();
        keyTypeToInputTypeMap.clear();
        props.entrySet().forEach(e -> {
            try {                
                final String key = e.getKey().toString();
                final String value = e.getValue().toString();
                final InputType inputType = InputType.valueOf(key); 
                if (VALUE_ALT.equals(value)) {
                    altInputType = inputType;
                } else if (VALUE_CTRL.equals(value)) {
                    ctrlInputType = inputType;
                } else if (value.length() == 1) {
                    charToInputTypeMap.put(value.charAt(0), inputType);
                } else {
                    keyTypeToInputTypeMap.put(KeyType.valueOf(value), inputType);
                }
            } catch (final Exception ignore) {                
                ignore.printStackTrace(); // TODO REMOVE
            }
        });
    }
    
    public InputMap fromProperties(final Properties props) {
        return new InputMap(props);
    }
    
    public Properties toProperties() {
        final Properties props = new Properties();
        if (altInputType != null) {
            props.put(altInputType, VALUE_ALT);
        }
        if (ctrlInputType != null) {
            props.put(ctrlInputType, VALUE_CTRL);
        }
        charToInputTypeMap.entrySet().forEach(e -> props.put(e.getValue(), e.getKey()));
        keyTypeToInputTypeMap.entrySet().forEach(e -> props.put(e.getValue(), e.getKey()));
        return props;
    }
}
