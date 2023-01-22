package textrads.netplay;

import java.io.IOException;
import java.net.Socket;
import textrads.util.ThreadUtil;

public class Client {
    
    private static enum State {
        NOT_STARTED,
        RUNNING,
        CANCELLED,
    }
    
    private volatile String host;
    private volatile int port = Server.DEFAULT_PORT;
    private volatile boolean player;
    
    private final Object stateMonitor = new Object();
    private State state = State.NOT_STARTED;    
    private Thread listenerThread;    
    private ClientSocketHandler handler;
    
    public void start() {        
        synchronized (stateMonitor) {
            if (state != State.NOT_STARTED) {
                return;
            }
            state = State.RUNNING;
            listenerThread = new Thread(this::listen);
            listenerThread.start();            
        }
    }
    
    private void listen() {
        try {
            outer: while (true) {
                
                ClientSocketHandler h;
                synchronized (stateMonitor) {
                    if (state != State.RUNNING) {
                        break;
                    }
                    h = handler;
                    while (h != null && h.isRunning()) {
                        try {
                            stateMonitor.wait();
                        } catch (final InterruptedException ignored) {
                            continue outer;
                        }
                    }
                }
                
                if (h == null || h.isTerminated()) {
                    try {
                        h = new ClientSocketHandler(this, new Socket(host, port), player);
                        h.start();
                    } catch (final IOException ignored) {
                        ThreadUtil.sleepOneSecond();
                        continue;
                    }
                }
                
                synchronized (stateMonitor) {                    
                    handler = h;                    
                }
            }
        } finally {
            synchronized (stateMonitor) {
                if (handler != null) {
                    handler.stop();
                }
            }
        }
    }
    
    public void handleTerminatedConnection() {
        synchronized (stateMonitor) {
            stateMonitor.notifyAll();
        }
    }
       
    public void update() {   
        final ClientSocketHandler h;
        synchronized (stateMonitor) { 
            h = handler;
            switch (state) {
                case NOT_STARTED:
                    return;
                case CANCELLED: 
                    if ((listenerThread == null || !listenerThread.isAlive()) && (h == null || h.isTerminated())) {
                        state = State.NOT_STARTED;
                    }
                    return;
                default:
                    if (h == null) {
                        return;
                    }
                    break;
            }
        }
        h.update();
    }    
    
    public void stop() {        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
            state = State.CANCELLED;
            ThreadUtil.interrupt(listenerThread);
            if (handler != null) {
                handler.stop();
            }
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

    public boolean isPlayer() {
        return player;
    }

    public void setPlayer(final boolean player) {
        this.player = player;
    }
}
