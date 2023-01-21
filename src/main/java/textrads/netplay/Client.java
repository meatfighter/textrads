package textrads.netplay;

import java.io.IOException;
import java.net.Socket;
import textrads.util.ThreadUtil;

public class Client {
    
    private String host;
    private int port = Server.DEFAULT_PORT;
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    private volatile Thread listenerThread;
    
    public void start() {
        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
        }
        
        final Thread listThread = new Thread(this::listen);
        listenerThread = listThread;
        listThread.start();
    }
    
    private void listen() {
        try {
            while (true) {
                synchronized (monitor) {
                    if (cancelled) {
                        break;
                    }
                }
                closeSocket();
                
                try {
                    socket = new Socket(host, port);
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                } catch (final IOException ignored) {
                    synchronized (monitor) {
                        if (cancelled) {
                            break;
                        }
                    }
                    ThreadUtil.sleepOneSecond();
                    continue;
                }                               
            }
        } finally {
            closeSocket();
            ThreadUtil.joinThread(heartbeatThread);
            socket = null;
            out = null;
            in = null;            
            listenerThread = null;
            heartbeatThread = null;
            synchronized (monitor) {
                running = cancelled = false;
            }
        }
    }
    
    private void sendHeartbeats() {
        while (true) {
            
        }
    }
    
    public void update() {
        // TODO
    }    
    
    public void stop() {
        
        synchronized (monitor) {
            if (!running || cancelled) {
                return;
            }
            cancelled = true;
        }
    }
    
    public void removeHandler(final ClientSocketHandler handler) {
        
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
