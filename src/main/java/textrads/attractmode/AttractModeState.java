package textrads.attractmode;

import com.googlecode.lanterna.input.KeyStroke;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import textrads.game.GameState;
import textrads.game.GameStateSource;
import textrads.input.InputEvent;
import textrads.input.InputSource;
import textrads.game.MonoGameState;
import textrads.app.Terminator;
import textrads.app.Textrads;
import textrads.ai.Ai;
import textrads.ai.AiSource;
import textrads.db.Database;
import textrads.db.DatabaseSource;

public class AttractModeState {
    
    private static final int DEMO_GARBAGE_HEIGHT = 12;
    private static final int DEMO_FLOOR_HEIGHT = 12;
    private static final int DEMO_LEVEL = 15;
    private static final int DEMO_AI_DIFFICULTY = 22;    
    
    static interface Durations {
        int TITLE_FLASHING = 7;
        int DEMO = 25;
        int RECORDS = 7;
        int PSA = 4;
    }
    
    private static final float FRAMES_PER_MOVE = Ai.getFramesPerMove(DEMO_AI_DIFFICULTY);
    private static final int FRAMES_PER_DEMO = Durations.DEMO * Textrads.FRAMES_PER_SECOND;
    private static final int FRAMES_PER_RECORDS = Durations.RECORDS * Textrads.FRAMES_PER_SECOND;
    private static final int FRAMES_PER_PSA = Durations.PSA * Textrads.FRAMES_PER_SECOND;
    
    public static enum Mode {
        TITLE_SCREEN,
        DEMO,
        RECORDS,
        PSA,
        ENTER_PRESSED,
    }
    
    public static enum Psa {
        WINNERS_DONT_USE_DRUGS,
        RECYCLE_IT_DONT_TRASH_IT,
        HACK_THE_PLANET,
    }
    
    private class AiState {
        
        private final MonoGameState monoGameState;
        private final Ai ai;        
        private final List<Byte> moves = new ArrayList<>(1024);
        private float moveTimer;
        
        AiState(final int index) {
            monoGameState = GameStateSource.getState().getStates()[index];
            ai = AiSource.getAis()[index];            
        }
        
        void reset() {
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
    
    private class RecordsDescriptor {
        
        private final String title;
        private final String key;
        private final AbstractRecordFormatter formatter;
        private final boolean todaysBest;

        public RecordsDescriptor(final String title, final String key, final AbstractRecordFormatter formatter,
                final boolean todaysBest) {
            this.title = title;
            this.key = key;
            this.formatter = formatter;
            this.todaysBest = todaysBest;
        }

        public String getTitle() {
            return title;
        }

        public String getKey() {
            return key;
        }

        public AbstractRecordFormatter getFormatter() {
            return formatter;
        }

        public boolean isTodaysBest() {
            return todaysBest;
        }
    }
    
    private final List<Byte> demoModes = new ArrayList<>();
    private final List<Psa> psas = new ArrayList<>();
    private final PressEnterState pressEnterState = new PressEnterState();
    private final TitleScreenState titleScreenState = new TitleScreenState();
    private final GameState gameState = GameStateSource.getState();
    private final Random random = ThreadLocalRandom.current(); 
    private final AiState[] aiStates = { new AiState(0), new AiState(1) };
    private final Database database = DatabaseSource.getDatabase();
    private final List<RecordsDescriptor> recordsDescriptors = new ArrayList<>();
    private final RecordsState recordsState = new RecordsState();
    private final RecordFormatter recordFormatter = new RecordFormatter();
    private final ExtendedRecordHeightFormatter extendedRecordHeightFormatter = new ExtendedRecordHeightFormatter();
    private final ExtendedRecordDifficultyFormatter extendedRecordDifficultyFormatter 
            = new ExtendedRecordDifficultyFormatter();
    
    private Mode mode;
    private byte demoMode;
    private Psa psa;
    private int timer;
    private boolean demoVsAi;
    private int demoModesIndex;
    private int recordsDescriptorsIndex;
    private int psasIndex;
    
    public AttractModeState() {
        initDemoModes();
        initRecordDescriptors();
        initPsas();
    }
    
    private void initDemoModes() {
        demoVsAi = random.nextBoolean();
        demoModes.clear();
        demoModes.add(GameState.Mode.MARATHON);
        demoModes.add(GameState.Mode.GARBAGE_HEAP);
        demoModes.add(GameState.Mode.RISING_GARBAGE);
        demoModes.add(GameState.Mode.FORTY_LINES);
        demoModes.add(GameState.Mode.NO_ROTATION);
        demoModes.add(GameState.Mode.INVISIBLE);
    }
    
    private void initRecordDescriptors() {
        recordsDescriptors.clear();
        
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Marathon Records", 
                Database.AllTimeKeys.MARATHON, recordFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Constant Level Records", 
                Database.AllTimeKeys.CONSTANT_LEVEL, recordFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Rising Garbage Records", 
                Database.AllTimeKeys.RISING_GARBAGE, recordFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Three Minutes Records", 
                Database.AllTimeKeys.THREE_MINUTES, recordFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Invisible Records", 
                Database.AllTimeKeys.INVISIBLE, recordFormatter, false));
        
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Garbage Heap Records", 
                Database.AllTimeKeys.GARBAGE_HEAP, extendedRecordHeightFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Forty Lines Records", 
                Database.AllTimeKeys.FORTY_LINES, extendedRecordHeightFormatter, false));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Vs. AI Records", 
                Database.AllTimeKeys.VS_AI, extendedRecordDifficultyFormatter, false));
        
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Marathon Records", 
                Database.TodaysKeys.MARATHON, recordFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Constant Level Records", 
                Database.TodaysKeys.CONSTANT_LEVEL, recordFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Rising Garbage Records", 
                Database.TodaysKeys.RISING_GARBAGE, recordFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Three Minutes Records", 
                Database.TodaysKeys.THREE_MINUTES, recordFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Invisible Records", 
                Database.TodaysKeys.INVISIBLE, recordFormatter, true));
        
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Garbage Heap Records", 
                Database.TodaysKeys.GARBAGE_HEAP, extendedRecordHeightFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Forty Lines Records", 
                Database.TodaysKeys.FORTY_LINES, extendedRecordHeightFormatter, true));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Vs. AI Records", 
                Database.TodaysKeys.VS_AI, extendedRecordDifficultyFormatter, true));
    }
    
    private void initPsas() {
        psas.clear();
        for (final Psa value : Psa.values()) {
            psas.add(value);
        }
    }
    
    public void reset() {
        InputSource.clear();
        pressEnterState.reset();
        titleScreenState.reset();
        mode = Mode.TITLE_SCREEN;
    }
    
    public void update() {
        pollForEnter();
        switch (mode) {
            case TITLE_SCREEN:
                updateTitleScreen();
                break;
            case DEMO:
                updateDemo();
                break;
            case RECORDS:
                updateRecords();
                break;
            case PSA:
                updatePsa();
                break;
        }
    }
    
    private void pollForEnter() {
        for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            }
            switch (keyStroke.getKeyType()) {
                case Enter:
                    mode = Mode.ENTER_PRESSED;
                    break;
                case Escape:
                    Terminator.setTerminate(true);
                    break;
            }
        }
    }
    
    private void updateTitleScreen() {
        titleScreenState.update();
        switch (titleScreenState.getMode()) {
            case PRESS_ENTER_FLASHING:
                pressEnterState.update();
                break;
            case DONE:
                startDemo();
                break;                
        }
    }

    private void updateDemo() {
        pressEnterState.update();
        aiStates[0].update();
        if (demoMode == GameState.Mode.VS_AI) {
            aiStates[1].update();
        }         
        gameState.update();
        
        if (--timer <= 0 || checkLost(0) || (demoMode == GameState.Mode.VS_AI && checkLost(1))) {
            startRecords();
        }
    }
    
    private void updateRecords() {
        recordsState.update();
        if (--timer <= 0) {
            startPsa();
        }
    }
    
    private void updatePsa() {
        if (--timer <= 0) {
            reset();
        }
    }
    
    private boolean checkLost(final int index) {
        return gameState.getStates()[index].getEndTimer() > 110;
    }
    
    private void startRecords() {
        chooseRecords();
        timer = FRAMES_PER_RECORDS;
        mode = Mode.RECORDS;
    }
    
    private void startDemo() {
        chooseDemoMode();
        final long seed = random.nextLong();
        final int garbageHeight = (demoMode == GameState.Mode.GARBAGE_HEAP) ? DEMO_GARBAGE_HEIGHT : 0;
        final int floorHeight = (demoMode == GameState.Mode.FORTY_LINES) ? DEMO_FLOOR_HEIGHT : 0;
        gameState.init(demoMode, seed, DEMO_LEVEL, garbageHeight, floorHeight, true, 0, 0);
        aiStates[0].reset();
        if (demoMode == GameState.Mode.VS_AI) {
            aiStates[1].reset();
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
    
    private void startPsa() {
        choosePsa();
        timer = FRAMES_PER_PSA;
        mode = Mode.PSA;
    }

    private void chooseDemoMode() {
        demoVsAi = !demoVsAi;
        if (demoVsAi) {
            demoMode = GameState.Mode.VS_AI;
            return;
        }
        if (--demoModesIndex < 0) {
            demoModesIndex = demoModes.size() - 1;
            Collections.shuffle(demoModes, random);
        }        
        demoMode = demoModes.get(demoModesIndex);
    }
    
    private void chooseRecords() {
        if (--recordsDescriptorsIndex < 0) {
            recordsDescriptorsIndex = recordsDescriptors.size() - 1;
            Collections.shuffle(recordsDescriptors, random);
        }
        final RecordsDescriptor recordsDescriptor = recordsDescriptors.get(recordsDescriptorsIndex);        
        recordsState.init(recordsDescriptor.getTitle(), database.get(recordsDescriptor.getKey(), 
                recordsDescriptor.isTodaysBest()), recordsDescriptor.getFormatter());
    }
    
    private void choosePsa() {
        if (--psasIndex < 0) {
            psasIndex = psas.size() - 1;
            Collections.shuffle(psas, random);
        }
        psa = psas.get(psasIndex);
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public boolean isEnterPressed() {
        return mode == Mode.ENTER_PRESSED;
    }

    public PressEnterState getPressEnterState() {
        return pressEnterState;
    }

    TitleScreenState getTitleScreenState() {
        return titleScreenState;
    }
    
    RecordsState getRecordsState() {
        return recordsState;
    }

    public Psa getPsa() {
        return psa;
    }
}