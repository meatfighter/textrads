package textrads.netplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server {
     
    private static final Integer DEFAULT_PORT = 8080;
    private static final int BACKLOG = 50;    
    
    public static enum Error {
        NETWORK_INTERFACE_ADDRESSES,
        SERVER_SOCKET,
    }
    
    public static List<InetAddress> getNetworkInterfaceAddresses() {
        final Set<InetAddress> addressSet = new HashSet<>();
        try {
            Collections.list(NetworkInterface.getNetworkInterfaces()).forEach(
                    i -> addressSet.addAll(Collections.list(i.getInetAddresses())));
        } catch (final SocketException ignored) {
        }
        
        final List<InetAddress> addresses = new ArrayList<>(addressSet);
        Collections.sort(addresses, Comparator.comparing(InetAddress::toString));
        return addresses;
    }
    
    private volatile InetAddress bindAddress;
    private volatile Integer port;
    
    private final List<ServerSocketHandler> handlers = new ArrayList<>();
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    private volatile Thread listenerThread;
    private volatile Thread heartbeatThread;
    private volatile ServerSocket serverSocket;
    
    private volatile Error error;
    private volatile boolean playerConnected;
    
    public void start() {
        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
        }
        
        listenerThread = new Thread(this::listen);
        listenerThread.start();
        heartbeatThread = new Thread(this::sendHeartbeats);
        heartbeatThread.start();
    }
    
    public void stop() {
        
        synchronized (monitor) {
            if (!running || cancelled) {
                return;
            }
            cancelled = true;
        }
               
        closeServerSocket();
        interruptThread(listenerThread);
        interruptThread(heartbeatThread);
    }
    
    private void interruptThread(final Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    private void joinThread(final Thread thread) {
        if (thread != null) {
            while (true) {
                try {
                    thread.join();
                    return;
                } catch (final InterruptedException ignored) {                
                }
            }
        }
    }
    
    private void sendHeartbeats() {
        while (true) {
            
        }
    }
    
    private void listen() {
        try {
            outer: while (true) {                
                synchronized (monitor) {
                    if (cancelled) {
                        break;
                    }
                }
                closeServerSocket();
                               
                InetAddress address = bindAddress;
                if (address == null) {
                    final List<InetAddress> addresses = getNetworkInterfaceAddresses();
                    if (addresses.isEmpty()) {
                        setError(Error.NETWORK_INTERFACE_ADDRESSES);
                        break;
                    }
                    address = addresses.get(0);
                }
                Integer p = port;
                if (p == null) {
                    p = DEFAULT_PORT;
                }
                
                try {
                    serverSocket = new ServerSocket(p, BACKLOG, address);
                } catch (final IOException ignored) {
                    setError(Error.SERVER_SOCKET);
                    break;
                }
                
                while (true) {
                    synchronized (monitor) {
                        if (cancelled) {
                            break outer;
                        }
                    }
                    
                    try {
                        final ServerSocketHandler handler = new ServerSocketHandler(this, serverSocket.accept());
                        synchronized (handlers) {
                            handlers.add(handler);
                        }
                    } catch (final IOException ignored) {
                    }
                }
            }
        } finally {
            closeServerSocket();
            joinThread(heartbeatThread);
            serverSocket = null;
            listenerThread = null;
            heartbeatThread = null;
            synchronized (monitor) {
                running = cancelled = false;
            }
        }
    }
    
    void removeHandler(final ServerSocketHandler handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }
    
    private void closeServerSocket() {
        try {   
            final ServerSocket ss = serverSocket;
            if (ss != null) {
                ss.close();
            }
        } catch (final IOException ignored) {            
        }
    }

    public InetAddress getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(final InetAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(final Integer port) {
        this.port = port;
    }

    public Error getError() {
        return error;
    }

    public void setError(final Error error) {
        this.error = error;
    }

    public boolean isPlayerConnected() {
        return playerConnected;
    }
}
