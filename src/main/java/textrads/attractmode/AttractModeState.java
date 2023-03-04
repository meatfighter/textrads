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
        DONE,
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

        public RecordsDescriptor(final String title, final String key, final AbstractRecordFormatter formatter) {
            this.title = title;
            this.key = key;
            this.formatter = formatter;
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
    }
    
    private final List<Byte> demoModes = new ArrayList<>();
    private final List<Psa> psas = new ArrayList<>();
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
    private int demoModesIndex;
    private int recordsDescriptorsIndex;
    private int psasIndex;
    
    public AttractModeState() {
        initDemoModes();
        initRecordDescriptors();
        initPsas();
    }
    
    private void initDemoModes() {
        demoModes.clear();
        demoModes.add(GameState.Mode.MARATHON);
        demoModes.add(GameState.Mode.GARBAGE_HEAP);
        demoModes.add(GameState.Mode.RISING_GARBAGE);
        demoModes.add(GameState.Mode.FORTY_LINES);
        demoModes.add(GameState.Mode.NO_ROTATION);
        demoModes.add(GameState.Mode.INVISIBLE);
        demoModes.add(GameState.Mode.VS_AI);
    }
    
    private void initRecordDescriptors() {
        recordsDescriptors.clear();
        
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Marathon Records", 
                Database.AllTimeKeys.MARATHON, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Constant Level Records", 
                Database.AllTimeKeys.CONSTANT_LEVEL, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Rising Garbage Records", 
                Database.AllTimeKeys.RISING_GARBAGE, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Three Minutes Records", 
                Database.AllTimeKeys.THREE_MINUTES, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Invisible Records", 
                Database.AllTimeKeys.INVISIBLE, recordFormatter));
        
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Garbage Heap Records", 
                Database.AllTimeKeys.GARBAGE_HEAP, extendedRecordHeightFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Forty Lines Records", 
                Database.AllTimeKeys.FORTY_LINES, extendedRecordHeightFormatter));
        recordsDescriptors.add(new RecordsDescriptor("All Time Best Vs. AI Records", 
                Database.AllTimeKeys.VS_AI, extendedRecordDifficultyFormatter));
        
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Marathon Records", 
                Database.TodaysKeys.MARATHON, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Constant Level Records", 
                Database.TodaysKeys.CONSTANT_LEVEL, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Rising Garbage Records", 
                Database.TodaysKeys.RISING_GARBAGE, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Three Minutes Records", 
                Database.TodaysKeys.THREE_MINUTES, recordFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Invisible Records", 
                Database.TodaysKeys.INVISIBLE, recordFormatter));
        
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Garbage Heap Records", 
                Database.TodaysKeys.GARBAGE_HEAP, extendedRecordHeightFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Forty Lines Records", 
                Database.TodaysKeys.FORTY_LINES, extendedRecordHeightFormatter));
        recordsDescriptors.add(new RecordsDescriptor("Today's Best Vs. AI Records", 
                Database.TodaysKeys.VS_AI, extendedRecordDifficultyFormatter));
    }
    
    private void initPsas() {
        psas.clear();
        for (final Psa psa : Psa.values()) {
            psas.add(psa);
        }
    }
    
    public void reset() {        
        titleScreenState.reset();
        mode = Mode.TITLE_SCREEN;
    }
    
    public void update() {
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
        return gameState.getStates()[index].getLostTimer() > 110;
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
    
    private void startPsa() {
        choosePsa();
        timer = FRAMES_PER_PSA;
        mode = Mode.PSA;
    }

    private void chooseDemoMode() {
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
        recordsState.init(recordsDescriptor.getTitle(), database.get(recordsDescriptor.getKey()), 
                recordsDescriptor.getFormatter());
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