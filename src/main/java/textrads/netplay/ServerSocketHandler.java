package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerSocketHandler {
    
    private final Server server;
    private final Socket socket;
    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    
    private volatile boolean running = true;
    
    public ServerSocketHandler(final Server server, final Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }
    
    public void sendHeartbeat() {        
    }
    
    public void close() {
        running = false;
        
        try {            
            socket.close();
        } catch (final IOException ignored) {
        }
    }
}
