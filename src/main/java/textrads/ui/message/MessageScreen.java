package textrads.ui.message;

import com.googlecode.lanterna.input.KeyStroke;
import textrads.input.InputSource;
import textrads.ui.menu.BackExitState;
import static textrads.ui.menu.Menu.SELECTION_FRAMES;

public class MessageScreen {
    
    private static final String DEFAULT_CONTINUE_LABEL = "Continue";
    private static final String DEFAULT_CANCEL_LABEL = "Back";
    
    private final MessageState messageState = new MessageState();
    private final BackExitState continueState;
    private final BackExitState cancelState;

    private String title;
    private boolean selected;
    private int selectionTimer;
    
    public MessageScreen() {
        this(DEFAULT_CONTINUE_LABEL, DEFAULT_CANCEL_LABEL);
    }
    
    public MessageScreen(final String continueLabel, final String cancelLabel) {
        continueState = new BackExitState("Enter", continueLabel);
        cancelState = new BackExitState("Esc", cancelLabel);
    }

    public void init(final String title, final String message, final MessageState.MessageType messageType) {
        this.title = title;
        messageState.init(message, messageType);
        selected = false;
        selectionTimer = SELECTION_FRAMES;
        continueState.reset();
        cancelState.reset();
        InputSource.clear();
    }
       
    public void update() {
        if (selected) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            }
        } else {    
            messageState.update();
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                handleInput(keyStroke);
            }
        } 
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                if (messageState.getMessageType() == MessageState.MessageType.WAITING) {
                    selected = true;
                    cancelState.setSelected(true);
                }
                break;
            case Enter:
                if (messageState.getMessageType() != MessageState.MessageType.WAITING) {
                    selected = true;
                    continueState.setSelected(true);
                }
                break;
        }
    }
    
    public boolean isSelected() {
        return (selectionTimer == 0) ? selected : false;
    }    

    public MessageState getMessageState() {
        return messageState;
    }
    
    public BackExitState getBackExitState() {
        return messageState.getMessageType() == MessageState.MessageType.WAITING ? cancelState : continueState;
    }

    public String getTitle() {
        return title;
    }
}
