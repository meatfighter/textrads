package textrads;

public enum GameEvent {
    ROTATE_CCW,
    ROTATE_CW,
    SHIFT_LEFT,
    SHIFT_RIGHT,
    SOFT_DROP,
    UPDATE;
    
    static GameEvent fromInputType(final InputType inputType) {
        if (inputType == null) {
            return null;
        }
        switch (inputType) {
            case ROTATE_CCW:
                return ROTATE_CCW;
            case ROTATE_CW:
                return ROTATE_CW;
            case SHIFT_LEFT:
                return SHIFT_LEFT;
            case SHIFT_RIGHT:
                return SHIFT_RIGHT;
            case SOFT_DROP:
                return SOFT_DROP;
            default:
                return null;
        }
    }
}
