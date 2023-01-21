package textrads;

public interface InputEvent {
    
    byte ROTATE_CCW_PRESSED = 0;
    byte ROTATE_CCW_REPEATED = 1;
    
    byte ROTATE_CW_PRESSED = 2;
    byte ROTATE_CW_REPEATED = 3;
    
    byte SHIFT_LEFT_PRESSED = 4;
    byte SHIFT_LEFT_REPEATED = 5;
    
    byte SHIFT_RIGHT_PRESSED = 6;
    byte SHIFT_RIGHT_REPEATED = 7;
    
    byte SOFT_DROP_PRESSED = 8;
    byte SOFT_DROP_REPEATED = 9;
    
    byte START_PAUSE_PRESSED = 10;
    byte START_PAUSE_REPEATED = 11;
    
    byte QUIT_PRESSED = 12;
    byte QUIT_REPEATED = 13;
    
    static Byte fromInputType(final InputType inputType, final boolean repeated) {
        
        if (inputType == null) {
            return null;
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
                    return null;
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
                return null;
        }
    }
}
