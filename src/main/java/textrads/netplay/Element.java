package textrads.netplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import textrads.InputEventList;
import textrads.util.IOUtil;

public class Element {
    
    private static final int DEFAULT_PLAYERS = 2;

    private final InputEventList[] inputEvents;
    
    private byte[] data;
    
    public Element() {
        this(DEFAULT_PLAYERS);
    }
    
    public Element(final int players) {
        inputEvents = new InputEventList[players];
        for (int i = players - 1; i >= 0; --i) {
            inputEvents[i] = new InputEventList();
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }
    
    public InputEventList[] getInputEvents() {
        return inputEvents;
    }
    
    public InputEventList getInputEvents(final int player) {
        return inputEvents[player];
    }
    
    public void clear() {
        data = null;
        for (int i = inputEvents.length - 1; i >= 0; --i) {
            inputEvents[i].clear();
        }
    }
    
    public void write(final DataOutputStream out) throws IOException {
        if (data != null) {
            out.write(Command.GAME_STATE);
            IOUtil.writeByteArray(out, data);
        } else {
            out.write(Command.INPUT_EVENTS);
            for (int i = 0; i < inputEvents.length; ++i) {
                inputEvents[i].write(out);
            }
        }
    }
    
    public void readGameState(final DataInputStream in) throws IOException {
        data = IOUtil.readByteArray(in);
    }
    
    public void readInputEvents(final DataInputStream in) throws IOException {
        for (int i = 0; i < inputEvents.length; ++i) {
            inputEvents[i].read(in);
        }
    }
}
