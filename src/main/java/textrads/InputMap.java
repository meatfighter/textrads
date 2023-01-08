package textrads;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InputMap {
    
    private static final String VALUE_ALT = "Alt";
    private static final String VALUE_CTRL = "Ctrl";
    private static final String VALUE_SHIFT = "Shift";
     
    private final Map<Character, InputType> charToInputTypeMap = new ConcurrentHashMap<>();
    private final Map<KeyType, InputType> keyTypeToInputTypeMap = new ConcurrentHashMap<>();

    private volatile InputType altInputType;
    private volatile InputType ctrlInputType;
    private volatile InputType shiftInputType;
    
    public static InputMap fromStringsList(final List<String[]> list) {
        return new InputMap(list);
    }    
    
    public InputMap() {
        reset();
    }
    
    public InputMap(final List<String[]> list) {
        set(list);
    }
    
    public InputType get(final KeyStroke keyStroke) {        
        if (keyStroke.isAltDown()) {
            return altInputType;
        }
        if (keyStroke.isCtrlDown()) {
            return ctrlInputType;
        }
        if (keyStroke.isShiftDown()) {
            return shiftInputType;
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
        if (shiftInputType == inputType) {
            shiftInputType = null;
        }
        charToInputTypeMap.entrySet().removeIf(e -> e.getValue() == inputType);
        keyTypeToInputTypeMap.entrySet().removeIf(e -> e.getValue() == inputType);
        
        if (keyStroke.isAltDown()) {
            altInputType = inputType;
        } else if (keyStroke.isCtrlDown()) {
            ctrlInputType = inputType;
        } else if (keyStroke.isShiftDown()) {
            shiftInputType = inputType;
        } else if (keyStroke.getKeyType() == KeyType.Character) {
            charToInputTypeMap.put(Character.toLowerCase(keyStroke.getCharacter()), inputType);
        } else {
            keyTypeToInputTypeMap.put(keyStroke.getKeyType(), inputType);
        }        
    }
    
    public void clear() {
        altInputType = null;
        ctrlInputType = null;
        shiftInputType = null;
        charToInputTypeMap.clear();
        keyTypeToInputTypeMap.clear();
    }
    
    public void reset() {
        clear();        
        charToInputTypeMap.put('z', InputType.ROTATE_CCW);
        charToInputTypeMap.put('x', InputType.ROTATE_CW);        
        keyTypeToInputTypeMap.put(KeyType.ArrowLeft, InputType.SHIFT_LEFT);
        keyTypeToInputTypeMap.put(KeyType.ArrowRight, InputType.SHIFT_RIGHT);
        keyTypeToInputTypeMap.put(KeyType.ArrowDown, InputType.SOFT_DROP);
        keyTypeToInputTypeMap.put(KeyType.Enter, InputType.PAUSE);
        keyTypeToInputTypeMap.put(KeyType.Escape, InputType.QUIT);
    }
    
    public void set(final List<String[]> list) {
        clear();
        list.forEach(s -> {
            if (s == null || s.length != 2 || s[0] == null || s[1] == null) {
                return;
            }
            final InputType inputType = EnumUtil.valueOf(InputType.class, s[0]);
            if (inputType == null) {
                return;
            }
            if (s[1].length() == 1) {
                charToInputTypeMap.put(Character.toLowerCase(s[1].charAt(0)), inputType);
                return;
            } 
            final KeyType keyType = EnumUtil.valueOf(KeyType.class, s[1]);
            if (keyType == null) {
                return;
            }
            keyTypeToInputTypeMap.put(keyType, inputType);
        });
    }
    
    public List<String[]> toStringsList() {
        final List<String[]> list = new ArrayList<>();
        if (altInputType != null) {
            list.add(new String[] { altInputType.toString(), VALUE_ALT });
        }
        if (ctrlInputType != null) {
            list.add(new String[] { ctrlInputType.toString(), VALUE_CTRL });
        }
        if (shiftInputType != null) {
            list.add(new String[] { shiftInputType.toString(), VALUE_SHIFT });
        }
        charToInputTypeMap.entrySet()
                .forEach(e -> list.add(new String[] { e.getValue().toString(), e.getKey().toString() }));
        keyTypeToInputTypeMap.entrySet()
                .forEach(e -> list.add(new String[] { e.getValue().toString(), e.getKey().toString() }));        
        return list;
    }
}
