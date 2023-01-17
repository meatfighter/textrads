package textrads;

import java.io.Serializable;

public class GameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final MonoGameState[] states = new MonoGameState[2];
    
    private boolean paused;
    
    public GameState() {
        for (int i = states.length - 1; i >= 0; --i) {
            states[i] = new MonoGameState();
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
}
