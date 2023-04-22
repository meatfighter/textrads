package textrads.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IOUtil {
    
    private static final int MAX_BYTE_ARRAY_LENGTH = 1024 * 1024;
    
    private static final int BAOS_INITIAL_SIZE = 64 * 1024;
    
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
        
        public NetworkInterfaceAddress(final String name) {
            this(name, null);
        }
        
        public NetworkInterfaceAddress(final String name, final InetAddress address) {
            this.name = name;
            this.address = address;
            sortPriority = 0;
        }
        
        public NetworkInterfaceAddress(final NetworkInterface networkInterface, final InetAddress address) {
            this.address = address;
            this.name = String.format("%s (%s)", networkInterface.getDisplayName(), address.getHostAddress());
            
            final String n = networkInterface.getName().toLowerCase();
            final String d = networkInterface.getDisplayName().toLowerCase();            
            sortPriority = (n.startsWith("l") || d.contains("loop") || d.contains("local") ? 0 : 32)
                    | (n.startsWith("e") || d.contains("ethernet") ? 0 : 16)
                    | (n.startsWith("w") || d.contains("wireless") ? 0 : 8)
                    | (n.startsWith("p") || d.contains("point") ? 0 : 4)
                    | (n.startsWith("v") || d.contains("virtual") ? 0 : 2)
                    | (n.startsWith("t") || d.contains("tunnel") ? 0 : 1);
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
                NETWORK_INTERFACE_ADDRESSES.add(new NetworkInterfaceAddress(networkInterface, address));
            });
        });
        Collections.sort(NETWORK_INTERFACE_ADDRESSES);
        NETWORK_INTERFACE_ADDRESSES.add(0, new NetworkInterfaceAddress("Any/all local addresses"));
    }
    
    public static List<NetworkInterfaceAddress> getNetworkInterfaceAddresses() {
        return NETWORK_INTERFACE_ADDRESSES;
    }    
    
    public static void writeByteArray(final DataOutputStream out, final byte[] data) throws IOException {
        if (data == null) {
            out.writeInt(0);
            return;
        }
        out.writeInt(data.length);
        out.write(data);
    }
    
    public static byte[] readByteArray(final DataInputStream in) throws IOException {
        final int length = in.readInt();
        if (length < 0 || length > MAX_BYTE_ARRAY_LENGTH) {
            throw new IOException("Invalid byte array length.");
        }
        if (length == 0) {
            return null;
        }
        final byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }
    
    public static byte[] toByteArray(final Serializable obj) throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(BAOS_INITIAL_SIZE)) {
            try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(obj);
            }
            return baos.toByteArray();
        }
    }
    
    public static <T> T fromByteArray(final byte[] data) throws IOException, ClassNotFoundException {
        try (final ByteArrayInputStream bais = new ByteArrayInputStream(data);
                final ObjectInputStream ois = new ObjectInputStream(bais)) {
            return (T) ois.readObject();
        }
    }    
    
    private IOUtil() {        
    }
}
