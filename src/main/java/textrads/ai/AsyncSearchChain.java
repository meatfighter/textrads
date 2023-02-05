package textrads.ai;

import java.util.ArrayList;
import java.util.List;

public class AsyncSearchChain {

    private final List<Coordinate> moves = new ArrayList<>();
    private final Thread thread = new Thread(this::loop);;    
    
    public void init() {
        thread.start();
    }
    
    private void loop() {
        while (true) {
            
        }
    }
}
