package textrads.ai;

public final class AiSource {
    
    private static final Ai[] ais = { new Ai(), new Ai() };
    
    public static Ai getAi(final int index) {
        return ais[index];
    }
    
    private AiSource() {        
    }
}
