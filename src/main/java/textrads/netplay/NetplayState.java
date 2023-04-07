package textrads.netplay;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.List;
import textrads.ui.menu.Chooser;
import textrads.ui.menu.ChooserRenderer;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;
import textrads.util.IOUtil;

public class NetplayState {
    
    static enum State {
        PLAY_AS,
    }
    
    private final Menu playAsMenu = createPlayAsMenu();
    
    private final Chooser<IOUtil.NetworkInterfaceAddress> chooser 
            = new Chooser<>("Which host will the server accept connections from?");
    private final ChooserRenderer chooserRenderer = new ChooserRenderer();

    // TODO:
    //        chooser.init(IOUtil.getNetworkInterfaceAddresses());
    
    private final ConnectScreenState connectMenuState = new ConnectScreenState();
    private final ConnectScreenRenderer connectMenuRenderer = new ConnectScreenRenderer();

            // TODO TESTING
//            connectMenuState.init("Server", "localhost", "8080", 
//                    "ERROR: Failed to start server.", 
//                    ConnectScreenState.MessageType.ERROR); 
    
    // private final Question question = new Question(new TextField(null, new MaxLengthValidator(256), true));
    // question.init("Which host will the client connect to?", "127.0.0.1");
    
    private State state;
    private boolean returnToMainMenu;
    
    private Menu createPlayAsMenu() {
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Play as Server", 'S'));
        menuItems.add(new MenuItem("Play as Client", 'C'));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems));
        
        return new Menu(menuColumns, "Vs. Human");
    }    
    
    public void reset() {
        returnToMainMenu = false;
        gotoPlayAs();
    }
    
    public void update() {
        switch (state) {
            case PLAY_AS:
                updatePlayAs();
                break;
        }
    }
    
    private void gotoPlayAs() {
        state = State.PLAY_AS;
        playAsMenu.reset();
    }
    
    private void updatePlayAs() {
        playAsMenu.update();
        final KeyStroke keyStroke = playAsMenu.getSelection();
        if (keyStroke == null) {
            return;
        }
        switch (keyStroke.getKeyType()) {
            case Escape:
                returnToMainMenu = true;
                break;
            case Character: {
                final Character c = keyStroke.getCharacter();
                switch (Character.toUpperCase(c)) {
                    case 'S':
                        break;
                    case 'C':
                        break;
                }
                break;
            }
        }
    }

    public State getState() {
        return state;
    }

    public Menu getPlayAsMenu() {
        return playAsMenu;
    }

    public boolean isReturnToMainMenu() {
        return returnToMainMenu;
    }
}
