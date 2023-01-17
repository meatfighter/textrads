package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import textrads.GameState;

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
                switch (in.read()) {
                    case Command.HEARTBEAT:
                        break;
                    case Command.STATE:
                        break;
                    case Command.EVENTS:
                        break;
                }
            }
        } catch (final Exception ignored) {            
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
    
    public void sendState(final GameState state) {
        synchronized (out) {
            
        }
    }
    
    public void sendEvents(final List<Integer>[] events) {
        synchronized (out) {
            
        }
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
