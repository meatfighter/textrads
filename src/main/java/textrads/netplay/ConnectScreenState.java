package textrads.netplay;

import com.googlecode.lanterna.input.KeyStroke;
import static com.googlecode.lanterna.input.KeyType.Escape;
import java.util.ArrayList;
import java.util.List;
import textrads.app.Textrads;
import textrads.input.InputSource;
import textrads.ui.menu.BackExitState;
import static textrads.ui.menu.Menu.SELECTION_FRAMES;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;

public class ConnectScreenState {
    
    public static final int MAX_DOTS = 3;
    
    private static final double DOTS_PER_SECOND = 2.0;
    
    private static final float DOTS_PER_FRAME = (float) (DOTS_PER_SECOND / (double) Textrads.FRAMES_PER_SECOND);
    
    public static enum MessageType {        
        INFORM,
        WAITING,
        ERROR,
    }

    private final List<MenuColumn> startMenu = createStartMenu();
    private final List<MenuColumn> setMenu = createSetMenu();
    private final BackExitState backExitState = new BackExitState();    
    
    private String title;
    private String host;
    private String port;
    private String message;
    private MessageType messageType;
        
    private KeyStroke selection;
    private int selectionTimer;
    
    private float dotsFraction;
    private int dots;
    
    private List<MenuColumn> createStartMenu() {
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Start"));
        
        final MenuColumn menuColumn = new MenuColumn(menuItems);
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(menuColumn);
        
        return menuColumns;
    }
    
    private List<MenuColumn> createSetMenu() {
        final List<MenuItem> menuItems0 = new ArrayList<>();
        menuItems0.add(new MenuItem("Set Host", 'H'));
        
        final List<MenuItem> menuItems1 = new ArrayList<>();
        menuItems1.add(new MenuItem("Set Port", 'P'));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems0));
        menuColumns.add(new MenuColumn(menuItems1));
        
        return menuColumns;
    }
    
    public void init(final String title, final String host, final String port) {
        init(title, host, port, null, null);
    }
    
    public void init(final String title, final String host, final String port, 
            final String message, final MessageType messageType) {
        
        this.title = title;
        this.host = host;
        this.port = port;
        this.message = message;
        this.messageType = messageType;
        
        startMenu.forEach(menuColumn -> menuColumn.reset());
        setMenu.forEach(menuColumn -> menuColumn.reset());        
        backExitState.reset();
        
        selection = null;
        selectionTimer = SELECTION_FRAMES;
        
        dotsFraction = 1f;
        dots = 0;
        
        InputSource.clear();
    }
    
    public void update() {
        if (selection == null) {
            for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
                final KeyStroke keyStroke = InputSource.poll();
                if (keyStroke == null) {
                    break;
                }
                handleInput(keyStroke);
            }
        } else {            
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            }
        }
        
        if (messageType == MessageType.WAITING) {
            dotsFraction -= DOTS_PER_FRAME;
            if (dotsFraction <= 0f) {
                dotsFraction += 1f;
                dots = (dots == MAX_DOTS) ? 0 : 1 + dots;
            }
        }
    }
    
    private void handleInput(final KeyStroke keyStroke) {
        switch (keyStroke.getKeyType()) {
            case Escape:
                if (backExitState.isEscapeEnabled()) {
                    selection = keyStroke;
                    backExitState.setEscSelected(true);
                }
                break;
            case Character: {
                if (message != null) {
                    break;
                }
                final Character character = keyStroke.getCharacter();
                if (character == null) {
                    break;
                }
                final char c = Character.toUpperCase(character);
                if (!handleInput(startMenu, keyStroke, c)) {
                    handleInput(setMenu, keyStroke, c);
                }
                break;
            }
        }
    } 
    
    private boolean handleInput(final List<MenuColumn> menuColumns, final KeyStroke keyStroke, final char c) {
        for (final MenuColumn menuColumn : menuColumns) {
            if (menuColumn.handleInput(c)) {
                selection = keyStroke;
                return true;
            }    
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public List<MenuColumn> getStartMenu() {
        return startMenu;
    }

    public List<MenuColumn> getSetMenu() {
        return setMenu;
    }

    public BackExitState getBackExitState() {
        return backExitState;
    }
    
    public KeyStroke getSelection() {
        return (selectionTimer == 0) ? selection : null;
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