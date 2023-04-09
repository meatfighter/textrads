package textrads.db;

import java.io.Serializable;

public class NetplayConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static final int DEFAULT_PORT = 8080;
    public static final byte DEFAULT_LEVEL = -1;
    
    private final String host;
    private final int port;
    private final byte level;
    
    public NetplayConfig() {
        this(null, DEFAULT_PORT, DEFAULT_LEVEL);
    }

    public NetplayConfig(final String host, final int port, final byte level) {
        this.host = host;
        this.port = port;
        this.level = level;
    }
    
    public NetplayConfig setHost(final String host) {
        return new NetplayConfig(host, port, level);
    }

    public String getHost() {
        return host;
    }
    
    public NetplayConfig setPort(final int port) {
        return new NetplayConfig(host, port, level);
    }

    public int getPort() {
        return port;
    }
    
    public NetplayConfig setLevel(final byte level) {
        return new NetplayConfig(host, port, level);
    }

    public byte getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return String.format("host = %s, port = %s, level = %s", host, port, level);
    }
}
