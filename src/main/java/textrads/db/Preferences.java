package textrads.db;

import java.io.Serializable;
import textrads.play.GameState;

public class Preferences implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int VALUES = 13;
    
    private final byte[] values;    
    private final String initials;
    
    public Preferences() {
        values = new byte[VALUES];
        for (int i = VALUES - 1; i >= 0; --i) {
            values[i] = -1;
        }
        initials = null;
    }
    
    private Preferences(final byte[] values, final String initials) {
        this.values = values;
        this.initials = initials;
    }

    public byte getLevel(final byte mode) {
        return values[mode];
    }
    
    public Preferences setLevel(final byte mode, final byte value) {
        final byte[] vs = new byte[VALUES];
        System.arraycopy(values, 0, vs, 0, VALUES);
        vs[mode] = value;
        return new Preferences(vs, initials);
    }
    
    public byte getChallenge(final byte mode) {
        switch (mode) {
            case GameState.Mode.GARBAGE_HEAP:
                return values[10];
            case GameState.Mode.FORTY_LINES:
                return values[11];
            case GameState.Mode.VS_AI:
                return values[12];
            default:
                return -1;
        }
    }
    
    public Preferences setChallenge(final byte mode, final byte value) {
        final byte[] vs = new byte[VALUES];
        System.arraycopy(values, 0, vs, 0, VALUES);
        switch (mode) {
            case GameState.Mode.GARBAGE_HEAP:
                vs[10] = value;
                break;
            case GameState.Mode.FORTY_LINES:
                vs[11] = value;
                break;
            case GameState.Mode.VS_AI:
                vs[12] = value;
                break;
        }
        return new Preferences(vs, initials);
    }    
    
    public String getInitials() {
        return initials;
    }
    
    public Preferences setInitials(final String initials) {
        return new Preferences(values, initials);
    }
}