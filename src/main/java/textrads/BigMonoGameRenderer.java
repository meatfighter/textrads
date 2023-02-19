package textrads;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.util.GraphicsUtil;

public class BigMonoGameRenderer extends MonoGameRenderer {

    private final Dimensions DIMENSIONS = new Dimensions(64, 42);
    
    @Override
    public Dimensions getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public void render(final TextGraphics g, final TerminalSize size, final MonoGameState state, final int x, 
            final int y, final boolean showWins) {

        GraphicsUtil.setColor(g, ATTACK_COLOR, BACKGROUND_COLOR);
        for (int i = state.getAttackRows() - 1; i >= 0; --i) {
            g.putString(x + 1, y + 40 - 2 * i, "  ");
            g.putString(x + 1, y + 39 - 2 * i, "  ");
        }
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, LINE_COLOR);                  
        for (int i = 1; i < 41; ++i) {
            g.setCharacter(x, y + i, Symbols.SINGLE_LINE_VERTICAL);            
        }
        g.setCharacter(x, y, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
        g.putString(x + 1, y, "\u2500\u2500");
        g.setCharacter(x, y + 41, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
        g.putString(x + 1, y + 41, "\u2500\u2500");
       
        for (int i = 1; i < 41; ++i) {
            g.setCharacter(x + 3, y + i, Symbols.SINGLE_LINE_VERTICAL);            
        }
        for (int i = 0; i < 40; ++i) {
            g.setCharacter(x + i + 4, y + 41, Symbols.SINGLE_LINE_HORIZONTAL);
        }
        for (int i = 1; i < 29; ++i) {
            g.setCharacter(x + 44, y + i, Symbols.SINGLE_LINE_VERTICAL);
            g.setCharacter(x + 63, y + i, Symbols.SINGLE_LINE_VERTICAL);
        }
        for (int i = 30; i < 42; ++i) {
            g.setCharacter(x + 44, y + i, Symbols.SINGLE_LINE_VERTICAL);
        }
        for (int i = 42; i < 60; ++i) {
            g.setCharacter(x + i + 3, y, Symbols.SINGLE_LINE_HORIZONTAL);
            g.setCharacter(x + i + 3, y + 29, Symbols.SINGLE_LINE_HORIZONTAL);
        }
        g.setCharacter(x + 3, y, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        g.setCharacter(x + 3, y + 41, Symbols.SINGLE_LINE_T_UP);
        g.setCharacter(x + 44, y + 41, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
        g.setCharacter(x + 44, y, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
        g.setCharacter(x + 63, y, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        g.setCharacter(x + 44, y + 29, Symbols.SINGLE_LINE_T_RIGHT);
        g.setCharacter(x + 63, y + 29, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);      
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, LABEL_COLOR);
        final int oy = y + (showWins ? 31 : 32);
        g.putString(x + 46, oy, "Score");
        g.putString(x + 46, oy + 2, "Time");
        g.putString(x + 46, oy + 4, "Level");
        g.putString(x + 46, oy + 6, "Lines");
        if (showWins) {
            g.putString(x + 46, oy + 8, "Wins");
        }

        GraphicsUtil.setColor(g, BACKGROUND_COLOR, VALUE_COLOR);
        g.putString(x + 52, oy, Integer.toString(state.getScore()));
        g.putString(x + 51, oy + 2, GraphicsUtil.formatTime(state.getUpdates()));
        g.putString(x + 52, oy + 4, Integer.toString(state.getLevel()));
        g.putString(x + 52, oy + 6, Integer.toString(state.getLines()));
        if (showWins) {
            g.putString(x + 52, oy + 8, Integer.toString(state.getWins()));
        }        
                
        final byte[][] playfield = state.getPlayfield();
        for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                GraphicsUtil.setColor(g, BLOCK_COLORS[playfield[i][j]], BACKGROUND_COLOR);
                g.putString(x + 4 + 4 * j, y + 1 + 2 * i, "    ");
                g.putString(x + 4 + 4 * j, y + 2 + 2 * i, "    ");
            }
        }
        
        final List<Byte> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final int type = nexts.get(i);
            drawTetromino(g, x + ((type == Tetromino.I_TYPE || type == Tetromino.O_TYPE) ? 50 : 52), 
                    y + 1 + 6 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case MonoGameState.COUNTDOWN_MODE: {
                final int countdown = state.getCountdownValue();
                int X = x + 4;
                switch(countdown) {
                    case 0:
                        X += 2;
                        break;
                    case 1:
                        X += 18;
                        break;
                    default:
                        X += 14;
                        break;
                }
                BlockText.draw(countdown, g, X, y + 16, COUNTDOWN_COLOR, false);
                break;                
            }            
            case MonoGameState.TETROMINO_FALLING_MODE:
                drawTetromino(g, x + 4 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case MonoGameState.CLEARING_LINES_MODE: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    GraphicsUtil.setColor(g, FLASH_COLOR, BACKGROUND_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                            g.putString(x + 4 + 4 * j, y + 2 * lineY + 1, "    ");
                            g.putString(x + 4 + 4 * j, y + 2 * lineY + 2, "    ");
                        }
                    }
                }
                break;
            }
            case MonoGameState.GAME_OVER_MODE: {
                final int t = state.getGameOverTimer();
                if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                    drawTetromino(g, x + 4 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                            state.getTetrominoType(), state.getTetrominoRotation(),
                            BLOCK_COLORS[state.getTetrominoType() + 1]);                    
                } else if (t >= 90) {
                    GraphicsUtil.setColor(g, GAME_OVER_COLOR, BACKGROUND_COLOR);
                    final int start = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89));
                    for (int i = start; i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {                            
                            g.putString(x + 4 + 4 * j, y + 1 + 2 * i, "    ");
                            g.putString(x + 4 + 4 * j, y + 2 + 2 * i, "    ");
                        }
                    }
                }
                break;
            }
        }
    }    
    
    private void drawTetromino(final TextGraphics g, final int x, final int y, final int type, final int rotation) {
        drawTetromino(g, x, y, type, rotation, BLOCK_COLORS[type + 1]);
    }
    
    private void drawTetromino(final TextGraphics g, final int x, final int y, final int type, final int rotation, 
            final TextColor color) {
        
        GraphicsUtil.setColor(g, color, BACKGROUND_COLOR);
        for (final Offset offset : Tetromino.TETROMINOES[type][rotation].offsets) {
            final int bx = x + 4 * offset.x;
            final int by = y + 2 * offset.y;
            g.putString(bx, by, "    ");
            g.putString(bx, by + 1, "    ");
        }
    }    
}