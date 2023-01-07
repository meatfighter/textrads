package textrads;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class Textrads {
    
    public void launch() throws Exception {
        try (final Screen screen = new TerminalScreen(new DefaultTerminalFactory().createTerminal())) {
            screen.startScreen();
            screen.setCursorPosition(null); // turn off cursor
            
            final TextGraphics g = screen.newTextGraphics();
            TerminalSize size = screen.getTerminalSize();
            
            while (true) {
                final TerminalSize s = screen.doResizeIfNecessary​();
                if (s != null) {
                    size = s;
                }
                final KeyStroke keyStroke = screen.pollInput();
                if (keyStroke != null) {
                    if (keyStroke.getKeyType() == KeyType.Escape) {
                        break;
                    } else if (keyStroke.getKeyType() == KeyType.Character) {
                        g.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
                        g.putString(size.getColumns() / 2, size.getRows() / 2, 
                                Character.toString(keyStroke.getCharacter()));
                    }
                }
                screen.refresh(); 
                Thread.sleep(15);
            }            
        } 
    }
    

    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
