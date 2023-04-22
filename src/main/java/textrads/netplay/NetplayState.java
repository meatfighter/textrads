package textrads.netplay;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
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

    private static String WAITING_FOR_CLIENT_STR = "Waiting for client to connect";
    private static String CONNECTING_TO_SERVER_STR = "Connecting to server";
    
    static enum State {
        PLAY_AS,
        
        SERVER_CONFIG,
        SERVER_CONFIG_HOST,
        SERVER_CONFIG_PORT,
        SERVER_START_WAITING,
        SERVER_START_ERROR,
        SERVER_CHANNEL,
        SERVER_WAITING,
        SERVER_ERROR,
        
        CLIENT_CONFIG,
        CLIENT_CONFIG_HOST,
        CLIENT_CONFIG_PORT,
        CLIENT_CONFIG_HOST_ERROR,
        CLIENT_START_WAITING,
        CLIENT_START_ERROR,
        CLIENT_CHANNEL,
        CLIENT_WAITING,
        CLIENT_ERROR,
    }
    
    static enum ChannelState {
        PROMPTING_LEVEL,
        WAITING_TO_PLAY,
        PLAYING,
    }
    
    private final Database database = DatabaseSource.getDatabase();
    
    private final Server server = new Server();
    private final Client client = new Client();
    
    private final Menu playAsMenu = createPlayAsMenu();
    
    private final Chooser<IOUtil.NetworkInterfaceAddress> serverHostChooser 
            = new Chooser<>("Which host will the server accept connections from?");    

    private final ConnectScreenState connectMenuState = new ConnectScreenState();    

    private final Question portQuestion = new Question(new TextField(null, new NumberValidator(1, 65535)));
    private final Question clientHostQuestion = new Question(new TextField(null, new LengthValidator(1, 256), true));
    
    private final MessageScreen messageScreen = new MessageScreen();
    
    private final Question levelQuestion = new Question(new TextField("Level (0\u2500\u250029)?", 
            new NumberValidator(0, 29)));    
    
    private State state;
    private ChannelState channelState;
    private boolean returnToMainMenu;
    
    private String hostname;
    private InetAddress host;
    private int port;
    private byte level;
    
    private MessageChannel channel;
    private boolean channelJustEstablished;
    private boolean serverSubmittedLevel;
    private boolean clientSubmittedLevel;
    
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
            case SERVER_START_WAITING:
                updateServerStartWaiting();
                break;
            case SERVER_START_ERROR:
                updateServerStartError();
                break;
            case SERVER_CHANNEL:
                updateServerChannel();
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
            case CLIENT_START_WAITING:
                updateClientStartWaiting();
                break;
            case CLIENT_START_ERROR:
                updateClientStartError();
                break;
            case CLIENT_CHANNEL:
                updateClientChannel();
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
        
        server.stop();
        
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
                        gotoServerStartWaiting();
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
    
    private void gotoServerStartWaiting() {
        state = State.SERVER_START_WAITING;
        connectMenuState.init("Server", hostname, Integer.toString(port), WAITING_FOR_CLIENT_STR, 
                MessageState.MessageType.WAITING);
        server.setBindAddress(host);
        server.setPort(port);
        server.start();
    }
    
    private void updateServerStartWaiting() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke != null && keyStroke.getKeyType() == KeyType.Escape) {
            server.stop();
            gotoServerConfig();
            return;
        }

        if (!server.isRunning()) {
            final String error = server.getError();
            if (isNotBlank(error)) {
                gotoServerStartError(error);
            } else {
                gotoServerConfig();
            }
            return;
        }
        
        channel = server.getMessageChannel();
        if (channel != null) {
            gotoServerChannel();
        }
    }
    
    private void gotoServerStartError(final String error) {
        state = State.SERVER_START_ERROR;
        connectMenuState.init("Server", hostname, Integer.toString(port), error, MessageState.MessageType.ERROR);
    }
    
    private void updateServerStartError() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke != null && keyStroke.getKeyType() == KeyType.Escape) {
            gotoServerConfig();
        }
    }
    
    private void gotoServerChannel() {
        state = State.SERVER_CHANNEL;
        
        channelState = ChannelState.PROMPTING_LEVEL;
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        level = config.getLevel();
        levelQuestion.init("Server", (level >= 0) ? Integer.toString(level) : "");
        
        channelJustEstablished = true;
        serverSubmittedLevel = false;
        clientSubmittedLevel = false;        
    }
    
    private void updateServerChannel() {
        
        if (channel.isTerminated()) {
            channel = null;
            gotoServerWaiting();
            return;
        }
        
        if (channelJustEstablished) {
            channelJustEstablished = false;
            if (!clientSubmittedLevel) {
                final Message message = channel.getWriteMessage();
                if (message == null) {
                    return;
                }
                message.setType(Message.Type.PROMPT_LEVEL);
                channel.incrementWriteIndex();
            } else {
                // TODO GAME
            }
        }
        
        switch (channelState) {
            case PROMPTING_LEVEL:
                updateServerPromptLevel();
                break;
            case WAITING_TO_PLAY:
                break;
            case PLAYING:
                break;
        }
    }
    
    private void updateServerPromptLevel() {
        levelQuestion.update();
        
        if (levelQuestion.isEscPressed()) {
            gotoServerConfig();
            return;
        }
        
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
    }
    
    private void gotoServerWaiting() {
        state = State.SERVER_WAITING;
        messageScreen.init("Server", WAITING_FOR_CLIENT_STR, MessageState.MessageType.WAITING);
    }
    
    private void updateServerWaiting() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoServerConfig();
            return;
        }
        
        if (!server.isRunning()) {
            final String error = server.getError();
            if (isNotBlank(error)) {
                gotoServerError(error);
            } else {
                gotoServerConfig();
            }
            return;
        }

        channel = server.getMessageChannel();
        if (channel != null) {
            state = State.SERVER_CHANNEL;
            channelJustEstablished = true;
        }        
    }
    
    private void gotoServerError(final String error) {
        state = State.SERVER_ERROR;
        messageScreen.init("Server", error, MessageState.MessageType.ERROR);
    }
    
    private void updateServerError() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoServerConfig();
        }
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
        
        client.stop();
        
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
                        gotoClientStartWaiting();
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
    
    private void gotoClientStartWaiting() {
        state = State.CLIENT_START_WAITING;
        connectMenuState.init("Client", hostname, Integer.toString(port), CONNECTING_TO_SERVER_STR, 
                MessageState.MessageType.WAITING);
        client.setHost(hostname);
        client.setPort(port);
        client.start();
    }
    
    private void updateClientStartWaiting() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke != null && keyStroke.getKeyType() == KeyType.Escape) {
            client.stop();
            gotoClientConfig();
            return;
        }

        if (!client.isRunning()) {
            final String error = client.getError();
            if (isNotBlank(error)) {
                gotoClientStartError(error);
            } else {
                gotoClientConfig();
            }
        }
    }
    
    private void gotoClientStartError(final String error) {
        state = State.CLIENT_START_ERROR;
        connectMenuState.init("Client", hostname, Integer.toString(port), error, MessageState.MessageType.ERROR);
    }
    
    private void updateClientStartError() {
        connectMenuState.update();
        final KeyStroke keyStroke = connectMenuState.getSelection();
        if (keyStroke != null && keyStroke.getKeyType() == KeyType.Escape) {
            gotoClientConfig();
        }
    }

    private void gotoClientChannel() {
        state = State.CLIENT_CHANNEL;
        channelJustEstablished = true;
        
    }
    
    private void updateClientChannel() {
        
        if (channel.isTerminated()) {
            channel = null;
            gotoClientWaiting();
            return;
        }
        
        if (channelJustEstablished) {
            channelJustEstablished = false;
        }
    }

    private void gotoClientWaiting() {
        state = State.CLIENT_WAITING;
        messageScreen.init("Client", CONNECTING_TO_SERVER_STR, MessageState.MessageType.WAITING);
    }
    
    private void updateClientWaiting() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoClientConfig();
            return;
        }
        
        if (!client.isRunning()) {
            final String error = client.getError();
            if (isNotBlank(error)) {
                gotoClientError(error);
            } else {
                gotoClientConfig();
            }
            return;
        }

        channel = server.getMessageChannel();
        if (channel != null) {
            state = State.SERVER_CHANNEL;
            channelJustEstablished = true;
        }        
    }
    
    private void gotoClientError(final String error) {
        state = State.CLIENT_ERROR;
        messageScreen.init("Client", error, MessageState.MessageType.ERROR);
    }
    
    private void updateClientError() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoClientConfig();
        }
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

    public ChannelState getChannelState() {
        return channelState;
    }

    public Question getLevelQuestion() {
        return levelQuestion;
    }
}
