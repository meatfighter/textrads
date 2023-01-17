package textrads;

public final class GameStateSource {

    private static volatile GameState state;
    
    public static void setState(final GameState state) {
        GameStateSource.state = state;
    }
    
    public static GameState getState() {
        return state;
    }
    
    private GameStateSource() {
    }
}
