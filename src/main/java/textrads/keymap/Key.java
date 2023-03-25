package textrads.keymap;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.Serializable;
import java.util.Objects;

import static org.apache.commons.lang3.CharUtils.isAsciiPrintable;

public class Key implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final KeyType keyType;
    private final Character character;
    
    public Key(final KeyStroke keyStroke) {
        this(keyStroke.getKeyType(), keyStroke.getCharacter());
    }
    
    public Key(final KeyType keyType) {
        this(keyType, null);
    }
    
    public Key(final char c) {
        this(KeyType.Character, c);
    }
    
    public Key(final KeyType keyType, final Character character) {
        this.keyType = keyType;
        this.character = character;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public Character getCharacter() {
        return character;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 47 * hash + Objects.hashCode(this.keyType);
        hash = 47 * hash + Objects.hashCode(this.character);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Key other = (Key) obj;
        if (this.keyType != other.keyType) {
            return false;
        }
        return Objects.equals(this.character, other.character);
    }
    
    @Override
    public String toString() {
        if (keyType == null) {
            return "";
        }
        switch (keyType) {
            case ArrowDown:
                return "Down Arrow";
            case ArrowRight:
                return "Right Arrow";
            case ArrowLeft:
                return "Left Arrow";
            case ArrowUp:
                return "Up Arrow";
            case Backspace:
                return "Backspace";
            case Character: {
                if (character == null || !isAsciiPrintable(character)) {
                    return "";
                }
                if (character == ' ') {
                    return "Space Bar";
                }
                return String.valueOf(Character.toUpperCase(character));
            }
            case Delete:
                return "Delete";
            case End:
                return "End";
            case Enter:
                return "Enter";
            case Escape:
                return "Escape";
            case F1:
                return "F1";    
            case F10:
                return "F10";
            case F11:
                return "F11";
            case F12:
                return "F12";
            case F13:
                return "F13";
            case F14:
                return "F14";
            case F15:
                return "F15";
            case F16:
                return "F16";
            case F17:
                return "F17";
            case F18:
                return "F18";    
            case F19:
                return "F19";
            case F2:
                return "F2";
            case F3:
                return "F3";
            case F4:
                return "F4";
            case F5:
                return "F5";
            case F6:
                return "F6";
            case F7:
                return "F7";
            case F8:
                return "F8";
            case F9:
                return "F9";
            case Home:
                return "Home";
            case Insert:
                return "Insert";
            case PageDown:
                return "Page Down";
            case PageUp:
                return "Page Up";
            case Tab:
                return "Tab";
            default:
                return "";
        }
    }    
}
