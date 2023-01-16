package textrads.netplay;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server {
      
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
    
    private InetAddress host;
    private int port;
    
    public synchronized void start() {
        
    }
    
    public synchronized void stop() {
        
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
