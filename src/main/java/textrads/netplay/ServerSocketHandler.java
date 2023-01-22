package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

            }         
        } finally {
            close();
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
