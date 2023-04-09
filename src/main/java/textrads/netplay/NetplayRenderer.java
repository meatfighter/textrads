package textrads.netplay;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import textrads.ui.menu.ChooserRenderer;
import textrads.ui.menu.MenuRenderer;
import textrads.ui.message.MessageScreenRenderer;
import textrads.ui.question.QuestionRenderer;
import static textrads.netplay.NetplayState.State.CLIENT_CONFIG_HOST_ERROR;

public class NetplayRenderer {
    
    private final MenuRenderer menuRenderer = new MenuRenderer();
    private final ConnectScreenRenderer connectMenuRenderer = new ConnectScreenRenderer();
    private final ChooserRenderer chooserRenderer = new ChooserRenderer();
    private final QuestionRenderer questionRenderer = new QuestionRenderer();
    private final MessageScreenRenderer messageScreenRenderer = new MessageScreenRenderer();
    
    public void render(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        switch (state.getState()) {
            case PLAY_AS:
                renderPlayAs(g, size, state);
                break;
            case SERVER_CONFIG:
                renderServerConfig(g, size, state);
                break;
            case SERVER_CONFIG_HOST:
                renderServerConfigHost(g, size, state);
                break;
            case SERVER_CONFIG_PORT:
                renderServerConfigPort(g, size, state);
                break;
            case CLIENT_CONFIG:
                renderClientConfig(g, size, state);
                break;
            case CLIENT_CONFIG_HOST:
                renderClientConfigHost(g, size, state);
                break;
            case CLIENT_CONFIG_PORT:
                renderClientConfigPort(g, size, state);
                break;
            case CLIENT_CONFIG_HOST_ERROR:
                renderClientConfigHostError(g, size, state);
                break;
        }
    }
    
    private void renderPlayAs(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        menuRenderer.render(g, size, state.getPlayAsMenu());
    }
    
    private void renderServerConfig(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        connectMenuRenderer.render(g, size, state.getConnectMenuState());
    }
    
    private void renderServerConfigHost(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        chooserRenderer.render(g, size, state.getServerHostChooser());
    }
    
    private void renderServerConfigPort(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        questionRenderer.render(g, size, state.getPortQuestion());
    }
    
    private void renderClientConfig(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        connectMenuRenderer.render(g, size, state.getConnectMenuState());
    }
    
    private void renderClientConfigHost(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        questionRenderer.render(g, size, state.getClientHostQuestion());
    }
    
    private void renderClientConfigPort(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        questionRenderer.render(g, size, state.getPortQuestion());
    }
    
    private void renderClientConfigHostError(final TextGraphics g, final TerminalSize size, final NetplayState state) {
        messageScreenRenderer.render(g, size, state.getMessageScreen());
    }
}
