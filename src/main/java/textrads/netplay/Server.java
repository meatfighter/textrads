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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import textrads.util.ThreadUtil;

public class Server {
     
    public static final int DEFAULT_PORT = 8080;

    public static final long HEARTBEAT_PERIOD = TimeUnit.SECONDS.toMillis(10);
    
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
    private volatile int port = DEFAULT_PORT;
    
    private final List<ServerSocketHandler> handlers = new CopyOnWriteArrayList<>();
    
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
        
        final Thread listThread = new Thread(this::listen);
        listenerThread = listThread;
        listThread.start();
        
        final Thread heartThread = new Thread(this::sendHeartbeats);
        heartbeatThread = heartThread;
        heartThread.start();
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
               
        closeServerSocket();
        ThreadUtil.interruptThread(listenerThread);
        ThreadUtil.interruptThread(heartbeatThread);
    }
    
    private void sendHeartbeats() {
//        while (true) {
//            handlers.forEach(handler -> {
//                handler.sendHeartbeat();
//            });
//        }
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
                
                try {
                    serverSocket = new ServerSocket(port, BACKLOG, address);
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
                        handlers.add(new ServerSocketHandler(this, serverSocket.accept()));
                    } catch (final IOException ignored) {
                    }
                }
            }
        } finally {
            closeServerSocket();
            ThreadUtil.joinThread(heartbeatThread);
            serverSocket = null;
            listenerThread = null;
            heartbeatThread = null;
            synchronized (monitor) {
                running = cancelled = false;
            }
        }
    }
    
    void removeHandler(final ServerSocketHandler handler) {
        handlers.remove(handler);
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
