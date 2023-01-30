package textrads.ai;

// TODO REMOVE THIS TEST CLASS (move to private project)

import java.util.ArrayList;
import java.util.List;

public class TestAI {

    public void launch() throws Exception {
        
        final boolean[][] playfield = Playfield.createPlayfield();
        Playfield.print(playfield);
        
        final SearchChain searchChain = new SearchChain();
        searchChain.search(6, 5, playfield, 52, (byte) 25, 20);
        
        if (searchChain.isBestFound()) {
            System.out.format("%d %d %d%n", searchChain.getX(), searchChain.getY(), searchChain.getRotation());
            
            final List<Byte> moves = new ArrayList<>();
            searchChain.getMoves(moves);
            System.out.println(moves);
        }
    }
    
    public static void main(final String... args) throws Exception {
        new TestAI().launch();
    }
}
