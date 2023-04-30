package textrads.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import textrads.util.ThreadUtil;

public class Server {
    
    static final long MAX_HANDSHAKE_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(30);
     
    public static final int DEFAULT_PORT = 8080;

    private static final int BACKLOG = 50;  
    
    private volatile InetAddress bindAddress;
    private volatile int port = DEFAULT_PORT;
    
    private final Object monitor = new Object();
    
    private boolean running;    
    private Thread listenerThread;
    private MessageChannel channel;
    private ServerSocket serverSocket;
    private String error;
    
    public void start() {        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
            error = null;
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
        System.out.println("-- 1isten");
        
        try {
            synchronized (monitor) {
                serverSocket = new ServerSocket(port, BACKLOG, bindAddress);
                System.out.println("listening on: " + port + " " + bindAddress);
            }
        } catch (final IOException e) {
            error = e.getMessage();
            stop();
            return;
        }
                
        try {
            while (true) {
                final ServerSocket ss;
                synchronized (monitor) {
                    while (running && channel != null && !channel.isTerminated()) {
                        try {
                            System.out.println("Waiting for disconnection...");
                            monitor.wait();
                        } catch (final InterruptedException ignored) {
                        }
                    }
                    if (!running) {
                        break;
                    }
                    
                    // TODO TESTING
                    if (channel != null && channel.isTerminated()) {
                        System.out.println("Channel terminated.");
                    }
                    
                    channel = null;
                    ss = serverSocket;
                }
                
                if (ss == null) {
                    return;
                }
                
                final MessageChannel c;
                try {
                    System.out.println("Waiting for connection...");
                    c = new MessageChannel(ss.accept(), chan -> {
                        synchronized (monitor) {
                            monitor.notifyAll();
                        }
                    });
                    c.start();
                } catch (final IOException e) {
                    System.out.println("Connection error: " + e.getMessage());
                    ThreadUtil.sleepOneSecond();
                    continue;
                }                

                synchronized (monitor) {                    
                    final long startTime = System.currentTimeMillis();
                    while (running && !c.isTerminated() 
                            && c.getHandshakeStatus() == MessageChannel.HandshakeStatus.PENDING) {
                        final long remainingTime = MAX_HANDSHAKE_WAIT_MILLIS - (System.currentTimeMillis() - startTime);
                        if (remainingTime <= 0) {                            
                            break;
                        }
                        try {
                            monitor.wait(remainingTime);
                        } catch (final InterruptedException ignored) {
                        }
                    }                    
                    if (!running) {
                        break;
                    }
                    
                    switch (c.getHandshakeStatus()) {
                        case SUCCESS:
                            channel = c;
                            break;
                        case FAIL:
                            c.stop();
                            error = "Bad handshake.";
                            return;
                        case PENDING:
                            c.stop();
                            break;                            
                    }
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

    public String getError() {
        synchronized (monitor) {
            return error;
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