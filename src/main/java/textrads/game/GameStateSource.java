package textrads.game;

public final class GameStateSource {

    private static volatile GameState state = new GameState();
    
    public static void setState(final GameState state) {
        GameStateSource.state = state;
    }
    
    public static GameState getState() {
        return state;
    }
    
    private GameStateSource() {
    }
}
