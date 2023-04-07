package textrads.ui.message;

import textrads.input.InputSource;
import textrads.ui.menu.BackExitState;

public class MessageScreen {
    
    private final MessageState messageState = new MessageState();
    private final BackExitState backExitState = new BackExitState();

    private String title;
    
    public void init(final String title, final String message, final MessageState.MessageType messageType) {
        this.title = title;
        messageState.init(message, messageType);
        backExitState.reset();
        InputSource.clear();
    }
    
    public void update() {
        messageState.update();
        
        // TODO KEYS
    }
    
    
}
