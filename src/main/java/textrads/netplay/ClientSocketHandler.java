package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocketHandler {
    
    private final Client client;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final Queue outQueue = new Queue(1);
    private final Thread outQueueThread;
    
    private final Queue inQueue = new Queue();
    private final Thread inQueueThread;
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    public ClientSocketHandler(final Client client, final Socket socket) throws IOException {
        this.client = client;
        this.socket = socket;
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        outQueueThread = new Thread(this::runOutQueue);
        inQueueThread = new Thread(this::runInQueue);
    }
    
    public void start() {
        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
        }
        
        outQueueThread.start();
        inQueueThread.start();
    }
    
    public void stop() {
        
        synchronized (monitor) {
            if (!running || cancelled) {
                return;
            }
            cancelled = true;
        }
        
        closeSocket();
        outQueueThread.interrupt();
        inQueueThread.interrupt();
    }
    
    public void update() {

    }
    
        
    private void runOutQueue() {
        try {
            while (true) {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        break;
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
        }                
    }
    
    private void runInQueue() {        
        try {
            outer: while (true) {                
                synchronized (monitor) {
                    if (!running || cancelled) {
                        break;
                    }
                }                      
                try {
                    switch ((byte) in.read()) {
                        case Command.HEARTBEAT:
                            break;
                        case Command.GAME_STATE:
                            inQueue.getWriteElement().readGameState(in);
                            inQueue.incrementWriteIndex();
                            break;
                        case Command.INPUT_EVENTS:
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
    
    private void closeSocket() {
        try {   
            final Socket s = socket;
            if (s != null) {
                s.close();
            }
        } catch (final IOException ignored) {            
        }
    }    
}
