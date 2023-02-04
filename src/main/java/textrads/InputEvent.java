package textrads;

public interface InputEvent {
    
    byte NOTHING_PRESSED = 0;
    
    byte ROTATE_CCW_PRESSED = 1;
    byte ROTATE_CCW_REPEATED = 2;
    
    byte ROTATE_CW_PRESSED = 3;
    byte ROTATE_CW_REPEATED = 4;
    
    byte SHIFT_LEFT_PRESSED = 5;
    byte SHIFT_LEFT_REPEATED = 6;
    
    byte SHIFT_RIGHT_PRESSED = 7;
    byte SHIFT_RIGHT_REPEATED = 8;
    
    byte SOFT_DROP_PRESSED = 9;
    byte SOFT_DROP_REPEATED = 10;
    
    byte START_PAUSE_PRESSED = 11;
    byte START_PAUSE_REPEATED = 12;
    
    byte QUIT_PRESSED = 13;
    byte QUIT_REPEATED = 14;
    
    static String toString(final byte value) {
        switch (value) {
            case NOTHING_PRESSED:
                return "NOTHING_PRESSED";
            case ROTATE_CCW_PRESSED:
                return "ROTATE_CCW_PRESSED";
            case ROTATE_CCW_REPEATED:
                return "ROTATE_CCW_REPEATED";
            case ROTATE_CW_PRESSED:
                return "ROTATE_CW_PRESSED";
            case ROTATE_CW_REPEATED:
                return "ROTATE_CW_REPEATED";
            case SHIFT_LEFT_PRESSED:
                return "SHIFT_LEFT_PRESSED";
            case SHIFT_LEFT_REPEATED:
                return "SHIFT_LEFT_REPEATED";
            case SHIFT_RIGHT_PRESSED:
                return "SHIFT_RIGHT_PRESSED";
            case SHIFT_RIGHT_REPEATED:
                return "SHIFT_RIGHT_REPEATED";
            case SOFT_DROP_PRESSED:
                return "SOFT_DROP_PRESSED";
            case SOFT_DROP_REPEATED:
                return "SOFT_DROP_REPEATED";
            case START_PAUSE_PRESSED:
                return "START_PAUSE_PRESSED";
            case START_PAUSE_REPEATED:
                return "START_PAUSE_REPEATED";
            case QUIT_PRESSED:
                return "QUIT_PRESSED";
            case QUIT_REPEATED:
                return "QUIT_REPEATED";
            default:
                throw new IllegalArgumentException("Invalid value: " + value);
        }
    }
    
    static Byte fromInputType(final InputType inputType, final boolean repeated) {
        
        if (inputType == null) {
            return NOTHING_PRESSED;
        }
        
        if (repeated) {
            switch (inputType) {
                case ROTATE_CCW:
                    return ROTATE_CCW_REPEATED;
                case ROTATE_CW:
                    return ROTATE_CW_REPEATED;
                case SHIFT_LEFT:
                    return SHIFT_LEFT_REPEATED;
                case SHIFT_RIGHT:
                    return SHIFT_RIGHT_REPEATED;
                case SOFT_DROP:
                    return SOFT_DROP_REPEATED;
                case START_PAUSE:
                    return START_PAUSE_REPEATED;
                case QUIT:
                    return QUIT_REPEATED;
                default:
                    return NOTHING_PRESSED;
            }
        }
        
        switch (inputType) {
            case ROTATE_CCW:
                return ROTATE_CCW_PRESSED;
            case ROTATE_CW:
                return ROTATE_CW_PRESSED;
            case SHIFT_LEFT:
                return SHIFT_LEFT_PRESSED;
            case SHIFT_RIGHT:
                return SHIFT_RIGHT_PRESSED;
            case SOFT_DROP:
                return SOFT_DROP_PRESSED;
            case START_PAUSE:
                return START_PAUSE_PRESSED;
            case QUIT:
                return QUIT_PRESSED;    
            default:
                return NOTHING_PRESSED;
        }
    }
}
