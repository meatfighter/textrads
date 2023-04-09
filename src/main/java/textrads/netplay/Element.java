package textrads.netplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import textrads.input.InputEventList;
import textrads.util.IOUtil;

public class Element {
    
    private final InputEventList serverInputEventList = new InputEventList();
    private final InputEventList clientInputEventList = new InputEventList();
    
    private byte[] data;
    
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
