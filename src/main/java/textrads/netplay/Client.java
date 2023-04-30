package textrads.netplay;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import textrads.util.ThreadUtil;

import static textrads.netplay.Server.MAX_HANDSHAKE_WAIT_MILLIS;

public class Client {
    
    private volatile InetAddress host;
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
                    
                    // TODO TESTING
                    if (channel != null && channel.isTerminated()) {
                        System.out.println("Channel terminated.");
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
                    e.printStackTrace(); // TODO REMOVE
                    synchronized (monitor) {
                        if (firstConnectionAttempt) {
                            if (e instanceof ConnectException) {
                                error = "Connection refused.";
                            } else {
                                error = e.getMessage();
                            }
                            break;
                        }
                    }
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
                            if (firstConnectionAttempt) {
                                error = "Bad handshake.";
                                return;
                            }
                            break;
                        case PENDING:
                            c.stop();
                            if (firstConnectionAttempt) {
                                error = "Failed to receive handshake.";
                                return;
                            }
                            break;                            
                    }   
                    
                    firstConnectionAttempt = false;
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
       
    public InetAddress getHost() {
        return host;
    }

    public void setHost(final InetAddress host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }
}
