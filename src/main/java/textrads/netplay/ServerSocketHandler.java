package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import textrads.GameState;

public class ServerSocketHandler {
    
    private final Server server;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    
    private volatile boolean running = true;
    
    public ServerSocketHandler(final Server server, final Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        new Thread(this::handleInput).start(); // TODO DON'T START THREADS IN CONSTRUCTOR
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
