package textrads.ai;

public final class AiSource {
    
    private static final Ai[] ais = { new Ai(), new Ai() };
    
    public static Ai[] getAis() {
        return ais;
    }
    
    private AiSource() {        
    }
}
