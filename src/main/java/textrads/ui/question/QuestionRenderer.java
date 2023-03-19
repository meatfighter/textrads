package textrads.ui.question;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.common.Colors;
import textrads.ui.menu.BackExitRenderer;
import textrads.util.GraphicsUtil;

public class QuestionRenderer {
    
    public static final TextColor BACKGROUND_COLOR = Colors.BLACK;
    private static final TextColor TITLE_COLOR = Colors.WHITE;
    
    private final TextFieldRenderer textFieldRenderer = new TextFieldRenderer();
    private final BackExitRenderer backExitRenderer = new BackExitRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final Question question) {
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
        g.fill(' ');
        
        final int oy = (size.getRows() - question.getHeight()) / 2;

        GraphicsUtil.setColor(g, BACKGROUND_COLOR, TITLE_COLOR);
        GraphicsUtil.centerString(g, size, oy, question.getTitle());
        
        textFieldRenderer.render(g, size, question.getTextField(), oy + 3);
        backExitRenderer.render(g, size, question.getBackExitState(), oy + question.getHeight());
    }
}
