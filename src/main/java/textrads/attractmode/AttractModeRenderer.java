package textrads.attractmode;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.play.GameRenderer;
import textrads.play.GameStateSource;

public class AttractModeRenderer {
    
    private final TitleScreenRenderer titleScreenRenderer = new TitleScreenRenderer();
    private final GameRenderer gameRenderer = new GameRenderer();
    private final RecordsRenderer recordsRenderer = new RecordsRenderer();
    private final WinnersDontUseDrugsRenderer winnersDontUseDrugsRenderer = new WinnersDontUseDrugsRenderer();
    private final RecycleItDontTrashItRenderer recycleItDontTrashItRenderer = new RecycleItDontTrashItRenderer();
    private final HackThePlanetRenderer hackThePlanetRenderer = new HackThePlanetRenderer();    

    public void render(final TextGraphics g, final TerminalSize size, final AttractModeState state) {
        
        switch(state.getMode()) {
            case TITLE_SCREEN:
                titleScreenRenderer.render(g, size, state.getTitleScreenState(), state.getPressEnterState());
                break;
            case DEMO:
                gameRenderer.render(g, size, GameStateSource.getState(), state.getPressEnterState());
                break;
            case RECORDS:
                recordsRenderer.render(g, size, state.getRecordsState());
                break;
            case PSA:
                switch (state.getPsa()) {
                    case WINNERS_DONT_USE_DRUGS:
                        winnersDontUseDrugsRenderer.render(g, size);
                        break;
                    case RECYCLE_IT_DONT_TRASH_IT:
                        recycleItDontTrashItRenderer.render(g, size);
                        break;
                    case HACK_THE_PLANET:
                        hackThePlanetRenderer.render(g, size);
                        break;
                }
                break;
        }
    }
}
