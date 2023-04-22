package textrads.netplay;

import java.io.IOException;
import java.net.Socket;
import textrads.util.ThreadUtil;

public class Client {
        
    private volatile String host;
    private volatile int port = Server.DEFAULT_PORT;
    
    private final Object monitor = new Object();
    
    private boolean running;    
    private Thread connectThread;    
    private MessageChannel channel;
    private boolean firstConnectionAttempt;
    private String error;
    
    public void start() {        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
            firstConnectionAttempt = true;
            error = null;
            connectThread = new Thread(this::connect);
            connectThread.start();
        }
    }
    
    public void stop() {        
        synchronized (monitor) {
            if (!running) {
                return;
            }
            running = false;
            if (connectThread != null) {                
                ThreadUtil.interrupt(connectThread);
                connectThread = null;
            }
            if (channel != null) {
                channel.stop();
                channel = null;
            }
        }        
    }    
    
    private void connect() {
        System.out.println("-- connect");
        try {
            while (true) {
                
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
                    if (channel != null && channel.isHandshakeError()) {
                        System.out.println("Bad handshake :(");
                        error = "Bad handshake.";
                        return;
                    }
                    channel = null;
                }

                final MessageChannel c;
                try {
                    System.out.println("Connecting...");
                    c = new MessageChannel(new Socket(host, port), chan -> {
                        synchronized (monitor) {
                            monitor.notifyAll();
                        }
                    });
                    c.start();
                } catch (final IOException e) {
                    System.out.println("Connection error: " + e.getMessage());
                    synchronized (monitor) {
                        if (firstConnectionAttempt) {                            
                            error = e.getMessage();
                            return;
                        }
                    }
                    ThreadUtil.sleepOneSecond();
                    continue;
                }
                
                synchronized (monitor) {
                    firstConnectionAttempt = false;                    
                    channel = c;                    
                }
            }
        } finally {
            stop();
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
