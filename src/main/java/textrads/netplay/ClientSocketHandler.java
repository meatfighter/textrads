package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import textrads.play.GameState;
import textrads.play.GameStateSource;
import textrads.input.InputEventList;
import textrads.input.InputEventSource;
import textrads.app.Textrads;
import textrads.util.ThreadUtil;

public class ClientSocketHandler {
    
    private static final long MAX_IN_QUEUE_WAIT_MILLIS = Math.round(1000.0 / Textrads.FRAMES_PER_SECOND);
    
    private static enum State {
        NOT_STARTED,
        RUNNING,
        CANCELLED,
        TERMINATED,
    }
    
    private final Client client;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final Queue outQueue = new Queue(1);
    private final Thread outQueueThread;
    
    private final Queue inQueue = new Queue();
    private final Thread inQueueThread;
    
    private final boolean player; // false = observer
    
    private final Object stateMonitor = new Object();
    private State state = State.NOT_STARTED;
    
    public ClientSocketHandler(final Client client, final Socket socket, final boolean player) throws IOException {
        this.client = client;
        this.socket = socket;
        this.player = player;
        
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        outQueueThread = new Thread(this::runOutQueue);
        inQueueThread = new Thread(this::runInQueue);
    }
    
    public void start() {
        
        synchronized (stateMonitor) {
            if (state != State.NOT_STARTED) {
                return;
            }
            outQueueThread.start();
            inQueueThread.start();
            state = State.RUNNING;
        }
    }
    
    public void stop() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
            state = State.CANCELLED;
        }
        
        closeSocket();
        ThreadUtil.interrupt(outQueueThread);
        ThreadUtil.interrupt(inQueueThread);
    }
    
    private void closeSocket() {
        try {   
            final Socket s = socket;
            if (s != null) {
                s.close();
            }
        } catch (final IOException ignored) {            
        }
    }     
    
    public void update() {

        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
        }
        
        if (outQueue.isFull()) {
            stop();
            return;
        }        
        if (player) {
            InputEventSource.poll(outQueue.getWriteElement().getInputEvents(0));
            outQueue.incrementWriteIndex();
        } else {
            InputEventSource.clear();
        }
        
        while (inQueue.isEmpty()) {
            synchronized (stateMonitor) {
                if (state != State.RUNNING) {
                    return;
                }
            }            
            try {            
                inQueue.waitForData(MAX_IN_QUEUE_WAIT_MILLIS);
            } catch (final InterruptedException ignored) {                
            }
        }
        for (int i = inQueue.size() - 1; i >= 0; --i) {
            final Element element = inQueue.getReadElement();
            final byte[] data = element.getData();
            if (data != null) {
                try {
                    GameStateSource.setState(data);
                } catch (final IOException | ClassNotFoundException ignored) {
                    stop();
                    return;
                }
            } else {
                final GameState gameState = GameStateSource.getState();
                final InputEventList[] events = element.getInputEvents();
                for (int p = 0; p < events.length; ++p) {
                    final InputEventList es = events[p];
                    for (int j = 0; j < es.size(); ++j) {
                        gameState.handleInputEvent(es.get(j), p);
                    }
                }
            }
            inQueue.incrementReadIndex();
        }
    }
    
    public boolean isRunning() {
        synchronized (stateMonitor) {
            return state == State.RUNNING;
        }
    }
    
    public boolean isTerminated() {
        synchronized (stateMonitor) {
            return state == State.TERMINATED;
        }
    }
        
    private void runOutQueue() {
        try {
            while (true) {
                synchronized (stateMonitor) {
                    if (state != State.RUNNING) {
                        return;
                    }
                } 
                try {
                    if (outQueue.isEmpty()) {
                        outQueue.waitForData(Server.HEARTBEAT_PERIOD);
                    }
                    if (outQueue.isEmpty()) {
                        out.write(Command.HEARTBEAT);
                    } else {
                        outQueue.getReadElement().write(out);        
                        outQueue.incrementReadIndex();
                    }
                    out.flush();
                } catch (final InterruptedException ignored) {
                } catch (final IOException ignored) {
                    break;
                }
            }
        } finally {
            stop();            
            ThreadUtil.join(inQueueThread);
            synchronized (stateMonitor) {
                state = State.TERMINATED;
            }
            client.handleTerminatedConnection();
        }                
    }
    
    private void runInQueue() {        
        try {
            outer: while (true) {                
                synchronized (stateMonitor) {
                    if (state != State.RUNNING) {
                        return;
                    }
                }                      
                try {
                    switch ((byte) in.read()) {
                        case Command.HEARTBEAT:
                            break;
                        case Command.GAME_STATE:
                            if (inQueue.isFull()) {
                                break outer;
                            }
                            inQueue.getWriteElement().readGameState(in);
                            inQueue.incrementWriteIndex();
                            break;
                        case Command.INPUT_EVENTS:
                            if (inQueue.isFull()) {
                                break outer;
                            }
                            inQueue.getWriteElement().readInputEvents(in);
                            inQueue.incrementWriteIndex();
                            break;
                        default:
                            break outer;
                    }   
                } catch (final IOException ignored) {
                    break;
                }                                
            }
        } finally {
            stop();
        }
    }
}
