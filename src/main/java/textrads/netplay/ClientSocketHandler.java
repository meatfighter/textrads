package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import textrads.ByteList;

public class ClientSocketHandler {
           
    private final Client client;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final EventQueue outQueue = new EventQueue(1);
    private final Thread outQueueThread;
    
    private final EventQueue inQueue = new EventQueue();
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
                        writeEvents();
                    }
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
        out.flush();
    }
    
    private void writeEvents() throws IOException {
        final ByteList list = outQueue.getReadElement().getEvents(0);
        out.write(Command.EVENTS);
        out.write(list.size());
        out.write(list.getData(), 0, list.size());        
        outQueue.incrementReadIndex();
        out.flush();
    }
    
    private void runInQueue() {        
        try {
            while (true) {                
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
                    }   
                } catch (final IOException ignored) {
                    break;
                }                                
            }
        } finally {
            stop();
        }
    }
    
    private void readState() throws IOException {
        final byte[] data = new byte[in.readInt()];
        in.readFully(data);
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            
        }
    }
    
    private void readEvents() throws IOException {
        
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
