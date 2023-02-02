package textrads.ai;

// TODO REMOVE THIS TEST CLASS (move to private project)

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import textrads.Tetromino;

public class TestAI {

    public void launch() throws Exception {
  
//        System.out.println(Tetromino.TETROMINOES[Tetromino.I_TYPE][0].validPosition[19 + 2][7 + 2]);
        
        final boolean[][] playfield = Playfield.createPlayfield();        
        final SearchChain searchChain = new SearchChain();
  
//        final boolean[][] playfield = {  
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { false, false, false, false, false, false, false, false, false, false, }, 
//            { true, true, true, false, false, false, false, false, false, false, }, 
//            { true, true, true, false, false, false, false, false, false, false, }, 
//            { true, true, true, false, false, true, true, false, false, false, }, 
//            { true, true, true, true, true, true, true, true, false, false, }, 
//            { true, true, true, true, true, true, true, true, true, false, },            
//        };
        
        
//        final int[] sequence = { 5, 0, 6, 4, 2, 2, 1, 6 };      
//        for (int i = 0; i < sequence.length - 1; ++i) {
//            searchChain.search(sequence[i], sequence[i + 1], playfield, 52, (byte) 25, 20);
//            Playfield.lock(playfield, sequence[i], searchChain.getX(), searchChain.getY(), searchChain.getRotation());
//            Playfield.print(playfield);
//        }
        //System.out.println(Playfield.toBooleanString(playfield));
        
        //searchChain = new SearchChain();
//        searchChain.testing = true;
//        searchChain.search(6, 5, playfield, 52, (byte) 25, 20);
        
        
        
        final Random random = ThreadLocalRandom.current();
        int current = random.nextInt(7);
        int next = random.nextInt(7);
        
        int lines = 0;
        while (true) {          
            //System.out.format("%d %d%n", current, next);
            //Playfield.print(playfield);
            searchChain.search(current, next, playfield, 52, (byte) 25, 1);

            if (searchChain.isBestFound()) {
                lines += Playfield.lock(playfield, current, searchChain.getX(), searchChain.getY(), searchChain.getRotation());
                //Playfield.print(playfield);
                System.out.println(lines);
                current = next;
                next = random.nextInt(7);
                
    //            System.out.format("%d %d %d%n", searchChain.getX(), searchChain.getY(), searchChain.getRotation());
    //            
    //            final List<Byte> moves = new ArrayList<>();
    //            searchChain.getMoves(moves);
    //            System.out.println(moves);
            } else {
                System.out.println("game over");
                break;
            }
        }
    }
    
    public static void main(final String... args) throws Exception {
        new TestAI().launch();
    }
}
