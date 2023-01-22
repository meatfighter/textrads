package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import textrads.InputEventList;
import textrads.GameState;
import textrads.GameStateSource;
import textrads.InputEventSource;
import textrads.util.IOUtil;

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
                        writeHeartbeat();
                    } else {
                        writeElement();
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
    
    private void writeHeartbeat() throws IOException {
        out.write(Command.HEARTBEAT);        
    }
    
    private void writeElement() throws IOException {
        outQueue.getReadElement().write(out);        
        outQueue.incrementReadIndex();
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
                        case Command.STATE:
                            readState();
                            break;
                        case Command.EVENTS:
                            readEvents();
                            break;
                        default:
                            break outer;
                    }   
                } catch (final IOException | ClassNotFoundException ignored) {
                    break;
                }                                
            }
        } finally {
            stop();
        }
    }
    
    private void readState() throws IOException, ClassNotFoundException {
        final int length = in.readInt();
        if (length < 0 || length > MAX_OBJECT_LENGTH) {
            throw new IOException("invalid object length");
        }
        final byte[] data = new byte[in.readInt()];
        in.readFully(data);
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            final Object obj = ois.readObject();
            if (!(obj instanceof GameState)) {
                throw new IOException("invalid object");
            }
            GameStateSource.setState((GameState) obj);
        }
    }
    
    private void readEvents() throws IOException {
        final InputEventList[] element = inQueue.getWriteElement();
        readEvents(element[0]);
        readEvents(element[1]);
    }
    
    private void readEvents(final InputEventList list) throws IOException {
        final int length = in.read();
        if (length < 0 || length > InputEventSource.MAX_POLLS) {
            throw new IOException("invalid events length");
        }        
        in.readFully(list.getData(), 0, length);
        list.setSize(length);
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
