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
import textrads.attractmode.AttractModeRenderer;
import textrads.attractmode.AttractModeState;
import textrads.db.Database;
import textrads.db.DatabaseSource;
import textrads.db.Preferences;
import textrads.ui.common.Images;
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
        CONTINUE,
        GIVE_UP,
        END,
    }
    
    private final Database database = DatabaseSource.getDatabase();
    private final InputEventList eventList = new InputEventList();

    private final Ai ai = AiSource.getAis()[1];
    private float moveTimer;
    private final List<Byte> moves = new ArrayList<>(1024);
    
    private final AttractModeState attractModeState = new AttractModeState();
    private final AttractModeRenderer attractModeRenderer = new AttractModeRenderer();
    
    private final Menu mainMenu = createMainMenu();
    private final MenuRenderer menuRenderer = new MenuRenderer();
    
    private final Question levelQuestion = new Question(new TextField("Level (0\u2500\u250029)?", 
            new NumberValidator(0, 29)));
    private final Question difficultyQuestion = new Question(new TextField("Difficulty (0\u2500\u250029)?", 
            new NumberValidator(0, 29))); 
    private final Question heightQuestion = new Question(new TextField("Height (1\u2500\u250012)?", 
            new NumberValidator(1, 12)));
    private final QuestionRenderer questionRenderer = new QuestionRenderer();
    
    private final CongratsScreenState congratsScreenState = new CongratsScreenState();
    private final CongratsScreenRenderer congratsScreenRenderer = new CongratsScreenRenderer();
    
    private final GameRenderer gameRenderer = new GameRenderer();
    
    private State state = State.ATTRACT;
    
    private byte gameMode;
    private byte level;
    private byte challenge;
    private float framesPerMove;
    private int selectionTimer;
    private int wins0;
    private int wins1;
        
    public void launch() throws Exception {
        
        InputEventSource.setKeyMap(database.get(Database.OtherKeys.KEY_MAP));

        try (final Terminal terminal = TerminalUtil.createTerminal();
                final Screen screen = new TerminalScreen(terminal)) {
            
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            InputSource.setScreen(screen);
            attractModeState.reset();
                        
                // TODO TESTING
                congratsScreenState.init("Testing", null);

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
    
    private void update() {
//        switch (state) {
//            case ATTRACT:
//                updateAttractMode();
//                break;
//            case MAIN_MENU:
//                updateMainMenu();
//                break;
//            case LEVEL_CONFIG:
//                updateLevelConfig();
//                break;
//            case HEIGHT_CONFIG:
//                updateHeightConfig();
//                break;
//            case DIFFICULTY_CONFIG:
//                updateDifficultyConfig();
//                break;
//            case PLAY:
//                updatePlay();
//                break;
//            case CONTINUE:
//                updateContinue();
//                break;
//        }

        congratsScreenState.update();
    }
    
    private void render(final TextGraphics g, final TerminalSize size) {
//        switch (state) {
//            case ATTRACT:
//                renderAttractMode(g, size);
//                break;
//            case MAIN_MENU:
//                renderMainMenu(g, size);
//                break;
//            case LEVEL_CONFIG:
//                renderLevelConfig(g, size);
//                break;
//            case HEIGHT_CONFIG:
//                renderHeightConfig(g, size);
//                break;
//            case DIFFICULTY_CONFIG:
//                renderDifficultyConfig(g, size);
//                break;
//            case PLAY:
//                renderPlay(g, size);
//                break;
//            case CONTINUE:
//                renderContinue(g, size);
//                break;
//        }

        congratsScreenRenderer.render(g, size, congratsScreenState);
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
                        gameMode = GameState.Mode.VS_HUMAN;
                        break;
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
            gameState.handleInputEvent(eventList.get(i), 0);
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
    }
    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
