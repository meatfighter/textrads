package textrads;

import java.io.Serializable;

public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final MonoGameState[] states = { new MonoGameState(this), new MonoGameState(this) };
    
    private int updates;
    private boolean paused;
    
    public GameState() {
        states[0].setOpponent(states[1]);
        states[1].setOpponent(states[0]);
        
        states[0].init();
        states[1].init();
    }
    
    public void handleInputEvent(final int event, final int player) {
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
}
