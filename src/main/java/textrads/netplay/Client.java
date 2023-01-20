package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import textrads.util.ThreadUtil;

public class Client {
    
    private String host;
    private int port = Server.DEFAULT_PORT;
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    private volatile Thread listenerThread;
    private volatile Thread heartbeatThread;
    private volatile Socket socket;
    private volatile InputStream in;
    private volatile OutputStream out;
    
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
    
    private void closeSocket() {
        try {   
            final Socket s = socket;
            if (s != null) {
                s.close();
            }
        } catch (final IOException ignored) {            
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
