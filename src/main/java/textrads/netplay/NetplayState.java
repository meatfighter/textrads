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

// TODO AFTER CLIENT RECONNECTS ON CONTINUE SCREEN, MESSAGE DOES NOT SWITCH BACK TO WATIING FOR CLIENT TO CONTINUE

public class NetplayState {

    private static final String WAITING_FOR_CLIENT_STR = "Waiting for client to connect";
    private static final String CONNECTING_TO_SERVER_STR = "Connecting to server";
    private static final String WAITING_FOR_SERVER_STR = "Waiting for server";
    private static final String WAITING_FOR_CLIENT_TO_CONTINUE = "Waiting for client to continue";
    
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
    
    private final Database database = DatabaseSource.getDatabase();
    private final InputQueue inputQueue = new InputQueue();
    
    private final Server server = new Server();
    private final Client client = new Client();
    
    private final Menu playAsMenu = createPlayAsMenu();
    
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
    private byte serverLevel;
    private byte clientLevel;    
    private byte serverWins;
    private byte clientWins;
    private int serverUpdates;
    private int clientUpdates;
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
        channelJustEstablished = true;        
        serverLevel = -1;
        clientLevel = -1;
        clientWins = 0;
        serverWins = 0;
        clientUpdates = 0;
        serverUpdates = 0;
        clientAckedGameState = false;
        waitClientContinue = false;
        requestedDisconnect = false;
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
            
            if (clientLevel < 0) {
                channel.write(Message.Type.GET_LEVEL);
            } else if (serverLevel < 0) {
                channel.write(Message.Type.WAIT_LEVEL);
            } else if (!clientAckedGameState) {
                final GameState gameState = GameStateSource.getState();
                final byte selection = gameState.getSelection();
                gameState.setSelection((byte) -1);
                channel.write(Message.Type.GAME_STATE, gameState);
                gameState.setSelection(selection);
                if (waitClientContinue) {
                    disconnectMessageScreen.init("Server", WAITING_FOR_CLIENT_TO_CONTINUE, 
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
                case Message.Type.LEVEL:
                    try {
                        clientLevel = IOUtil.fromByteArray(message.getData());
                    } catch (final IOException | ClassNotFoundException ignored) {
                        channel.stop();
                        return;
                    }
                    if (clientLevel < 0) {
                        channel.stop();
                        return;                    
                    }
                    if (serverLevel >= 0) {
                        initGameState();
                        channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
                    }
                    break;
                case Message.Type.ACK_GAME_STATE:
                    clientAckedGameState = true;
                    if (waitClientContinue) {
                        channel.write(Message.Type.GET_CONTINUE);
                    } else if (channelState != ChannelState.CONTINUE) {
                        gotoServerPlaying();
                    }
                    break;
                case Message.Type.INPUT_EVENTS:
                    inputQueue.enqueue(message.getInputEvents(0));
                    break;
                case Message.Type.CONTINUE:                    
                    waitClientContinue = false;
                    if (clientLevel == -1 || clientWins == 3 || serverWins == 3) {
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
        gameState.init(GameState.Mode.VS_HUMAN, ThreadLocalRandom.current().nextLong(), serverLevel, clientLevel, 0, 0, 
                false, serverWins, clientWins);
        final MonoGameState[] monoGameStates = gameState.getStates();
        monoGameStates[0].setLocalPlayer(true);
        monoGameStates[1].setLocalPlayer(false);
        monoGameStates[0].setUpdates(serverUpdates);
        monoGameStates[1].setUpdates(clientUpdates);
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
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoServerWaitingFor("Waiting to disconnect");
            return;
        }
        
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
        
        serverLevel = (byte) Integer.parseInt(levelQuestion.getValue());
        final NetplayConfig config = database.get(Database.OtherKeys.SERVER);
        database.saveAsync(Database.OtherKeys.SERVER, config.setLevel(serverLevel));
        
        if (clientLevel >= 0) {
            initGameState();
            channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
        }
                        
        gotoServerWaitingFor("Waiting for client's level");
    }
    
    private void gotoServerWaitingFor(final String reason) {
        channelState = ChannelState.WAITING_FOR;
        disconnectMessageScreen.init("Server", reason, MessageState.MessageType.WAITING);
    }
    
    private void updateServerWaitingFor() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoServerWaitingFor("Waiting to disconnect");
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
//            if (event == InputEvent.GIVE_UP_PRESSED) {
//                gotoGiveUp();
//                return;
//            }
            gameState.handleInputEvent(event, 0);
        }
        
        for (int i = 0, end = clientEvents.size(); i < end; ++i) {
            final byte event = clientEvents.get(i);
//            if (event == InputEvent.GIVE_UP_PRESSED) {
//                gotoGiveUp();
//                return;
//            }
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
        
        final MonoGameState[] states = gameState.getStates();
        serverWins = states[0].getWins();
        clientWins = states[1].getWins();
        serverUpdates = states[0].getUpdates();
        clientUpdates = states[1].getUpdates();

        channel.write(Message.Type.GET_CONTINUE);
        
        InputSource.clear();
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
        
        if (serverWins == 3 || clientWins == 3) {
            serverLevel = -1;
            clientLevel = -1;
            clientWins = 0;
            serverWins = 0;
            clientUpdates = 0;
            serverUpdates = 0;
            inputQueue.clear();
            gotoServerGettingLevel();
        } else {
            channelState = ChannelState.WAITING_FOR;
            disconnectMessageScreen.init("Server", WAITING_FOR_CLIENT_TO_CONTINUE, 
                    MessageState.MessageType.WAITING);
            if (!waitClientContinue) {
                clientAckedGameState = false;
                initGameState();
                channel.write(Message.Type.GAME_STATE, GameStateSource.getState());
            }
        }
    }
    
    private void gotoServerGiveUp() {
        channelState = ChannelState.GIVE_UP;
    }
    
    private void updateServerGiveUp() {
        
    }
    
    private void gotoServerWaiting() {
        state = State.SERVER_WAITING;
        disconnectMessageScreen.init("Server", WAITING_FOR_CLIENT_STR, MessageState.MessageType.WAITING);
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
        serverLevel = -1;
        clientLevel = -1;
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
            gotoClientWaitingFor("Waiting to disconnect");
            return;
        }
        
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
        
        clientLevel = (byte) Integer.parseInt(levelQuestion.getValue());
        final NetplayConfig config = database.get(Database.OtherKeys.CLIENT);
        database.saveAsync(Database.OtherKeys.CLIENT, config.setLevel(clientLevel));
        
        channel.write(Message.Type.LEVEL, clientLevel);
        
        gotoClientWaitingFor("Waiting for server's level");
    }    
    
    private void gotoClientWaitingFor(final String reason) {
        channelState = ChannelState.WAITING_FOR;
        disconnectMessageScreen.init("Client", reason, MessageState.MessageType.WAITING);
    }
    
    private void updateClientWaitingFor() {
        disconnectMessageScreen.update();
        if (disconnectMessageScreen.isSelected()) {
            channel.write(Message.Type.REQUEST_DISCONNECT);
            gotoClientWaitingFor("Waiting to disconnect");
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
        gotoClientWaitingFor("Waiting for server to continue");
    }
    
    private void gotoClientGiveUp() {
        channelState = ChannelState.GIVE_UP;
    }
    
    private void updateClientGiveUp() {
        
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

    public MessageScreen getDisconnectMessageScreen() {
        return disconnectMessageScreen;
    }

    public ChannelState getChannelState() {
        return channelState;
    }

    public Question getLevelQuestion() {
        return levelQuestion;
    }
}
