package textrads;

public enum GameEvent {
    
    ROTATE_CCW_PRESSED,
    ROTATE_CCW_REPEATED,
    
    ROTATE_CW_PRESSED,
    ROTATE_CW_REPEATED,
    
    SHIFT_LEFT_PRESSED,
    SHIFT_LEFT_REPEATED,
    
    SHIFT_RIGHT_PRESSED,
    SHIFT_RIGHT_REPEATED,
    
    SOFT_DROP_PRESSED,
    SOFT_DROP_REPEATED,
    
    PAUSE_PRESSED,
    PAUSE_REPEATED,
    
    QUIT_PRESSED,
    QUIT_REPEATED,
    
    UPDATE;
    
    static GameEvent fromInputType(final InputType inputType, final boolean repeated) {
        
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
