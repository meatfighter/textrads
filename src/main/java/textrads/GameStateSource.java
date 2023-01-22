package textrads;

import java.io.IOException;

public final class GameStateSource {

    private static volatile GameState state = new GameState();
    
    public static void setState(final GameState state) {
        GameStateSource.state = state;
    }
    
    public static void setState(final byte[] state) throws IOException, ClassNotFoundException {
        GameStateSource.state = GameState.fromByteArray(state);
    }    
    
    public static GameState getState() {
        return state;
    }
    
    private GameStateSource() {
    }
}
