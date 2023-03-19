package textrads.game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import textrads.input.InputEvent;
import textrads.input.InputType;

public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    public static interface Mode {
        byte MARATHON = 0;
        byte CONSTANT_LEVEL = 1;
        byte GARBAGE_HEAP = 2;
        byte RISING_GARBAGE = 3;
        byte THREE_MINUTES = 4;
        byte FORTY_LINES = 5;
        byte NO_ROTATION = 6;
        byte INVISIBLE = 7;
        byte VS_AI = 8;
        byte VS_HUMAN = 9;
        
        static String toString(final byte mode) {
            switch (mode) {
                case MARATHON:
                    return "Marathon";
                case CONSTANT_LEVEL:
                    return "Constant Level";
                case GARBAGE_HEAP:
                    return "Garbage Heap";
                case RISING_GARBAGE:
                    return "Rising Garbage";
                case THREE_MINUTES:
                    return "Three Minutes";
                case FORTY_LINES:
                    return "Forty Lines";
                case NO_ROTATION:
                    return "No Rotation";
                case INVISIBLE:
                    return "Invisible";
                case VS_AI:
                    return "Vs. AI";
                case VS_HUMAN:
                    return "Vs. Human";
                default:
                    return null;
            }
        }
    }

    private static final int MIN_OBJECT_SIZE = 64 * 1024;
    
    private final MonoGameState[] states = { new MonoGameState(this), new MonoGameState(this) };
    
    private boolean paused;
    private byte mode;
    
    public GameState() {
        states[0].setOpponent(states[1]);
        states[1].setOpponent(states[0]);
    }
    
    public void handleInputEvent(final byte event, final int player) {
        if (event == InputEvent.PAUSE_PRESSED && states[player].isPausible()) {
            paused = !paused;
        }
        states[player].handleInputEvent(event);
    }
    
    public void update() {
        if (paused) {
            return;
        }
        states[0].update();
        if (mode == Mode.VS_AI || mode == Mode.VS_HUMAN) {
            states[1].update();
        }
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
        return (mode == Mode.VS_AI || mode == Mode.VS_HUMAN) ? 2 : 1;
    }

    public void init(
            final byte mode, 
            final long seed, 
            final int startingLevel,
            final int garbageHeight, 
            final int floorHeight,
            final boolean skipCountdown,
            final int wins0,
            final int wins1) {
        this.mode = mode;
        states[0].init(seed, startingLevel, garbageHeight, floorHeight, skipCountdown, wins0);
        states[1].init(seed, startingLevel, garbageHeight, floorHeight, skipCountdown, wins1);
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
