package textrads;

import java.io.PrintWriter;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import static org.jline.keymap.KeyMap.esc;
import static org.jline.keymap.KeyMap.key;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp.Capability;
import org.jline.utils.NonBlockingReader;

public class Textrads {
    
    enum RowOperation {
     QUIT, UP, DOWN, LEFT, RIGHT
   }

    public void launch() throws Exception {
        
        final Terminal term = TerminalBuilder.builder().system(true).build();
        final PrintWriter out = term.writer();
        final NonBlockingReader in = term.reader();
        
        term.enterRawMode();
        term.puts(Capability.clear_screen);        
        term.puts(Capability.cursor_invisible);
        
                        
        out.append(new AttributedStringBuilder().style(AttributedStyle.DEFAULT.background(AttributedStyle.GREEN))
                                .append("foo\nbar").toAnsi());

        for (int i = 0; i < 256; ++i) {
            out.format("%d %c%n", i, i);
        }

        out.flush();
        
        


        final KeyMap<RowOperation> keys = new KeyMap<>();
        keys.setAmbiguousTimeout(200); // make ESC quicker
//        keys.bind(RowOperation.QUIT, esc());
        keys.bind(RowOperation.UP, key(term, Capability.key_up));
        keys.bind(RowOperation.DOWN, key(term, Capability.key_down));
        keys.bind(RowOperation.LEFT, key(term, Capability.key_left));
        keys.bind(RowOperation.RIGHT, key(term, Capability.key_right));
        
        final BindingReader bindingReader = new BindingReader(in);
        while (true) {
           RowOperation key = bindingReader.readBinding(keys);
           out.format("key: %s%n", key);
           out.flush();
//           if (key == RowOperation.QUIT) {
//               break;
//           }
        }

        
//        term.puts(Capability.clear_screen);
//        term.puts(Capability.cursor_visible);
//        out.flush();
    }
    

    
    public static void main(final String... args) throws Exception {
        new Textrads().launch();
    }
}
