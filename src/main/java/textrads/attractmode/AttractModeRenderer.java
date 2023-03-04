package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.GameRenderer;
import textrads.GameStateSource;

public class AttractModeRenderer {
    
    private final TitleScreenRenderer titleScreenRenderer = new TitleScreenRenderer();
    private final GameRenderer gameRenderer = new GameRenderer();
    private final WinnersDontUseDrugsRenderer winnersDontUseDrugsRenderer = new WinnersDontUseDrugsRenderer();
    private final RecycleItDontTrashItRenderer recycleItDontTrashItRenderer = new RecycleItDontTrashItRenderer();
    private final HackThePlanetRenderer hackThePlanetRenderer = new HackThePlanetRenderer();    

    public void render(final TextGraphics g, final TerminalSize size, final AttractModeState state) {
        
        switch(state.getMode()) {
            case TITLE_SCREEN:
                titleScreenRenderer.render(g, size, state.getTitleScreenState());
                break;
            case DEMO:
                gameRenderer.render(g, size, GameStateSource.getState());
                break;
        }
    }
}
