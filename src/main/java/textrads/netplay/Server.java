package textrads.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import textrads.util.ThreadUtil;

public class Server {
     
    public static final int DEFAULT_PORT = 8080;

    private static final int BACKLOG = 50;  
    
    private volatile InetAddress bindAddress;
    private volatile int port = DEFAULT_PORT;
    
    private final Object monitor = new Object();
    
    private boolean running;    
    private Thread listenerThread;
    private MessageChannel channel;
    private ServerSocket serverSocket;
    private String fatalError;
    
    public void start() {        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
            fatalError = null;
            listenerThread = new Thread(this::listen);
            listenerThread.start();
        }
    }
    
    public void stop() {
        
        synchronized (monitor) {
            if (!running) {
                return;
            }
            running = false;
            closeServerSocket();
            if (listenerThread != null) {
                ThreadUtil.interrupt(listenerThread);
                listenerThread = null;
            }
            if (channel != null) {
                channel.stop();
                channel = null;
            }                        
        }                       
    }
       
    private void listen() {
        try {
            synchronized (monitor) {
                serverSocket = new ServerSocket(port, BACKLOG, bindAddress);
            }
        } catch (final IOException e) {
            fatalError = e.getMessage();
            stop();
            return;
        }
                
        try {
            while (true) {
                final ServerSocket ss;
                synchronized (monitor) {
                    while (running && channel != null && !channel.isTerminated()) {
                        try {
                            monitor.wait();
                        } catch (final InterruptedException ignored) {
                        }
                    }
                    if (!running) {
                        break;
                    }
                    if (channel != null && channel.isHandshakeError()) {
                        fatalError = "Bad handshake.";
                        return;
                    }
                    channel = null;
                    ss = serverSocket;
                }
                
                if (ss == null) {
                    return;
                }
                
                final MessageChannel c;
                try {
                    c = new MessageChannel(ss.accept(), chan -> {
                        synchronized (monitor) {
                            monitor.notifyAll();
                        }
                    });
                    c.start();
                } catch (final IOException e) {
                    ThreadUtil.sleepOneSecond();
                    continue;
                }                

                synchronized (monitor) {
                    channel = c;
                }
            }
        } finally {
            stop();
        }
    }
    
    private void closeServerSocket() {
        synchronized (monitor) {
            try {   
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }                
            } catch (final IOException ignored) {            
            }
        }
    }
    
    public MessageChannel getMessageChannel() {
        synchronized (monitor) {
            return channel;
        }
    }
    
    public boolean isRunning() {
        synchronized (monitor) {
            return running;
        }
    }

    public String getFatalError() {
        synchronized (monitor) {
            return fatalError;
        }
    }    

    public InetAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}