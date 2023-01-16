package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import textrads.DualGameState;
import textrads.GameEvent;

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
        new Thread(this::handleInput).start();
    }
    
    private void handleInput() {
        try {
            while (true) {
                //in.read()
            }
        } finally {
            close();
        }
    }
    
    public void sendHeartbeat() {
        synchronized (out) {
            try {
                out.write(Command.HEARTBEAT);
                out.flush();
            } catch (final IOException e) {
                close();
            }
        }
    }
    
    public void sendState(final DualGameState state) {
        
    }
    
    public void sendEvents(final List<GameEvent>[] events) {
        
    }
    
    public void close() {
        running = false;
        
        try {            
            socket.close();
        } catch (final IOException ignored) {
        }
        
        server.removeHandler(this);
    }
}
