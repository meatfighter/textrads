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
    
    private final MonoGameState[] states = { new MonoGameState(this), new MonoGameState(this) };
    
    private int updates;
    private byte players;
    private boolean paused;
    
    public GameState() {
        states[0].setOpponent(states[1]);
        states[1].setOpponent(states[0]);
        
        states[0].init();
        states[1].init();
    }
    
    public void handleInputEvent(final byte event, final int player) {
        states[player].handleInputEvent(event);
    }
    
    public void update() {
        states[0].update();
        states[1].update();
        ++updates;
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

    public int getUpdates() {
        return updates;
    }

    public byte getPlayers() {
        return players;
    }

    public void setPlayers(final byte players) {
        this.players = players;
    }
    
    public void setSeed(final long seed) {
        states[0].setSeed(seed);
        states[1].setSeed(seed);
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
