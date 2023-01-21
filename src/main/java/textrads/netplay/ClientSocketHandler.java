package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSocketHandler {
        
    private final Client client;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    private final ByteQueue outQueue = new ByteQueue();
    private final Thread outQueueThread;
    
    private final ByteQueue inQueue = new ByteQueue();
    private final Thread inQueueThread;
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    public ClientSocketHandler(final Client client, final Socket socket) throws IOException {
        this.client = client;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
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
                    final BytePipe pipe = inQueue.borrowWriter();
                    pipe.incrementWriteIndex(in.read(pipe.getData(), pipe.getWriteIndex(), pipe.getMaxWriteLength()));
                } catch (final IOException ignored) {
                    break;
                } finally {
                    inQueue.returnWriter();
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
