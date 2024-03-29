package textrads.netplay;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import textrads.db.Database;
import textrads.db.DatabaseSource;
import textrads.db.NetplayConfig;
import textrads.game.GameState;
import textrads.game.GameStateSource;
import textrads.game.MonoGameState;
import textrads.input.InputEvent;
import textrads.input.InputEventList;
import textrads.input.InputEventSource;
import textrads.input.InputSource;
import textrads.ui.menu.BackExitState;
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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import textrads.game.Status;

public class NetplayState {

    private static final String WAITING_FOR_CLIENT_TO_CONNECT_STR = "Waiting for client to connect";
    private static final String WAITING_FOR_SERVER_STR = "Waiting for server";    
    private static final String CONNECTING_TO_SERVER_STR = "Connecting to server";        
    private static final String CLIENT_MIGHT_RESIGN_STR = "Client might resign";
    private static final String DISCONNECTING_STR = "Disconnecting";
    private static final String RESUMING_GAME_STR = "Resuming game";    
    
    private static final String WAITING_FOR_CLIENTS_LEVEL_STR = "Waiting for client's level";
    private static final String WAITING_FOR_SERVERS_LEVEL_STR = "Waiting for server's level";
    
    private static final String WAITING_FOR_CLIENT_TO_CONTINUE_STR = "Waiting for client to continue";
    private static final String WAITING_FOR_SERVER_TO_CONTINUE_STR = "Waiting for server to continue";
    
    static enum State {
        PLAY_AS,
        
        SERVER_CONFIG,
        SERVER_CONFIG_HOST,
        SERVER_CONFIG_PORT,
        SERVER_START_WAITING,
        SERVER_START_ERROR,
        SERVER_CHANNEL,
        SERVER_WAITING,
        SERVER_INFORMING,
        SERVER_ERROR,
        
        CLIENT_CONFIG,
        CLIENT_CONFIG_HOST,
        CLIENT_CONFIG_PORT,
        CLIENT_CONFIG_HOST_ERROR,
        CLIENT_START_WAITING,
        CLIENT_START_ERROR,
        CLIENT_CHANNEL,
        CLIENT_WAITING,
        CLIENT_INFORMING,
        CLIENT_ERROR,
    }
    
    static enum ChannelState {
        GETTING_LEVEL,
        WAITING_FOR,
        PLAYING,
        CONTINUE,
        GIVE_UP,
    }
    
    private final Status[] statuses = { new Status(), new Status() };
    
    private final Database database = DatabaseSource.getDatabase();
    private final InputQueue inputQueue = new InputQueue();
    
    private final Server server = new Server();
    private final Client client = new Client();
    
    private final Menu playAsMenu = createPlayAsMenu();
    private final Menu giveUpMenu = createGiveUpMenu();
    
    private final Chooser<IOUtil.NetworkInterfaceAddress> serverHostChooser 
            = new Chooser<>("Which host will the server accept connections from?");    

    private final ConnectScreenState connectMenuState = new ConnectScreenState();    

    private final Question portQuestion = new Question(new TextField(null, new NumberValidator(1, 65535)));
    private final Question clientHostQuestion = new Question(new TextField(null, new LengthValidator(1, 256), true));
    
    private final MessageScreen messageScreen = new MessageScreen();
    private final MessageScreen disconnectMessageScreen = new MessageScreen("Continue", "Disconnect");
    
    private final Question levelQuestion = new Question(new TextField("Level (0\u2500\u250029)?", 
            new NumberValidator(0, 29)), new BackExitState("Disconnect"));
    
    private State state;
    private ChannelState channelState;
    private boolean returnToMainMenu;
    
    private String hostname;
    private InetAddress host;
    private int port;
    private boolean serverMayResign;
    private boolean clientMayResign;
    private boolean clientAckedGameState;
    private boolean waitClientContinue;
    private boolean requestedDisconnect;
    private int selectionTimer;
    
    private MessageChannel channel;
    private boolean channelJustEstablished;
    
    private Menu createPlayAsMenu() {
        final List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem("Play as Server", 'S'));
        menuItems.add(new MenuItem("Play as Client", 'C'));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems));
        
        return new Menu(menuColumns, "Vs. Human");
    }

    private Menu createGiveUpMenu() {
        final List<MenuItem> menuItems0 = new ArrayList<>();
        menuItems0.add(new MenuItem("Yes"));
        
        final List<MenuItem> menuItems1 = new ArrayList<>();
        menuItems1.add(new MenuItem("No"));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems0));
        menuColumns.add(new MenuColumn(menuItems1));
        
        return new Menu(menuColumns, "Give Up?", new BackExitState("Disconnect"));
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
            case SERVER_WAITING:
                updateServerWaiting();
                break;
            case SERVER_INFORMING:
                updateServerInforming();
                break;
            case SERVER_ERROR:
                updateServerError();
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
            case CLIENT_WAITING:
                updateClientWaiting();
                break;
            case CLIENT_INFORMING:
                updateClientInforming();
                break;
            case CLIENT_ERROR:
                updateClientError();
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
        inputQueue.clear();
        
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        final NetworkInterfaceAddress nia = toNetworkInterfaceAddress(config.getHost());
        host = nia.getAddress();
        hostname = nia.getName();
        port = config.getPort();
        
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
                    case 'R':
                        resetServerConfigReset();
                        break;
                }
                break;
            }
        }
    }
    
    private void gotoServerStartWaiting() {
        state = State.SERVER_START_WAITING;
        connectMenuState.init("Server", hostname, Integer.toString(port), WAITING_FOR_CLIENT_TO_CONNECT_STR, 
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
        channelJustEstablished = true;
        statuses[0].reset();
        statuses[0].setLevel(-1);
        statuses[1].reset();
        statuses[1].setLevel(-1);
        clientAckedGameState = false;
        waitClientContinue = false;
        requestedDisconnect = false;
        clientMayResign = false;
        serverMayResign = false;        
        inputQueue.clear();
        gotoServerGettingLevel();
    }
    
    private void updateServerChannel() {
        
        if (channel.isTerminated()) {
            channel = null;
            channelJustEstablished = false;
            clientAckedGameState = false;
            inputQueue.clear();
            if (requestedDisconnect) {
                requestedDisconnect = false;
                server.stop();
                gotoServerInforming("Client disconnected.");
            } else {
                gotoServerWaiting();
            }
            return;
        }
        
        if (channelJustEstablished) {
            channelJustEstablished = false;
            
            if (clientMayResign) {
                channel.write(Message.Type.GET_GIVE_UP);
                gotoServerWaitingFor(CLIENT_MIGHT_RESIGN_STR);
            } else if (serverMayResign) {
                channel.write(Message.Type.WAIT_GIVE_UP);
            } else if (!waitClientContinue && statuses[1].getLevel() < 0) {
                channel.write(Message.Type.GET_LEVEL);
                if (statuses[0].getLevel() >= 0) {
                    gotoServerWaitingFor(WAITING_FOR_CLIENTS_LEVEL_STR);
                }
            } else if (!waitClientContinue && statuses[0].getLevel() < 0) {
                channel.write(Message.Type.WAIT_LEVEL);
            } else if (!clientAckedGameState) {
                final GameState gameState = GameStateSource.getState();
                final byte selection = gameState.getSelection();
                gameState.setSelection((byte) -1);
                channel.write(Message.Type.GAME_STATE, gameState);
                gameState.setSelection(selection);
                if (waitClientContinue && statuses[0].getLevel() >= 0) {
                    disconnectMessageScreen.init("Server", (statuses[1].getLevel() >= 0) 
                            ? WAITING_FOR_CLIENT_TO_CONTINUE_STR : WAITING_FOR_CLIENTS_LEVEL_STR, 
                            MessageState.MessageType.WAITING);
                }
            }
        }
        
        for (int i = channel.getAvailableMessages() - 1; i >= 0; --i) {
            final Message message = channel.getReadMessage();
            if (message == null) {
                channel.stop();
                return;
            }
            switch (message.getType()) {
                case Message.Type.LEVEL: {
                    final Byte level;
                    try {
                        level = IOUtil.fromByteArray(message.getData());
                    } catch (final IOException | ClassNotFoundException ignored) {
                        channel.stop();
                        return;
                    }
                    if (level == null || level < 0) {
                        channel.stop();
                        return;                    
                    }
                    statuses[1].setLevel(level);
                    if (statuses[0].getLevel() >= 0) {
                        initGameState();
                        channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
                    }
                    break;
                }
                case Message.Type.ACK_GAME_STATE:
                    clientAckedGameState = true;
                    
                    if (waitClientContinue) {
                        channel.write(Message.Type.GET_CONTINUE);
                    } else if (channelState == ChannelState.CONTINUE) {
                        channel.write(Message.Type.WAIT_CONTINUE);
                    } else {
                        gotoServerPlaying();
                    }
                    break;
                case Message.Type.INPUT_EVENTS:
                    inputQueue.enqueue(message.getInputEvents(0));
                    break;
                case Message.Type.CONTINUE:                    
                    waitClientContinue = false;
                    if (statuses[1].getLevel() < 0) {
                        channel.write(Message.Type.GET_LEVEL);
                    } else if (channelState != ChannelState.CONTINUE) {
                        clientAckedGameState = false;
                        initGameState();
                        channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
                    } 
                    break;
                case Message.Type.REQUEST_DISCONNECT:
                    requestedDisconnect = true;
                    channel.write(Message.Type.DISCONNECT);
                    break;
                case Message.Type.DISCONNECT:
                    gotoServerConfig();
                    break;
                case Message.Type.RESUME_GAME:                    
                    clientAckedGameState = false;
                    clientMayResign = serverMayResign = false;
                    channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
                    gotoServerWaitingFor(RESUMING_GAME_STR);
                    break;
                case Message.Type.GIVE_UP:
                    gotoServerChannel();
                    break;
            }
            channel.incrementReadIndex();
        }
        
        switch (channelState) {
            case GETTING_LEVEL:
                updateServerGettingLevel();
                break;
            case WAITING_FOR:
                updateServerWaitingFor();
                break;
            case PLAYING:
                updateServerPlaying();
                break;
            case CONTINUE:
                updateServerContinue();
                break;
            case GIVE_UP:
                updateServerGiveUp();
                break;
        }
    }
    
    private void initGameState() {
        final GameState gameState = GameStateSource.getState();
        gameState.init(GameState.Mode.VS_HUMAN, statuses, ThreadLocalRandom.current().nextLong(), 0, 0, false);
        final MonoGameState[] monoGameStates = gameState.getStates();
        monoGameStates[0].setLocalPlayer(true);
        monoGameStates[1].setLocalPlayer(false);
    }
    
    private void gotoServerGettingLevel() {
        channelState = ChannelState.GETTING_LEVEL;
        
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        final byte level = config.getLevel();
        levelQuestion.init("Server", (level >= 0) ? Integer.toString(level) : "");
    }
    
    private void updateServerGettingLevel() {
        levelQuestion.update();
        
        if (levelQuestion.isEscPressed()) {
            requestedDisconnect = true;            
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoServerWaitingFor(DISCONNECTING_STR);
            return;
        }
        
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
        
        statuses[0].setLevel(Integer.parseInt(levelQuestion.getValue()));
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        database.saveAsync(Database.OtherKeys.SERVER, config.setLevel((byte) statuses[0].getLevel()));
        
        if (statuses[1].getLevel() >= 0) {
            initGameState();
            channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
        }
                        
        gotoServerWaitingFor(WAITING_FOR_CLIENTS_LEVEL_STR);
    }
    
    private void gotoServerWaitingFor(final String reason) {
        channelState = ChannelState.WAITING_FOR;
        disconnectMessageScreen.init("Server", reason, MessageState.MessageType.WAITING);
    }
    
    private void updateServerWaitingFor() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {
            requestedDisconnect = true;
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoServerWaitingFor(DISCONNECTING_STR);
        }
    }
    
    private void gotoServerPlaying() {
        channelState = ChannelState.PLAYING;
        channel.write(Message.Type.PLAY);
        InputSource.clear();
    }
    
    private void updateServerPlaying() {

        final GameState gameState = GameStateSource.getState();
        if (gameState.isContinueMessage()) {
            gotoServerContinue();
            return;
        }
        
        final Message message = channel.getWriteMessage();
        if (message == null) {
            channel.stop();
            return;
        }
        
        message.setType(Message.Type.INPUT_EVENTS);
        
        final InputEventList serverEvents = message.getInputEvents(0);
        InputEventSource.poll(serverEvents);
        
        final InputEventList clientEvents = message.getInputEvents(1);
        inputQueue.dequeue(clientEvents);
        
        channel.incrementWriteIndex();
                
        for (int i = 0, end = serverEvents.size(); i < end; ++i) {
            final byte event = serverEvents.get(i);
            if (event == InputEvent.GIVE_UP_PRESSED) {
                gotoServerGiveUp();
                return;
            }
            gameState.handleInputEvent(event, 0);
        }
        
        for (int i = 0, end = clientEvents.size(); i < end; ++i) {
            final byte event = clientEvents.get(i);
            if (event == InputEvent.GIVE_UP_PRESSED) {
                clientMayResign = true;
                channel.write(Message.Type.GET_GIVE_UP);
                gotoServerWaitingFor(CLIENT_MIGHT_RESIGN_STR);
                return;
            }
            gameState.handleInputEvent(event, 1);
        }        
        
        gameState.update();
    }
    
    private void gotoServerContinue() {
        channelState = ChannelState.CONTINUE;
        selectionTimer = Menu.SELECTION_FRAMES;
        waitClientContinue = true;
        
        final GameState gameState = GameStateSource.getState();
        gameState.setSelection((byte) -1);
        gameState.loadStatuses(statuses);

        channel.write(Message.Type.GET_CONTINUE);
        
        InputSource.clear();
        
        if (statuses[0].getWins() >= 3 || statuses[1].getWins() >= 3) {
            statuses[0].reset();
            statuses[0].setLevel(-1);
            statuses[1].reset();
            statuses[1].setLevel(-1);
        }
    }
    
    private void updateServerContinue() {
        final GameState gameState = GameStateSource.getState();
        if (gameState.getSelection() >= 0) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            } else {
                handleServerContinue();
            }
            return;
        } 
        
        for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            } 
            if (keyStroke.getKeyType() == KeyType.Enter) {
                gameState.setSelection((byte) 0);
                break;
            }
        }        
    }
    
    private void handleServerContinue() {        
        if (statuses[0].getLevel() < 0) {
            gotoServerGettingLevel();
        } else {
            channelState = ChannelState.WAITING_FOR;
            disconnectMessageScreen.init("Server", WAITING_FOR_CLIENT_TO_CONTINUE_STR, 
                    MessageState.MessageType.WAITING);
            if (!waitClientContinue) {
                clientAckedGameState = false;
                initGameState();
                channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
            }
        }
    }
    
    private void gotoServerGiveUp() {
        channel.write(Message.Type.WAIT_GIVE_UP);
        channelState = ChannelState.GIVE_UP;
        serverMayResign = true;
        giveUpMenu.reset();
    }
    
    private void updateServerGiveUp() {
        giveUpMenu.update();
        final KeyStroke selection = giveUpMenu.getSelection();
        if (selection == null) {
            return;
        }
        if (selection.getKeyType() == KeyType.Escape) {
            requestedDisconnect = true;
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoServerWaitingFor(DISCONNECTING_STR);
            return;
        }
        final Character c = selection.getCharacter();
        if (c == null) {
            return;
        }
        switch (Character.toUpperCase(c)) {
            case 'Y':
                gotoServerChannel();
                break;
            case 'N':
                clientAckedGameState = false;
                clientMayResign = serverMayResign = false;
                channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
                gotoServerWaitingFor(RESUMING_GAME_STR);
                break;
        }
    }
    
    private void gotoServerWaiting() {
        state = State.SERVER_WAITING;
        disconnectMessageScreen.init("Server", WAITING_FOR_CLIENT_TO_CONNECT_STR, MessageState.MessageType.WAITING);
    }
    
    private void updateServerWaiting() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {            
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
            inputQueue.clear();
        }        
    }
    
    private void gotoServerInforming(final String error) {
        state = State.SERVER_INFORMING;
        messageScreen.init("Server", error, MessageState.MessageType.INFORM);
    }
    
    private void updateServerInforming() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoServerConfig();
        }
    }    
    
    private void gotoServerError(final String error) {
        state = State.SERVER_ERROR;
        disconnectMessageScreen.init("Server", error, MessageState.MessageType.ERROR);
    }
    
    private void updateServerError() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {            
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
    
    private void resetServerConfigReset() {
        
        NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        config = config.setHost(null).setPort(Server.DEFAULT_PORT);        
        database.saveAsync(Database.OtherKeys.SERVER, config);

        final NetworkInterfaceAddress nia = toNetworkInterfaceAddress(config.getHost());
        host = nia.getAddress();
        hostname = nia.getName();
        port = config.getPort();
        
        connectMenuState.init("Server", hostname, Integer.toString(port));
    }
    
    private void gotoClientConfig() {
        state = State.CLIENT_CONFIG;
        
        client.stop();
        
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        
        hostname = config.getHost();
        port = config.getPort();
        
        initClientHost();
        
        connectMenuState.init("Client", hostname, Integer.toString(config.getPort()));
    }
    
    private void initClientHost() {
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
                    case 'R':
                        resetClientConfigReset();
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
        client.setHost(host);
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
        
        channel = client.getMessageChannel();
        if (channel != null) {
            gotoClientChannel();
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
        statuses[0].reset();
        statuses[0].setLevel(-1);
        statuses[1].reset();
        statuses[1].setLevel(-1);
        requestedDisconnect = false;
        inputQueue.clear();
        gotoClientWaitingFor(WAITING_FOR_SERVER_STR);
    }
    
    private void updateClientChannel() {
        
        if (channel.isTerminated()) {
            channel = null;
            inputQueue.clear();
            if (requestedDisconnect) {
                requestedDisconnect = false;
                client.stop();
                gotoClientInforming("Server disconnected.");
            } else {
                gotoClientWaiting();
            }
            return;
        }
        
        if (channelState == ChannelState.PLAYING) {
            channel.waitForMessage();
        }
        
        for (int i = channel.getAvailableMessages() - 1; i >= 0; --i) {
            final Message message = channel.getReadMessage();
            if (message == null) {
                channel.stop();
                return;
            }
            switch (message.getType()) {
                case Message.Type.GET_LEVEL:
                    gotoClientGettingLevel();
                    break;
                case Message.Type.WAIT_LEVEL:
                    gotoClientWaitingFor(WAITING_FOR_SERVERS_LEVEL_STR);
                    break;
                case Message.Type.GAME_STATE:
                    try {
                        final GameState gameState = IOUtil.fromByteArray(message.getData());                        
                        final MonoGameState[] monoGameStates = gameState.getStates();
                        monoGameStates[0].setLocalPlayer(false);
                        monoGameStates[1].setLocalPlayer(true);
                        GameStateSource.setState(gameState);
                    } catch (final IOException | ClassNotFoundException ignored) {
                        channel.stop();
                        return;
                    }
                    channel.write(Message.Type.ACK_GAME_STATE);
                    break;
                case Message.Type.PLAY:
                    gotoClientPlaying();
                    break;
                case Message.Type.INPUT_EVENTS:
                    handleClientInputEvents(message.getInputEvents());
                    break;
                case Message.Type.GET_CONTINUE:
                    gotoClientContinue();
                    break;
                case Message.Type.REQUEST_DISCONNECT:
                    requestedDisconnect = true;
                    channel.write(Message.Type.DISCONNECT);
                    break;                    
                case Message.Type.DISCONNECT:
                    gotoClientConfig();
                    return;
                case Message.Type.WAIT_GIVE_UP:
                    gotoClientWaitingFor("Server might resign");
                    break;
                case Message.Type.GET_GIVE_UP:
                    gotoClientGiveUp();
                    break;
                case Message.Type.WAIT_CONTINUE:
                    gotoClientWaitingFor(WAITING_FOR_SERVER_TO_CONTINUE_STR);
                    break;
            }
            channel.incrementReadIndex();
        }
        
        switch (channelState) {
            case GETTING_LEVEL:
                updateClientGettingLevel();
                break;
            case WAITING_FOR:
                updateClientWaitingFor();
                break;
            case PLAYING:
                updateClientPlaying();
                break;
            case CONTINUE:
                updateClientContinue();
                break;
            case GIVE_UP:
                updateClientGiveUp();
                break;
        }        
    }
    
    private void handleClientInputEvents(final InputEventList[] eventLists) {
        final GameState gameState = GameStateSource.getState();
        for (int i = eventLists.length - 1; i >= 0; --i) {
            final InputEventList eventList = eventLists[i];
            for (int j = 0, end = eventList.size(); j < end; ++j) {
                final byte event = eventList.get(j);
                gameState.handleInputEvent(event, i);
            }
        }
        gameState.update();
    }
    
    private void gotoClientGettingLevel() {
        channelState = ChannelState.GETTING_LEVEL;
        
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        final byte level = config.getLevel();
        levelQuestion.init("Client", (level >= 0) ? Integer.toString(level) : "");
    }
    
    private void updateClientGettingLevel() {
        levelQuestion.update();
        
        if (levelQuestion.isEscPressed()) {
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoClientWaitingFor(DISCONNECTING_STR);
            return;
        }
        
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
        
        statuses[1].setLevel(Integer.parseInt(levelQuestion.getValue()));
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        database.saveAsync(Database.OtherKeys.CLIENT, config.setLevel((byte) statuses[1].getLevel()));
        
        channel.write(Message.Type.LEVEL, (byte) statuses[1].getLevel());
        
        gotoClientWaitingFor(WAITING_FOR_SERVERS_LEVEL_STR);
    }    
    
    private void gotoClientWaitingFor(final String reason) {
        channelState = ChannelState.WAITING_FOR;
        disconnectMessageScreen.init("Client", reason, MessageState.MessageType.WAITING);
    }
    
    private void updateClientWaitingFor() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoClientWaitingFor(DISCONNECTING_STR);
        }
    }
    
    private void gotoClientPlaying() {
        channelState = ChannelState.PLAYING;
        InputSource.clear();
    }
    
    private void updateClientPlaying() {
        
        final Message message = channel.getWriteMessage();
        if (message == null) {
            channel.stop();
            return;
        }
        
        message.setType(Message.Type.INPUT_EVENTS);
        
        final InputEventList events = message.getInputEvents(0);
        InputEventSource.poll(events);

        channel.incrementWriteIndex();
    }
    
    private void gotoClientContinue() {        
        channelState = ChannelState.CONTINUE;
        InputSource.clear();
        GameStateSource.getState().setSelection((byte) -1);
        selectionTimer = Menu.SELECTION_FRAMES;
    }
    
    private void updateClientContinue() {
        final GameState gameState = GameStateSource.getState();
        if (gameState.getSelection() >= 0) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            } else {
                handleClientContinue();
            }
            return;
        } 
        
        for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            } 
            if (keyStroke.getKeyType() == KeyType.Enter) {
                gameState.setSelection((byte) 0);
                break;
            }
        }        
    }
    
    private void handleClientContinue() {
        channel.write(Message.Type.CONTINUE);
        gotoClientWaitingFor(WAITING_FOR_SERVER_TO_CONTINUE_STR);
    }
    
    private void gotoClientGiveUp() {
        channelState = ChannelState.GIVE_UP;
        giveUpMenu.reset();        
    }
    
    private void updateClientGiveUp() {
        giveUpMenu.update();
        final KeyStroke selection = giveUpMenu.getSelection();
        if (selection == null) {
            return;
        }
        if (selection.getKeyType() == KeyType.Escape) {
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoClientWaitingFor(DISCONNECTING_STR);
            return;
        }
        final Character c = selection.getCharacter();
        if (c == null) {
            return;
        }
        switch (Character.toUpperCase(c)) {
            case 'Y':
                channel.write(Message.Type.GIVE_UP);
                gotoClientWaitingFor(DISCONNECTING_STR);
                break;
            case 'N':
                channel.write(Message.Type.RESUME_GAME);
                gotoClientWaitingFor(RESUMING_GAME_STR);
                break;
        }        
    }

    private void gotoClientWaiting() {
        state = State.CLIENT_WAITING;
        disconnectMessageScreen.init("Client", CONNECTING_TO_SERVER_STR, MessageState.MessageType.WAITING);
    }
    
    private void updateClientWaiting() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {            
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

        channel = client.getMessageChannel();
        if (channel != null) {
            gotoClientChannel();
        }        
    }
    
    private void gotoClientInforming(final String error) {
        state = State.CLIENT_INFORMING;
        messageScreen.init("Client", error, MessageState.MessageType.INFORM);
    }
    
    private void updateClientInforming() {
        messageScreen.update();
        if (messageScreen.isSelected()) {            
            gotoClientConfig();
        }
    }    
    
    private void gotoClientError(final String error) {
        state = State.CLIENT_ERROR;
        disconnectMessageScreen.init("Client", error, MessageState.MessageType.ERROR);
    }
    
    private void updateClientError() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {            
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
    
    private void resetClientConfigReset() {
        
        NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        config = config.setHost(null).setPort(Server.DEFAULT_PORT);        
        database.saveAsync(Database.OtherKeys.CLIENT, config);

        hostname = config.getHost();
        port = config.getPort();
        
        initClientHost();
        
        connectMenuState.init("Client", hostname, Integer.toString(config.getPort()));
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

    State getState() {
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

    public MessageScreen getDisconnectMessageScreen() {
        return disconnectMessageScreen;
    }

    ChannelState getChannelState() {
        return channelState;
    }

    public Question getLevelQuestion() {
        return levelQuestion;
    }

    public Menu getGiveUpMenu() {
        return giveUpMenu;
    }
}