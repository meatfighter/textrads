package textrads.netplay;

import java.io.IOException;
import java.net.Socket;
import textrads.util.ThreadUtil;

public class Client {
        
    private volatile String host;
    private volatile int port = Server.DEFAULT_PORT;
    
    private boolean running;
    private final Object stateMonitor = new Object();   
    private Thread connectThread;    
    private MessageChannel channel;
    
    public void start() {        
        synchronized (stateMonitor) {
            if (running) {
                return;
            }
            running = true;
            connectThread = new Thread(this::connect);
            connectThread.start();
        }
    }
    
    public void stop() {        
        synchronized (stateMonitor) {
            if (!running) {
                return;
            }
            running = false;
            ThreadUtil.interrupt(connectThread);
            if (channel != null) {
                channel.stop();
                channel = null;
            }
        }        
    }    
    
    private void connect() {
        try {
            while (true) {
                
                synchronized (stateMonitor) {
                    while (running && channel != null && !channel.isTerminated()) {
                        try {
                            stateMonitor.wait();
                        } catch (final InterruptedException ignored) {
                        }
                    }                    
                    if (!running) {
                        break;
                    }
                    channel = null;
                }

                final MessageChannel c;
                try {
                    c = new MessageChannel(new Socket(host, port), chan -> {
                        synchronized (stateMonitor) {
                            stateMonitor.notifyAll();
                        }
                    });
                    c.start();
                } catch (final IOException e) {
                    
                    // TODO FIRST TIME FAILURE
                    
                    ThreadUtil.sleepOneSecond();
                    continue;
                }
                
                synchronized (stateMonitor) {                    
                    channel = c;                    
                }
            }
        } finally {
            synchronized (stateMonitor) {
                if (channel != null) {
                    channel.stop();
                }
            }
        }
    }
    
    public MessageChannel getMessageChannel() {
        synchronized (stateMonitor) {
            return channel;
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
