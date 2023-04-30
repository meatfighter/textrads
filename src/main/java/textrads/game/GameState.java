package textrads.game;

import java.io.Serializable;
import textrads.input.InputEvent;

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

    private final MonoGameState[] states = { new MonoGameState(this), new MonoGameState(this) };
    
    private boolean paused;
    private byte mode;
    private byte selection;
    
    public GameState() {
        states[0].setOpponent(states[1]);
        states[1].setOpponent(states[0]);
    }
    
    public void handleInputEvent(final byte event, final int player) {
        if (event == InputEvent.PAUSE_PRESSED && states[player].isPausible()) {
            paused = !paused;
            return;
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
    
    public boolean isContinueMessage() {
        return (mode == GameState.Mode.VS_AI || mode == GameState.Mode.VS_HUMAN) 
                ? (states[0].getEndTimer() >= 110 && states[1].getEndTimer() >= 110) 
                : (states[0].isWon() || states[0].getEndTimer() >= 110);
    }
    
    public boolean isEnd() {
        switch (mode) {
            case Mode.VS_AI:
            case Mode.VS_HUMAN:
                return states[0].isEnd() && states[1].isEnd();
            default:
                return states[0].isEnd();
        }
    }
    
    public int getIndex(final MonoGameState state) {
        return (state == states[0]) ? 0 : 1;
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
            final int level0,
            final int level1,
            final int garbageHeight, 
            final int floorHeight,
            final boolean skipCountdown,
            final int wins0,
            final int wins1) {
                
        paused = false;
        this.mode = mode;
        selection = -1;
        
        states[0].init(seed, level0, garbageHeight, floorHeight, skipCountdown, wins0);
        states[1].init(seed, level1, garbageHeight, floorHeight, skipCountdown, wins1);
    }

    public byte getMode() {
        return mode;
    }

    public byte getSelection() {
        return selection;
    }

    public void setSelection(final byte selection) {
        this.selection = selection;
    }
}
