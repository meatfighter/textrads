package textrads.util;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import textrads.app.Terminator;

public final class TerminalUtil {
    
    private static final int[] ICON_SIZES = { 512, 256, 128, 64, 48, 32, 24, 22, 16 };
    
    public static Terminal createTerminal() throws IOException, InterruptedException, InvocationTargetException {
        
        final Terminal terminal = new DefaultTerminalFactory().createTerminal();
        if (!(terminal instanceof Frame)) {
            return terminal;
        }
        
        final List<Image> icons = new ArrayList<>();
        for (final int iconSize : ICON_SIZES) {
            icons.add(loadIcon(iconSize));
        }
        
        final Frame frame = (Frame) terminal;
        EventQueue.invokeAndWait(() -> {
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(final WindowEvent e) {
                    TerminalUtil.windowOpened(frame, icons);
                } 
                @Override
                public void windowClosed(final WindowEvent e) {
                    Terminator.setTerminate(true);
                }
            });
            windowOpened(frame, icons);
        });

        return terminal;
    }
    
    private static void windowOpened(final Frame frame, final List<Image> icons) {
        frame.setTitle("Textrads");
        frame.setIconImages(icons);
        frame.setLocationRelativeTo(null);
        frame.setExtendedState(frame.getExtendedState() | Frame.MAXIMIZED_BOTH);
    }

    private static Image loadIcon(final int iconSize) throws IOException {
        try (final InputStream is = TerminalUtil.class.getResourceAsStream(String.format("/icons/icon-%d.png", 
                    iconSize));
                final BufferedInputStream bis = new BufferedInputStream(is)) {
            return ImageIO.read(is);
        } 
    }
    
    private TerminalUtil() {        
    }
}
