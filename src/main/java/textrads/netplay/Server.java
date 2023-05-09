package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import textrads.util.IOUtil;
import textrads.util.ThreadUtil;

public class Server {
    
    static final long MAX_HANDSHAKE_WAIT_MILLIS = TimeUnit.SECONDS.toMillis(10);
     
    public static final int DEFAULT_PORT = 8080;

    private static final int BACKLOG = 50;  
    
    private final Object monitor = new Object();

    private InetAddress bindAddress;
    private int port = DEFAULT_PORT;    
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
            IOUtil.close(serverSocket);
            serverSocket = null;
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
            if (e instanceof BindException) {
                error = "Port already in use.";                
            } else {
                error = e.getMessage();
            }
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
                    
                    channel = null;
                    ss = serverSocket;
                }
                
                if (ss == null) {
                    return;
                }
                
                Socket socket = null;
                final InputStream in;
                final OutputStream out;
                try {
                    socket = ss.accept();
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                } catch (final IOException e) {
                    IOUtil.close(socket);
                    ThreadUtil.sleepOneSecond();
                    continue;
                }  
                
                final MessageChannel c = new MessageChannel(socket, in, out, chan -> {
                    synchronized (monitor) {
                        monitor.notifyAll();
                    }
                });
                c.start();

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
                        c.stop();
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
        synchronized (monitor) {
            return bindAddress;
        }
    }

    public void setBindAddress(final InetAddress bindAddress) {
        synchronized (monitor) {
            this.bindAddress = bindAddress;
        }
    }

    public int getPort() {
        synchronized (monitor) {
            return port;
        }
    }

    public void setPort(final int port) {
        synchronized (monitor) {
            this.port = port;
        }
    }
}