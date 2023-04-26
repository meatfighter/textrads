package textrads.netplay;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import textrads.input.InputEventList;
import textrads.util.IOUtil;

public class Message {
    
    private static final int DEFAULT_PLAYERS = 2;
    
    public static interface Type {
        byte HEARTBEAT = 0;
        byte INPUT_EVENTS = 1;
        byte HANDSHAKE = 2;
        byte GET_LEVEL = 3;
        byte LEVEL = 4;
        byte WAIT_LEVEL = 5;
        byte GAME_STATE = 6;
        byte ACK_GAME_STATE = 7;
        byte PLAY = 8;
    }    
    
    private final InputEventList[] inputEvents;
    
    private byte type;
    private byte[] data;

    public Message() {
        this(DEFAULT_PLAYERS);
    }

    public Message(final int players) {
        inputEvents = new InputEventList[players];
        for (int i = players - 1; i >= 0; --i) {
            inputEvents[i] = new InputEventList();
        }
    }

    public byte getType() {
        return type;
    }

    public void setType(final byte type) {
        this.type = type;
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
        out.write(type);
        switch (type) {
            case Type.HEARTBEAT:
                break;
            case Type.INPUT_EVENTS:
                for (int i = 0; i < inputEvents.length; ++i) {
                    inputEvents[i].write(out);
                }
                break;
            default:
                IOUtil.writeByteArray(out, data);
                break;
        }
    }
    
    public void read(final DataInputStream in) throws IOException {
        read(in, in.readByte());
    }
    
    public void read(final DataInputStream in, final byte type) throws IOException {
        this.type = type;
        switch (type) {
            case Type.HEARTBEAT:
                break;
            case Type.INPUT_EVENTS:
                for (int i = 0; i < inputEvents.length; ++i) {
                    inputEvents[i].read(in);
                }
                break;
            default:
                data = IOUtil.readByteArray(in);
                break;
        }
    }   
}
