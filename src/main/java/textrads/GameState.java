package textrads;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int MIN_OBJECT_SIZE = 64 * 1024;
    
    public static final byte MARATHON_MODE = 0;
    public static final byte CONSTANT_LEVEL = 1;
    public static final byte GARBAGE_HEAP_MODE = 2;
    public static final byte RISING_GARBAGE_MODE = 3;
    public static final byte THREE_MINUTES_MODE = 4;
    public static final byte FORTY_LINES_MODE = 5;
    public static final byte VS_AI_MODE = 6;
    public static final byte VS_HUMAN_MODE = 7;
    
    private final MonoGameState[] states = { new MonoGameState(this), new MonoGameState(this) };
    
    private boolean paused;
    private byte mode;
    
    public GameState() {
        states[0].setOpponent(states[1]);
        states[1].setOpponent(states[0]);
    }
    
    public void handleInputEvent(final byte event, final int player) {
        states[player].handleInputEvent(event);
    }
    
    public void update() {        
        states[0].update();
        states[1].update();
    }

    public MonoGameState[] getStates() {
        return states;
    }
    
    public boolean isPaused() {
        return paused;
    }

    public void setPaused(final boolean paused) {
        this.paused = paused;
    }

    public int getPlayers() {
        return (mode == VS_AI_MODE || mode == VS_HUMAN_MODE) ? 2 : 1;
    }

    public void init(final byte mode, final long seed) {
        this.mode = mode;
        states[0].init(seed, 0, 0, 10);
        states[1].init(seed, 0, 0, 0);
    }

    public byte getMode() {
        return mode;
    }
    
    public byte[] toByteArray() throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(MIN_OBJECT_SIZE)) {
            try (final ObjectOutputStream oos = new ObjectOutputStream(baos)) { // TODO MEASURE SIZE
                oos.writeObject(this);
            }
            return baos.toByteArray();
        }
    }
    
    public static GameState fromByteArray(final byte[] data) throws IOException, ClassNotFoundException {
        try (final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            final Object obj = ois.readObject();
            if (!(obj instanceof GameState)) {
                throw new IOException("invalid object");
            }
            return (GameState) obj;
        }
    }
}
