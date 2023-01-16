package textrads;

public interface GameEvent {
    
    int ROTATE_CCW_PRESSED = 0;
    int ROTATE_CCW_REPEATED = 1;
    
    int ROTATE_CW_PRESSED = 2;
    int ROTATE_CW_REPEATED = 3;
    
    int SHIFT_LEFT_PRESSED = 4;
    int SHIFT_LEFT_REPEATED = 5;
    
    int SHIFT_RIGHT_PRESSED = 6;
    int SHIFT_RIGHT_REPEATED = 7;
    
    int SOFT_DROP_PRESSED = 8;
    int SOFT_DROP_REPEATED = 9;
    
    int PAUSE_PRESSED = 10;
    int PAUSE_REPEATED = 11;
    
    int QUIT_PRESSED = 12;
    int QUIT_REPEATED = 13;
    
    int UPDATE = 14;
    
    static Integer fromInputType(final InputType inputType, final boolean repeated) {
        
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
                case PAUSE:
                    return PAUSE_REPEATED;
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
            case PAUSE:
                return PAUSE_PRESSED;
            case QUIT:
                return QUIT_PRESSED;    
            default:
                return null;
        }
    }
}
