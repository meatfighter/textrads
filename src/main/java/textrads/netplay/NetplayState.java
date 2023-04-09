package textrads.netplay;

import com.googlecode.lanterna.input.KeyStroke;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.isBlank;
import textrads.db.Database;
import textrads.db.DatabaseSource;
import textrads.db.NetplayConfig;
import textrads.ui.menu.Chooser;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;
import textrads.ui.message.MessageScreen;
import textrads.ui.message.MessageState;
import textrads.ui.question.LengthValidator;
import textrads.ui.question.NumberValidator;
import textrads.ui.question.Question;
import textrads.ui.question.TextField;
import textrads.util.IOUtil;
import textrads.util.IOUtil.NetworkInterfaceAddress;

public class NetplayState {
    
    static enum State {
        PLAY_AS,
        SERVER_CONFIG,
        SERVER_CONFIG_HOST,
        SERVER_CONFIG_PORT,
        CLIENT_CONFIG,
        CLIENT_CONFIG_HOST,
        CLIENT_CONFIG_PORT,
        CLIENT_CONFIG_HOST_ERROR,
    }
    
    private final Database database = DatabaseSource.getDatabase();
    
    private final Menu playAsMenu = createPlayAsMenu();
    
    private final Chooser<IOUtil.NetworkInterfaceAddress> serverHostChooser 
            = new Chooser<>("Which host will the server accept connections from?");    

    private final ConnectScreenState connectMenuState = new ConnectScreenState();    

    private final Question portQuestion = new Question(new TextField(null, new NumberValidator(1, 65535)));
    private final Question clientHostQuestion = new Question(new TextField(null, new LengthValidator(1, 256), true));
    
    private final MessageScreen messageScreen = new MessageScreen();
    
    private State state;
    private boolean returnToMainMenu;
    
    private String hostname;
    private InetAddress host;
    private int port;
    private byte level;
    
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
            case SERVER_CONFIG:
                updateServerConfig();
                break;
            case SERVER_CONFIG_HOST:
                updateServerConfigHost();
                break;
            case SERVER_CONFIG_PORT:
                updateServerConfigPort();
                break;
            case CLIENT_CONFIG:
                updateClientConfig();
                break;
            case CLIENT_CONFIG_HOST:
                updateClientConfigHost();
                break;
            case CLIENT_CONFIG_PORT:
                updateClientConfigPort();
                break;
            case CLIENT_CONFIG_HOST_ERROR:
                updateClientConfigHostError();
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
                if (c == null) {
                    break;
                }
                switch (Character.toUpperCase(c)) {
                    case 'S':
                        gotoServerConfig();
                        break;
                    case 'C':
                        gotoClientConfig();
                        break;
                }
                break;
            }
        }
    }
    
    private void gotoServerConfig() {
        state = State.SERVER_CONFIG;
        
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        final NetworkInterfaceAddress nia = toNetworkInterfaceAddress(config.getHost());
        host = nia.getAddress();
        hostname = nia.getName();
        port = config.getPort();
        level = config.getLevel();
        
        connectMenuState.init("Server", hostname, Integer.toString(config.getPort()));
    }
    
    private NetworkInterfaceAddress toNetworkInterfaceAddress(final String host) {
        final List<NetworkInterfaceAddress> nias = IOUtil.getNetworkInterfaceAddresses();
        for (final NetworkInterfaceAddress nia : nias) {
            final InetAddress address = nia.getAddress();
            if (address == null) {
                if (host == null) {
                    return nia;
                }
            } else if (address.getHostAddress().equals(host)) {
                return nia;
            }
        }
        return nias.get(0);
    }
    
    private void updateServerConfig() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke == null) {
            return;
        }
        switch (keyStroke.getKeyType()) {
            case Escape:
                gotoPlayAs();
                break;
            case Character: {
                final Character c = keyStroke.getCharacter();
                if (c == null) {
                    break;
                }
                switch (Character.toUpperCase(c)) {
                    case 'S':
                        gotoStartServer();
                        break;
                    case 'H':
                        gotoServerConfigHost();
                        break;
                    case 'P':
                        gotoServerConfigPort();
                        break;
                }
                break;
            }
        }
    }
    
    private void gotoStartServer() {
        
    }
    
    private void updateStartServer() {
        
    }    
    
    private void gotoServerConfigHost() {
        state = State.SERVER_CONFIG_HOST;
        serverHostChooser.init(IOUtil.getNetworkInterfaceAddresses());
    }
    
    private void updateServerConfigHost() {
        serverHostChooser.update();
        
        if (serverHostChooser.isEscPressed()) {
            connectMenuState.init("Server", hostname, Integer.toString(port));
            state = State.SERVER_CONFIG;            
            return;
        }
        
        final NetworkInterfaceAddress nia = serverHostChooser.getSelectedItem();
        if (nia == null) {
            return;
        }
        
        host = nia.getAddress();
        hostname = nia.getName();
        connectMenuState.init("Server", hostname, Integer.toString(port));
        state = State.SERVER_CONFIG;
        
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        database.saveAsync(Database.OtherKeys.SERVER, config.setHost(host == null ? null : host.toString()));
    }    
    
    private void gotoServerConfigPort() {
        state = State.SERVER_CONFIG_PORT;        
        portQuestion.init("Which port will the server accept connections from?", Integer.toString(port));
    }
    
    private void updateServerConfigPort() {
        updateConfigPort("Server", State.SERVER_CONFIG, Database.OtherKeys.SERVER);
    }   
    
    private void updateConfigPort(final String title, final State returnState, final String databaseKey) {
        portQuestion.update();
        
        if (portQuestion.isEscPressed()) {
            connectMenuState.init(title, hostname, Integer.toString(port));
            state = returnState;            
            return;            
        }
        
        if (!portQuestion.isEnterPressed()) {
            return;
        }
        
        port = Integer.parseInt(portQuestion.getValue());
        connectMenuState.init(title, hostname, portQuestion.getValue());
        state = returnState;
        
        final NetplayConfig config = database.get(databaseKey);
        database.saveAsync(databaseKey, config.setPort(port));
    }
    
    private void gotoClientConfig() {
        state = State.CLIENT_CONFIG;
        
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        
        hostname = config.getHost();
        port = config.getPort();
        
        if (isBlank(hostname)) {
            hostname = "localhost";
        }
        
        host = null;
        try {
            host = InetAddress.getByName(hostname);
        } catch (final UnknownHostException e0) {
            hostname = "localhost";
            try {
                host = InetAddress.getByName(hostname);
            } catch (final UnknownHostException e1) {            
                final List<NetworkInterfaceAddress> nias = IOUtil.getNetworkInterfaceAddresses();
                if (nias.size() >= 2) {
                    final NetworkInterfaceAddress nia = nias.get(1);
                    hostname = nia.getName();
                    host = nia.getAddress();                
                } else {
                    try {
                        host = InetAddress.getLocalHost();
                        hostname = host.getHostAddress();
                    } catch (final UnknownHostException e2) {
                    }
                }
            }
        }
        
        connectMenuState.init("Client", hostname, Integer.toString(config.getPort()));
    }
    
    private void updateClientConfig() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke == null) {
            return;
        }
        switch (keyStroke.getKeyType()) {
            case Escape:
                gotoPlayAs();
                break;
            case Character: {
                final Character c = keyStroke.getCharacter();
                if (c == null) {
                    break;
                }
                switch (Character.toUpperCase(c)) {
                    case 'S':
                        gotoStartClient();
                        break;
                    case 'H':
                        gotoClientConfigHost();
                        break;
                    case 'P':
                        gotoClientConfigPort();
                        break;
                }
                break;
            }
        }
    }
    
    private void gotoStartClient() {
        
    }

    private void updateClientStart() {
        
    }    
    
    private void gotoClientConfigHost() {
        state = State.CLIENT_CONFIG_HOST;
        clientHostQuestion.init("Which host will the client connect to?", hostname);
    }
    
    private void updateClientConfigHost() {
        clientHostQuestion.update();
        
        if (clientHostQuestion.isEscPressed()) {            
            connectMenuState.init("Client", hostname, Integer.toString(port));
            state = State.CLIENT_CONFIG;
            return;
        }
        
        if (!clientHostQuestion.isEnterPressed()) {
            return;
        }
        
        final String hn = clientHostQuestion.getValue().trim();
        try {
            host = InetAddress.getByName(hn);
        } catch (final UnknownHostException e) {
            gotoClientConfigHostError();
            return;
        }
        
        hostname = hn;
        connectMenuState.init("Client", hostname, Integer.toString(port));
        state = State.CLIENT_CONFIG;
        
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        database.saveAsync(Database.OtherKeys.CLIENT, config.setHost(hostname)); 
    }
    
    private void gotoClientConfigPort() {
        state = State.CLIENT_CONFIG_PORT;
        portQuestion.init("Which port will the client connect to?", Integer.toString(port));
    }

    private void updateClientConfigPort() {
        updateConfigPort("Client", State.CLIENT_CONFIG, Database.OtherKeys.CLIENT);
    }

    private void gotoClientConfigHostError() {
        state = State.CLIENT_CONFIG_HOST_ERROR;
        messageScreen.init("Invalid Host", "Host IP address not found.", MessageState.MessageType.ERROR);
    }
    
    private void updateClientConfigHostError() {
        messageScreen.update();
        if (messageScreen.isSelected()) {
            state = State.CLIENT_CONFIG_HOST;
            clientHostQuestion.init("Which host will the client connect to?", clientHostQuestion.getValue());
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

    public ConnectScreenState getConnectMenuState() {
        return connectMenuState;
    }

    public Chooser<NetworkInterfaceAddress> getServerHostChooser() {
        return serverHostChooser;
    }

    public Question getPortQuestion() {
        return portQuestion;
    }

    public Question getClientHostQuestion() {
        return clientHostQuestion;
    }

    public MessageScreen getMessageScreen() {
        return messageScreen;
    }
}
