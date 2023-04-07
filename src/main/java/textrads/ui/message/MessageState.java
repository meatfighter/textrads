package textrads.ui.message;

import textrads.app.Textrads;

public class MessageState {
    
    public static final int MAX_DOTS = 3;
    
    private static final double DOTS_PER_SECOND = 2.0;
    
    private static final float DOTS_PER_FRAME = (float) (DOTS_PER_SECOND / (double) Textrads.FRAMES_PER_SECOND);
    
    public static enum MessageType {        
        INFORM,
        WAITING,
        ERROR,
    }    
    
    private String message;
    private MessageType messageType;
    
    private float dotsFraction;
    private int dots;    

    public void init(final String message, final MessageType messageType) {
        this.message = message;
        this.messageType = messageType;
        
        dotsFraction = 1f;
        dots = 0;
    }
    
    public void update() {
        if (messageType == MessageType.WAITING) {
            dotsFraction -= DOTS_PER_FRAME;
            if (dotsFraction <= 0f) {
                dotsFraction += 1f;
                dots = (dots == MAX_DOTS) ? 0 : 1 + dots;
            }
        }
    }
    
    public String getMessage() {
        return message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public int getDots() {
        return dots;
    }    
}
