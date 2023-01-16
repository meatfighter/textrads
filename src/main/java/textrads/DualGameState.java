package textrads;

import java.io.Serializable;

public class DualGameState implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final MonoGameState[] states = new MonoGameState[2];
    
    public DualGameState() {
        for (int i = states.length - 1; i >= 0; --i) {
            states[i] = new MonoGameState();
        }
    }

    public MonoGameState[] getStates() {
        return states;
    }
}
