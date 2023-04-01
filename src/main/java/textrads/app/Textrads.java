package textrads.app;

import textrads.input.InputEvent;
import textrads.input.InputEventList;
import textrads.input.InputSource;
import textrads.input.InputEventSource;
import textrads.game.GameRenderer;
import textrads.game.MonoGameState;
import textrads.game.GameStateSource;
import textrads.game.GameState;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.Terminal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import textrads.ai.Ai;
import textrads.ai.AiSource;
import textrads.attractmode.AbstractRecordFormatter;
import textrads.attractmode.AttractModeRenderer;
import textrads.attractmode.AttractModeState;
import textrads.attractmode.ExtendedRecordHeightFormatter;
import textrads.attractmode.RecordFormatter;
import textrads.attractmode.RecordsRenderer;
import textrads.attractmode.RecordsState;
import textrads.db.Database;
import textrads.db.DatabaseSource;
import textrads.db.ExtendedRecord;
import textrads.db.Preferences;
import textrads.db.Record;
import textrads.db.RecordList;
import textrads.netplay.NetplayRenderer;
import textrads.netplay.NetplayState;
import textrads.netplay.Server;
import textrads.ui.menu.ContinueExitState;
import textrads.ui.menu.MenuColumn;
import textrads.ui.menu.MenuItem;
import textrads.ui.menu.Menu;
import textrads.ui.menu.MenuRenderer;
import textrads.ui.question.CongratsScreenRenderer;
import textrads.ui.question.CongratsScreenState;
import textrads.ui.question.NumberValidator;
import textrads.ui.question.Question;
import textrads.ui.question.QuestionRenderer;
import textrads.ui.question.TextField;
import textrads.util.TerminalUtil;

public class Textrads {
    
    public static final int FRAMES_PER_SECOND = 60;
    
    private static final int MAX_FRAME_SKIPS = 3;
    private static final int MIN_SLEEP_MICROS = 1500;
    
    public static final long NANOS_PER_FRAME = Math.round(1.0E9 / FRAMES_PER_SECOND);
    private static final long MIN_SLEEP_NANOS = TimeUnit.MICROSECONDS.toNanos(MIN_SLEEP_MICROS);
    
    public static enum State {
        ATTRACT,
        MAIN_MENU,
        LEVEL_CONFIG,
        HEIGHT_CONFIG,
        DIFFICULTY_CONFIG,
        PLAY,
        GIVE_UP,
        CONTINUE,
        CONGRATS,
        RECORDS,
        NETPLAY,
    }
    
    private final Database database = DatabaseSource.getDatabase();
    private final InputEventList eventList = new InputEventList();

    private final Ai ai = AiSource.getAis()[1];
    private float moveTimer;
    private final List<Byte> moves = new ArrayList<>(1024);
    
    private final AttractModeState attractModeState = new AttractModeState();
    private final AttractModeRenderer attractModeRenderer = new AttractModeRenderer();
    
    private final Menu mainMenu = createMainMenu();
    private final Menu giveUpMenu = createGiveUpMenu();
    private final MenuRenderer menuRenderer = new MenuRenderer();
    
    private final Question levelQuestion = new Question(new TextField("Level (0\u2500\u250029)?", 
            new NumberValidator(0, 29)));
    private final Question difficultyQuestion = new Question(new TextField("Difficulty (0\u2500\u250029)?", 
            new NumberValidator(0, 29))); 
    private final Question heightQuestion = new Question(new TextField("Height (1\u2500\u250012)?", 
            new NumberValidator(1, 12)));
    private final QuestionRenderer questionRenderer = new QuestionRenderer();

    private final GameRenderer gameRenderer = new GameRenderer();
    
    private final CongratsScreenState congratsScreenState = new CongratsScreenState();
    private final CongratsScreenRenderer congratsScreenRenderer = new CongratsScreenRenderer();
    
    private final ContinueExitState continueExitState = new ContinueExitState();
    private final RecordsState recordsState = new RecordsState();
    private final RecordsRenderer recordsRenderer = new RecordsRenderer();
    private final RecordFormatter recordFormatter = new RecordFormatter();
    private final ExtendedRecordHeightFormatter extendedRecordHeightFormatter = new ExtendedRecordHeightFormatter();
    
    private final NetplayState netplayState = new NetplayState();
    private final NetplayRenderer netplayRenderer = new NetplayRenderer();
    
    private State state = State.ATTRACT;
    
    private byte gameMode;
    private byte level;
    private byte challenge;
    private float framesPerMove;
    private int selectionTimer;
    private int wins0;
    private int wins1;
    private String gameModeName;
    private String allTimesKey;
    private String todaysKey;
    private RecordList<? super Record> allTimesList;
    private RecordList<? super Record> todaysList;    
    private int allTimesIndex;
    private int todaysIndex;
    private AbstractRecordFormatter formatter;
        
    public void launch() throws Exception {
        
        InputEventSource.setKeyMap(database.get(Database.OtherKeys.KEY_MAP));

        try (final Terminal terminal = TerminalUtil.createTerminal();
                final Screen screen = new TerminalScreen(terminal)) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            InputSource.setScreen(screen);
            attractModeState.reset();
            
            recordsState.init("All Time Best Marathon Records", database.get(Database.AllTimeKeys.MARATHON, true), 
                    new RecordFormatter());

            final TextGraphics g = screen.newTextGraphics();
            TerminalSize size = screen.getTerminalSize();
            
            long updateTime = System.nanoTime();
            while (!Terminator.isTerminate()) {
            
                final TerminalSize ts = screen.doResizeIfNecessary​();
                if (ts != null) {
                    size = ts;
                }
                
                int updateFrames = 0;
                while (true) {
                    update();
                    updateTime += NANOS_PER_FRAME;
                    if (updateTime > System.nanoTime()) {
                        break;
                    }
                    if (++updateFrames > MAX_FRAME_SKIPS) {
                        updateTime = System.nanoTime() + NANOS_PER_FRAME;
                        break;
                    }
                }
                
                render(g, size);
                screen.refresh();                 
                
                final long remainingTime = updateTime - System.nanoTime();
                if (remainingTime >= MIN_SLEEP_NANOS) {
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(remainingTime));
                } else {
                    while (updateTime - System.nanoTime() > 0) {                        
                    }
                }
            }            
        } 
        
        System.exit(0);
    }
    
    private Menu createMainMenu() {
        final List<MenuItem> menuItems0 = new ArrayList<>();
        menuItems0.add(new MenuItem("Marathon"));
        menuItems0.add(new MenuItem("Constant Level"));
        menuItems0.add(new MenuItem());
        menuItems0.add(new MenuItem("Garbage Heap"));
        menuItems0.add(new MenuItem("Rising Garbage"));

        final List<MenuItem> menuItems1 = new ArrayList<>();
        menuItems1.add(new MenuItem("Three Minutes"));
        menuItems1.add(new MenuItem("Forty Lines"));
        menuItems1.add(new MenuItem());
        menuItems1.add(new MenuItem("No Rotation"));
        menuItems1.add(new MenuItem("Invisible"));

        final List<MenuItem> menuItems2 = new ArrayList<>();
        menuItems2.add(new MenuItem("Vs. AI", 'A'));
        menuItems2.add(new MenuItem("Vs. Human", 'H'));
        menuItems2.add(new MenuItem());
        menuItems2.add(new MenuItem());
        menuItems2.add(new MenuItem("Keymapping"));

        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems0));
        menuColumns.add(new MenuColumn(menuItems1));
        menuColumns.add(new MenuColumn(menuItems2));

        return new Menu(menuColumns, "Main");
    }
    
    private Menu createGiveUpMenu() {
        final List<MenuItem> menuItems0 = new ArrayList<>();
        menuItems0.add(new MenuItem("Yes"));
        
        final List<MenuItem> menuItems1 = new ArrayList<>();
        menuItems1.add(new MenuItem("No"));
        
        final List<MenuColumn> menuColumns = new ArrayList<>();
        menuColumns.add(new MenuColumn(menuItems0));
        menuColumns.add(new MenuColumn(menuItems1));
        
        return new Menu(menuColumns, "Give Up?", false);
    }
    
    private void update() {
        switch (state) {
            case ATTRACT:
                updateAttractMode();
                break;
            case MAIN_MENU:
                updateMainMenu();
                break;
            case LEVEL_CONFIG:
                updateLevelConfig();
                break;
            case HEIGHT_CONFIG:
                updateHeightConfig();
                break;
            case DIFFICULTY_CONFIG:
                updateDifficultyConfig();
                break;
            case PLAY:
                updatePlay();
                break;
            case GIVE_UP:
                updateGiveUp();
                break;
            case CONTINUE:
                updateContinue();
                break;
            case CONGRATS:
                updateCongrats();
                break;
            case RECORDS:
                updateRecords();
                break;
            case NETPLAY:
                updateNetplay();
                break;
        }
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
        switch (state) {
            case ATTRACT:
                renderAttractMode(g, size);
                break;
            case MAIN_MENU:
                renderMainMenu(g, size);
                break;
            case LEVEL_CONFIG:
                renderLevelConfig(g, size);
                break;
            case HEIGHT_CONFIG:
                renderHeightConfig(g, size);
                break;
            case DIFFICULTY_CONFIG:
                renderDifficultyConfig(g, size);
                break;
            case PLAY:
                renderPlay(g, size);
                break;
            case GIVE_UP:
                renderGiveUp(g, size);
                break;
            case CONTINUE:
                renderContinue(g, size);
                break;
            case CONGRATS:
                renderCongrats(g, size);
                break;
            case RECORDS:
                renderRecords(g, size);
                break;
            case NETPLAY:
                renderNetplay(g, size);
                break;
        }
    }
    
    private void updateAttractMode() {
        attractModeState.update();
        if (attractModeState.isEnterPressed()) {
            gotoMainMenu();
        }
    }
    
    private void renderAttractMode(final TextGraphics g, final TerminalSize size) {
        attractModeRenderer.render(g, size, attractModeState);
    }
    
    private void gotoMainMenu() {
        state = State.MAIN_MENU;
        mainMenu.reset();
    }
    
    private void updateMainMenu() {
        mainMenu.update();
        final KeyStroke selection = mainMenu.getSelection();
        if (selection == null) {
            return;
        }
        switch (selection.getKeyType()) {
            case Escape:
                state = State.ATTRACT;
                attractModeState.reset();
                break;
            case Character: {
                final Character c = selection.getCharacter();
                if (c == null) {
                    break;
                }
                switch (Character.toUpperCase(c)) {
                    case 'M':
                        gameMode = GameState.Mode.MARATHON;
                        break;
                    case 'C':
                        gameMode = GameState.Mode.CONSTANT_LEVEL;
                        break;
                    case 'G':
                        gameMode = GameState.Mode.GARBAGE_HEAP;
                        break;
                    case 'R':
                        gameMode = GameState.Mode.RISING_GARBAGE;
                        break;
                    case 'T':
                        gameMode = GameState.Mode.THREE_MINUTES;
                        break;
                    case 'F':
                        gameMode = GameState.Mode.FORTY_LINES;
                        break;
                    case 'N':
                        gameMode = GameState.Mode.NO_ROTATION;
                        break;
                    case 'I':
                        gameMode = GameState.Mode.INVISIBLE;
                        break;
                    case 'A':
                        gameMode = GameState.Mode.VS_AI;
                        break;
                    case 'H':
                        gotoNetplay();
                        return;
                }
                gotoLevelConfig();
                break;
            }
        }
    }
    
    private void renderMainMenu(final TextGraphics g, final TerminalSize size) {
        menuRenderer.render(g, size, mainMenu);
    }
    
    private void gotoLevelConfig() {
        state = State.LEVEL_CONFIG;
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        level = preferences.getLevel(gameMode);                
        levelQuestion.init(GameState.Mode.toString(gameMode), (level >= 0) ? Integer.toString(level) : "");
    }    
    
    private void updateLevelConfig() {
        levelQuestion.update();
        
        if (levelQuestion.isEscPressed()) {
            state = State.MAIN_MENU;
            mainMenu.reset();
            return;
        }
            
        if (!levelQuestion.isEnterPressed()) {
            return;
        }
        
        level = Byte.parseByte(levelQuestion.getValue());
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        database.saveAsync(Database.OtherKeys.PREFERENCES, preferences.setLevel(gameMode, level));
        
        switch (gameMode) {
            case GameState.Mode.GARBAGE_HEAP:
            case GameState.Mode.FORTY_LINES:
                gotoHeightConfig();
                break;
            case GameState.Mode.VS_AI:
                gotoDifficultyConfig();
                break;
            default:
                wins0 = wins1 = 0;
                gotoPlay();
                break;
        }                
    }
    
    private void renderLevelConfig(final TextGraphics g, final TerminalSize size) {
        questionRenderer.render(g, size, levelQuestion);
    }
    
    private void gotoHeightConfig() {
        state = State.HEIGHT_CONFIG;
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        challenge = preferences.getChallenge(gameMode);                
        heightQuestion.init(GameState.Mode.toString(gameMode), (challenge >= 0) ? Integer.toString(challenge) : "");
    }    
    
    private void updateHeightConfig() {
        heightQuestion.update();
        
        if (heightQuestion.isEscPressed()) {
            gotoLevelConfig();
            return;
        }
        
        if (!heightQuestion.isEnterPressed()) {
            return;
        }  
        
        challenge = Byte.parseByte(heightQuestion.getValue());
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        database.saveAsync(Database.OtherKeys.PREFERENCES, preferences.setChallenge(gameMode, challenge));
        wins0 = wins1 = 0;
        gotoPlay();
    }
    
    private void renderHeightConfig(final TextGraphics g, final TerminalSize size) {
        questionRenderer.render(g, size, heightQuestion);
    }
    
    private void gotoDifficultyConfig() {
        state = State.DIFFICULTY_CONFIG;
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        challenge = preferences.getChallenge(gameMode);                
        difficultyQuestion.init(GameState.Mode.toString(gameMode), 
                (challenge >= 0) ? Integer.toString(challenge) : "");
    }    
    
    private void updateDifficultyConfig() {
        difficultyQuestion.update();
        
        if (difficultyQuestion.isEscPressed()) {
            gotoLevelConfig();
            return;
        }
        
        if (!difficultyQuestion.isEnterPressed()) {
            return;
        }
        
        challenge = Byte.parseByte(difficultyQuestion.getValue());
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);
        database.saveAsync(Database.OtherKeys.PREFERENCES, preferences.setChallenge(gameMode, challenge));
        wins0 = wins1 = 0;
        gotoPlay();
    }
    
    private void renderDifficultyConfig(final TextGraphics g, final TerminalSize size) {
        questionRenderer.render(g, size, difficultyQuestion);
    } 
    
    private void gotoPlay() {
        state = State.PLAY;
        final GameState gameState = GameStateSource.getState();
        final long seed = ThreadLocalRandom.current().nextLong();
        gameState.init(gameMode, seed, level, (gameMode == GameState.Mode.GARBAGE_HEAP) ? challenge : 0, 
                (gameMode == GameState.Mode.FORTY_LINES) ? challenge : 0, false, wins0, wins1);
        if (gameMode == GameState.Mode.VS_AI) {
            ai.init(GameState.Mode.VS_AI, seed, level, 0, 0, challenge, true);
            framesPerMove = Ai.getFramesPerMove(challenge);
            moveTimer = Float.MAX_VALUE;
        }
    }
    
    private void updatePlay() {
        final GameState gameState = GameStateSource.getState();
        if (gameState.isContinueMessage()) {
            gotoContinue();
            return;
        }
            
        InputEventSource.poll(eventList);
        for (int i = 0, end = eventList.size(); i < end; ++i) {
            final byte event = eventList.get(i);
            if (event == InputEvent.GIVE_UP_PRESSED) {
                gotoGiveUp();
                return;
            }
            gameState.handleInputEvent(event, 0);
        }
        
        if (gameMode == GameState.Mode.VS_AI && !gameState.isPaused()) {
            final MonoGameState monoGameState = gameState.getStates()[1];
            
            if (monoGameState.isJustSpawned()) { 
                moveTimer = framesPerMove;
                ai.getMoves(moves, monoGameState.getLastAttackRows());
            } 
                        
            --moveTimer;            
            while (moveTimer <= 0) {
                moveTimer += framesPerMove;
                if (moves.isEmpty()) {
                    monoGameState.handleInputEvent(InputEvent.SOFT_DROP_PRESSED);
                } else {                    
                    monoGameState.handleInputEvent(moves.remove(0));
                }
            }
        }        
        
        gameState.update();
    }    
    
    private void renderPlay(final TextGraphics g, final TerminalSize size) {
        gameRenderer.render(g, size, GameStateSource.getState(), null);
    }
    
    private void gotoGiveUp() {
        state = State.GIVE_UP;
        giveUpMenu.reset();        
    }
    
    private void updateGiveUp() {
        giveUpMenu.update();
        final KeyStroke selection = giveUpMenu.getSelection();
        if (selection == null || selection.getKeyType() != KeyType.Character) {
            return;
        }
        final Character c = selection.getCharacter();
        if (c == null) {
            return;
        }
        switch (Character.toUpperCase(c)) {
            case 'Y':
                returnToMenu();
                break;
            case 'N':
                state = State.PLAY;
                break;
        }
    }
    
    private void returnToMenu() {
        switch (gameMode) {
            case GameState.Mode.GARBAGE_HEAP:
            case GameState.Mode.FORTY_LINES:
                gotoHeightConfig();
                break;
            case GameState.Mode.VS_AI:
                gotoDifficultyConfig();
                break;
            default:
                gotoLevelConfig();
                break;
        }
    }
    
    private void renderGiveUp(final TextGraphics g, final TerminalSize size) {
        menuRenderer.render(g, size, giveUpMenu);
    }

    private void gotoContinue() {
        state = State.CONTINUE;
        InputSource.clear();
        GameStateSource.getState().setSelection((byte) -1);
        selectionTimer = Menu.SELECTION_FRAMES;
    }
    
    private void updateContinue() {
        final GameState gameState = GameStateSource.getState();
        if (gameState.getSelection() >= 0) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            } else {
                handleContinue();
            }
            return;
        } 
        
        for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            } 
            if (keyStroke.getKeyType() == KeyType.Enter) {
                gameState.setSelection((byte) 0);
                break;
            }
        }
    }
    
    private void renderContinue(final TextGraphics g, final TerminalSize size) {
        gameRenderer.render(g, size, GameStateSource.getState(), null);
    }
    
    private void handleContinue() {
        
        final GameState gameState = GameStateSource.getState();
        
        if (gameMode == GameState.Mode.VS_AI || gameMode == GameState.Mode.VS_HUMAN) {
            final MonoGameState[] states = gameState.getStates();
            wins0 = states[0].getWins();
            wins1 = states[1].getWins();
            if (wins0 < 3 && wins1 < 3) {
                gotoPlay();
                return;
            }
            if (gameMode == GameState.Mode.VS_AI && wins0 < 3) {
                gotoDifficultyConfig();
            }
        }
        
        switch (gameMode) {
            case GameState.Mode.MARATHON:
                gameModeName = "Marathon";
                allTimesKey = Database.AllTimeKeys.MARATHON;
                todaysKey = Database.TodaysKeys.MARATHON;                
                break;
            case GameState.Mode.CONSTANT_LEVEL:
                gameModeName = "Constant Level";
                allTimesKey = Database.AllTimeKeys.CONSTANT_LEVEL;
                todaysKey = Database.TodaysKeys.CONSTANT_LEVEL;                
                break;
            case GameState.Mode.RISING_GARBAGE:
                gameModeName = "Rising Garbage";
                allTimesKey = Database.AllTimeKeys.RISING_GARBAGE;
                todaysKey = Database.TodaysKeys.RISING_GARBAGE;
                break;
            case GameState.Mode.THREE_MINUTES:
                gameModeName = "Three Minutes";
                allTimesKey = Database.AllTimeKeys.THREE_MINUTES;
                todaysKey = Database.TodaysKeys.THREE_MINUTES;
                break;
            case GameState.Mode.INVISIBLE:
                gameModeName = "Invisible";
                allTimesKey = Database.AllTimeKeys.INVISIBLE;
                todaysKey = Database.TodaysKeys.INVISIBLE;
                break;
            case GameState.Mode.GARBAGE_HEAP:
                gameModeName = "Garbage Heap";
                allTimesKey = Database.AllTimeKeys.GARBAGE_HEAP;
                todaysKey = Database.TodaysKeys.GARBAGE_HEAP;
                break;
            case GameState.Mode.FORTY_LINES:
                gameModeName = "Forty Lines";
                allTimesKey = Database.AllTimeKeys.FORTY_LINES;
                todaysKey = Database.TodaysKeys.FORTY_LINES;
                break;
            case GameState.Mode.VS_AI:
                gameModeName = "Vs. AI";
                allTimesKey = Database.AllTimeKeys.VS_AI;
                todaysKey = Database.TodaysKeys.VS_AI;
                break;
        }
        
        final MonoGameState monoGameState = gameState.getStates()[0];
        final Record record;
        switch (gameMode) {
            case GameState.Mode.GARBAGE_HEAP:
            case GameState.Mode.FORTY_LINES:
            case GameState.Mode.VS_AI:
                record = new ExtendedRecord("AAA", challenge, monoGameState.getLevel(), monoGameState.getUpdates(), 
                        monoGameState.getScore(), System.currentTimeMillis());
                formatter = extendedRecordHeightFormatter;
                break;
            default:
                record = new Record("AAA", monoGameState.getScore(), monoGameState.getLevel(), 
                        System.currentTimeMillis());
                formatter = recordFormatter;
                break;
        }
        
        allTimesList = database.get(allTimesKey, false);
        allTimesIndex = allTimesList.findIndex(record);
        
        todaysList = database.get(todaysKey, true);
        todaysIndex = todaysList.findIndex(record);
     
        if (allTimesIndex < RecordList.COUNT || todaysIndex < RecordList.COUNT) {
            gotoCongrats();
        } else {
            returnToMenu();
        }
    }
    
    private void gotoCongrats() {    
        
        final String prefix;
        final int index;
        if (allTimesIndex < RecordList.COUNT) {
            prefix = "the All Time";
            index = allTimesIndex;
        } else {
            prefix = "Today's";
            index = todaysIndex;
        }
        
        final String place;
        switch (index) {
            case 0:
                place = "1st";
                break;
            case 1:
                place = "2nd";
                break;
            case 2:
                place = "3rd";
                break;
            default:
                place = String.format("%dth", index + 1);
                break;
        }
        
        final Preferences preferences = database.get(Database.OtherKeys.PREFERENCES);        
        congratsScreenState.init(String.format("Congratulations! You got %s Best %s Place.", prefix, place), 
                preferences.getInitials());
        state = State.CONGRATS;
    }
    
    private void updateCongrats() {
        congratsScreenState.update();
        if (!congratsScreenState.isEnterPressed()) {
            return;
        }
        
        final MonoGameState monoGameState = GameStateSource.getState().getStates()[0];
        final Record record;
        switch (gameMode) {
            case GameState.Mode.GARBAGE_HEAP:
            case GameState.Mode.FORTY_LINES:
            case GameState.Mode.VS_AI:
                record = new ExtendedRecord(congratsScreenState.getInitials(), challenge, monoGameState.getLevel(), 
                        monoGameState.getUpdates(), monoGameState.getScore(), System.currentTimeMillis());
                break;
            default:
                record = new Record(congratsScreenState.getInitials(), monoGameState.getScore(), 
                        monoGameState.getLevel(), System.currentTimeMillis());
                break;
        }        
        
        if (allTimesIndex < RecordList.COUNT) {
            allTimesList = allTimesList.insert(allTimesIndex, record);
            database.saveAsync(allTimesKey, allTimesList);
        }
        if (todaysIndex < RecordList.COUNT) {
            todaysList = todaysList.insert(todaysIndex, record);
            database.saveAsync(todaysKey, todaysList);
        }
        
        gotoRecords();
    }
    
    private void renderCongrats(final TextGraphics g, final TerminalSize size) {
        congratsScreenRenderer.render(g, size, congratsScreenState);
    }
    
    private void gotoRecords() {
        final String prefix;
        final RecordList<? super Record> recordList;
        if (allTimesIndex < RecordList.COUNT) {
            prefix = "All Time";
            recordList = allTimesList;
        } else {
            prefix = "Today's";
            recordList = todaysList;
        }        
        recordsState.init(String.format("%s Best %s Records", prefix, gameModeName), recordList, formatter);
        
        state = State.RECORDS;
        continueExitState.setEnterSelected(false);
        selectionTimer = Menu.SELECTION_FRAMES;
        InputSource.clear();
    }
    
    private void updateRecords() {
        recordsState.update();
        
        if (continueExitState.isEnterSelected()) {
            InputSource.clear();
            if (selectionTimer > 0) {
                --selectionTimer;
            } else {
                returnToMenu();
            }
            return;
        } 
        
        for (int i = InputSource.MAX_POLLS - 1; i >= 0; --i) {
            final KeyStroke keyStroke = InputSource.poll();
            if (keyStroke == null) {
                break;
            } 
            if (keyStroke.getKeyType() == KeyType.Enter) {
                continueExitState.setEnterSelected(true);
                break;
            }
        }
    }
    
    private void renderRecords(final TextGraphics g, final TerminalSize size) {
        recordsRenderer.render(g, size, recordsState, continueExitState);
    }
    
    private void gotoNetplay() {
        state = State.NETPLAY;
        gameMode = GameState.Mode.VS_HUMAN;
        netplayState.reset();
    }
    
    private void updateNetplay() {
        netplayState.update();
        if (netplayState.isReturnToMainMenu()) {
            gotoMainMenu();
        }
    }
    
    private void renderNetplay(final TextGraphics g, final TerminalSize size) {
        netplayRenderer.render(g, size, netplayState);
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
