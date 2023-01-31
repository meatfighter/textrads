package textrads.ai;

// TODO REMOVE THIS TEST CLASS (move to private project)

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TestAI {

    public void launch() throws Exception {
        
        final boolean[][] playfield = Playfield.createPlayfield();        
        final SearchChain searchChain = new SearchChain();
        
        final Random random = ThreadLocalRandom.current();
        int current = random.nextInt(7);
        int next = random.nextInt(7);
        
        while (true) {          
            System.out.format("%d %d%n", current, next);
            Playfield.print(playfield);
            searchChain.search(current, next, playfield, 52, (byte) 25, 20);

            if (searchChain.isBestFound()) {
                Playfield.lock(playfield, current, searchChain.getX(), searchChain.getY(), searchChain.getRotation());
                Playfield.print(playfield);
                current = next;
                next = random.nextInt(7);
                
    //            System.out.format("%d %d %d%n", searchChain.getX(), searchChain.getY(), searchChain.getRotation());
    //            
    //            final List<Byte> moves = new ArrayList<>();
    //            searchChain.getMoves(moves);
    //            System.out.println(moves);
            } else {
                break;
            }
        }
    }
    
    public static void main(final String... args) throws Exception {
        new TestAI().launch();
    }
}
