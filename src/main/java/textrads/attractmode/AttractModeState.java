package textrads.attractmode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import textrads.GameState;
import textrads.GameStateSource;
import textrads.InputEvent;
import textrads.MonoGameState;
import textrads.Textrads;
import textrads.ai.Ai;
import textrads.ai.AiSource;

public class AttractModeState {
    
    private static final int DEMO_GARBAGE_HEIGHT = 12;
    private static final int DEMO_FLOOR_HEIGHT = 12;
    private static final int DEMO_LEVEL = 15;
    private static final int DEMO_AI_DIFFICULTY = 22;    
    
    static interface Durations {
        int TITLE_FLASHING = 7;
        int DEMO = 25;
        int RECORDS = 7;
        int PSA = 3;        
    }
    
    private static final float FRAMES_PER_MOVE = Ai.getFramesPerMove(DEMO_AI_DIFFICULTY);
    private static final int FRAMES_PER_DEMO = Durations.DEMO * Textrads.FRAMES_PER_SECOND;
    
    public static enum Mode {
        TITLE_SCREEN,
        DEMO,
        RECORDS,
        PSA,
        DONE,
    }
    
    private class AiState {
        
        private final MonoGameState monoGameState;
        private final Ai ai;        
        private final List<Byte> moves = new ArrayList<>(1024);
        private float moveTimer;
        
        AiState(final int index) {
            monoGameState = GameStateSource.getState().getStates()[index];
            ai = AiSource.getAis()[index];
            moveTimer = Float.MAX_VALUE;
        }
        
        void update() {
            if (monoGameState.isJustSpawned()) {
                moveTimer = FRAMES_PER_MOVE;
                ai.getMoves(moves, monoGameState.getLastAttackRows());
            } 
            --moveTimer;            
            while (moveTimer <= 0) {
                moveTimer += FRAMES_PER_MOVE;
                if (moves.isEmpty()) {
                    monoGameState.handleInputEvent(InputEvent.SOFT_DROP_PRESSED);
                } else {                    
                    monoGameState.handleInputEvent(moves.remove(0));
                }
            }
        }

        public Ai getAi() {
            return ai;
        }
    }
    
    private final List<Byte> gameModes = new ArrayList<>();    
    private final TitleScreenState titleScreenState = new TitleScreenState();
    private final GameState gameState = GameStateSource.getState();
    private final Random random = ThreadLocalRandom.current(); 
    private final AiState[] aiStates = { new AiState(0), new AiState(1) };
    
    private Mode mode = Mode.TITLE_SCREEN;
    private byte demoMode;
    private int timer;
    
    public void reset() {
        titleScreenState.reset();
    }
    
    public void update() {
        switch (mode) {
            case TITLE_SCREEN:
                updateTitleScreen();
                break;
            case DEMO:
                updateDemo();
                break;
        }
    }
    
    private void updateTitleScreen() {
        titleScreenState.update();
        if (titleScreenState.getMode() == TitleScreenState.Mode.DONE) {
            startDemo();            
        }
    }

    private void updateDemo() {
        aiStates[0].update();
        if (demoMode == GameState.Mode.VS_AI) {
            aiStates[1].update();
        }         
        gameState.update();
        
        if (--timer <= 0 || checkLost(0) || checkLost(1)) {
            startRecords();
        }
    }
    
    private boolean checkLost(final int index) {
        return gameState.getStates()[index].getLostTimer() > 110;
    }
    
    private void startRecords() {
        mode = Mode.RECORDS;
    }
    
    private void startDemo() {
        chooseDemoMode();
        final long seed = random.nextLong();
        final int garbageHeight = (demoMode == GameState.Mode.GARBAGE_HEAP) ? DEMO_GARBAGE_HEIGHT : 0;
        final int floorHeight = (demoMode == GameState.Mode.FORTY_LINES) ? DEMO_FLOOR_HEIGHT : 0;
        gameState.init(demoMode, seed, DEMO_LEVEL, garbageHeight, floorHeight, true);
        if (demoMode == GameState.Mode.VS_AI) {
            final boolean findBestMode = random.nextBoolean();
            aiStates[0].getAi().init(demoMode, seed, DEMO_LEVEL, garbageHeight, floorHeight, DEMO_AI_DIFFICULTY, 
                    findBestMode);
            aiStates[1].getAi().init(demoMode, seed, DEMO_LEVEL, garbageHeight, floorHeight, DEMO_AI_DIFFICULTY, 
                    !findBestMode);
        } else {            
            aiStates[0].getAi().init(demoMode, seed, DEMO_LEVEL, garbageHeight, floorHeight, DEMO_AI_DIFFICULTY, true);
        }
        timer = FRAMES_PER_DEMO;
        mode = Mode.DEMO;
    }
    
    public void chooseDemoMode() {        
        if (gameModes.isEmpty()) {
            gameModes.add(GameState.Mode.MARATHON);
            gameModes.add(GameState.Mode.GARBAGE_HEAP);
            gameModes.add(GameState.Mode.RISING_GARBAGE);
            gameModes.add(GameState.Mode.FORTY_LINES);
            gameModes.add(GameState.Mode.INVISIBLE);
            gameModes.add(GameState.Mode.VS_AI);
            Collections.shuffle(gameModes, random);
        }
        demoMode = gameModes.remove(gameModes.size() - 1);
    }
    
    public Mode getMode() {
        return mode;
    }

    TitleScreenState getTitleScreenState() {
        return titleScreenState;
    }
}