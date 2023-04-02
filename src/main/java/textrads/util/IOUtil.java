package textrads.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IOUtil {
    
    private static final int MAX_BYTE_ARRAY_LENGTH = 1024 * 1024;
    
    private static final List<NetworkInterfaceAddress> NETWORK_INTERFACE_ADDRESSES = new ArrayList<>();
    
    static {
        try {
            init();
        } catch (final SocketException ignored) {            
        }
    }    
    
    public static class NetworkInterfaceAddress implements Comparable<NetworkInterfaceAddress> {
        
        private final InetAddress address;
        private final String name;
        private final int sortPriority;
        
        public NetworkInterfaceAddress(final InetAddress address, final String name) {
            this.address = address;
            this.name = name;
            
            final String n = name.toLowerCase();
            sortPriority = (n.contains("local") ? 0 : 0x10)
                    | (n.contains("loop") ? 0 : 0x08)
                    | (n.contains("ethernet") ? 0 : 0x04)
                    | (n.contains("wireless") ? 0 : 0x02)          
                    | (n.contains("wi") && n.contains("fi") ? 0 : 0x01);
        }

        @Override
        public int compareTo(final NetworkInterfaceAddress o) {
            
            final int priorityComparison = Integer.compare(sortPriority, o.sortPriority);
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            return String.CASE_INSENSITIVE_ORDER.compare(name, o.name);
        }
        
        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            return name.equals(((NetworkInterfaceAddress) obj).name);
        }

        public InetAddress getAddress() {
            return address;
        }

        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    public static void init() throws SocketException {
        if (!NETWORK_INTERFACE_ADDRESSES.isEmpty()) {
            return;
        }
        Collections.list(NetworkInterface.getNetworkInterfaces()).forEach(networkInterface -> {
            Collections.list(networkInterface.getInetAddresses()).forEach(address -> {
                NETWORK_INTERFACE_ADDRESSES.add(new NetworkInterfaceAddress(address, 
                        String.format("%s: %s", networkInterface.getDisplayName(), address)));
            });
        });
        Collections.sort(NETWORK_INTERFACE_ADDRESSES);
        NETWORK_INTERFACE_ADDRESSES.add(0, new NetworkInterfaceAddress(null, "Any/all local addresses"));
    }
    
    public static List<NetworkInterfaceAddress> getNetworkInterfaceAddresses() {
        return NETWORK_INTERFACE_ADDRESSES;
    }    
    
    public static void writeByteArray(final DataOutputStream out, final byte[] data) throws IOException {
        out.writeInt(data.length);
        out.write(data);
    }
    
    public static byte[] readByteArray(final DataInputStream in) throws IOException {
        final int length = in.readInt();
        if (length < 0 || length > MAX_BYTE_ARRAY_LENGTH) {
            throw new IOException("invalid byte array length");
        }
        final byte[] data = new byte[in.readInt()];
        in.readFully(data);
        return data;
    }
    
    private IOUtil() {        
    }
}
