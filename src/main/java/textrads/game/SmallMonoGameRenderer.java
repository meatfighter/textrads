package textrads.game;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.ui.common.BlockText;
import textrads.ui.common.Dimensions;
import textrads.ui.common.Offset;
import textrads.app.Tetromino;
import static textrads.game.MonoGameRenderer.BACKGROUND_COLOR;
import static textrads.game.MonoGameRenderer.END_TITLE_COLOR;
import static textrads.game.MonoGameRenderer.LINE_COLOR;
import textrads.ui.menu.MenuItemRenderer;
import textrads.util.GraphicsUtil;

public class SmallMonoGameRenderer extends MonoGameRenderer {
    
    private final Dimensions DIMENSIONS = new Dimensions(35, 22);
    
    @Override
    public Dimensions getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public void render(final TextGraphics g, final TerminalSize size, final MonoGameState state, final int x, 
            final int y, final boolean showWins) {

        GraphicsUtil.setColor(g, ATTACK_COLOR, BACKGROUND_COLOR);
        for (int i = state.getAttackRows() - 1; i >= 0; --i) {
            g.setCharacter(x + 1, y + 20 - i, ' ');
        }
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, LINE_COLOR);                    
        for (int i = 1; i < 21; ++i) {
            g.setCharacter(x, y + i, Symbols.SINGLE_LINE_VERTICAL);            
        }
        g.setCharacter(x, y, Symbols.SINGLE_LINE_TOP_LEFT_CORNER); 
        g.setCharacter(x + 1, y, Symbols.SINGLE_LINE_HORIZONTAL);
        g.setCharacter(x, y + 21, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
        g.setCharacter(x + 1, y + 21, Symbols.SINGLE_LINE_HORIZONTAL);

        for (int i = 1; i < 21; ++i) {
            g.setCharacter(x + 2, y + i, Symbols.SINGLE_LINE_VERTICAL);            
        }
        for (int i = 0; i < 20; ++i) {
            g.setCharacter(x + i + 3, y + 21, Symbols.SINGLE_LINE_HORIZONTAL);
        }
        for (int i = 1; i < 15; ++i) {
            g.setCharacter(x + 23, y + i, Symbols.SINGLE_LINE_VERTICAL);
            g.setCharacter(x + 34, y + i, Symbols.SINGLE_LINE_VERTICAL);
        }
        for (int i = 16; i < 21; ++i) {
            g.setCharacter(x + 23, y + i, Symbols.SINGLE_LINE_VERTICAL);
        }
        for (int i = 21; i < 32; ++i) {
            g.setCharacter(x + i + 2, y, Symbols.SINGLE_LINE_HORIZONTAL);
            g.setCharacter(x + i + 2, y + 15, Symbols.SINGLE_LINE_HORIZONTAL);
        }
        g.setCharacter(x + 2, y, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        g.setCharacter(x + 2, y + 21, Symbols.SINGLE_LINE_T_UP);
        g.setCharacter(x + 23, y + 21, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
        g.setCharacter(x + 23, y, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
        g.setCharacter(x + 34, y, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
        g.setCharacter(x + 23, y + 15, Symbols.SINGLE_LINE_T_RIGHT);
        g.setCharacter(x + 34, y + 15, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
        
        GraphicsUtil.setColor(g, BACKGROUND_COLOR, LABEL_COLOR);
        g.putString(x + 24, y + 16, "Score");
        g.putString(x + 24, y + 17, "Time");
        g.putString(x + 24, y + 18, "Level");
        g.putString(x + 24, y + 19, "Lines");
        if (showWins) {
            g.putString(x + 24, y + 20, "Wins");
        }

        GraphicsUtil.setColor(g, BACKGROUND_COLOR, VALUE_COLOR);
        GraphicsUtil.putIntRight(g, x + 33, y + 16, state.getScore());
        GraphicsUtil.putStringRight(g, x + 33, y + 17, GraphicsUtil.formatTime(state.getUpdates()));
        GraphicsUtil.putIntRight(g, x + 33, y + 18, state.getLevel());
        GraphicsUtil.putIntRight(g, x + 33, y + 19, state.getLines());
        if (showWins) {
            GraphicsUtil.putIntRight(g, x + 33, y + 20, state.getWins());
        }
        
        final byte[][] playfield = state.getPlayfield();
        final int floorHeight = state.getFloorHeight();
        if (floorHeight > 0) {
            GraphicsUtil.setColor(g, GARBAGE_COLOR, BACKGROUND_COLOR);
            for (int i = floorHeight - 1; i >= 0; --i) {
                g.putString(x + 3, y + MonoGameState.PLAYFIELD_HEIGHT - i, "                    ");
            }
        }
        if (state.getGameState().getMode() != GameState.Mode.INVISIBLE) {
            for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1 - floorHeight; i >= 0; --i) {
                final byte[] row = playfield[i];
                for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                    final byte block = row[j];
                    if (block != MonoGameState.EMPTY_BLOCK) {
                        GraphicsUtil.setColor(g, BLOCK_COLORS[block], BACKGROUND_COLOR);
                        g.putString(x + 3 + 2 * j, y + 1 + i, "  ");
                    }
                }
            }
        }
        
        final List<Byte> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final byte type = nexts.get(i);
            drawTetromino(g, x + ((type == Tetromino.I_TYPE || type == Tetromino.O_TYPE) ? 27 : 28), 
                    y + 1 + 3 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case MonoGameState.Mode.COUNTDOWN: {
                final int countdown = state.getCountdownValue();
                int X = x + 3;
                switch(countdown) {
                    case 0:
                        X += 1;
                        break;
                    case 1:
                        X += 9;
                        break;
                    default:
                        X += 7;
                        break;
                }
                BlockText.draw(countdown, g, X, y + 8, COUNTDOWN_COLOR, true);
                break;                
            }
            case MonoGameState.Mode.TETROMINO_FALLING:
                drawTetromino(g, x + 3 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case MonoGameState.Mode.CLEARING_LINES: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    GraphicsUtil.setColor(g, FLASH_COLOR, BACKGROUND_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        g.putString(x + 3, y + lineY + 1, "                    ");
                    }
                }
                break;
            }
            case MonoGameState.Mode.END: {
                final int t = state.getEndTimer();
                if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                    drawTetromino(g, x + 3 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                            state.getTetrominoType(), state.getTetrominoRotation(),
                            BLOCK_COLORS[state.getTetrominoType() + 1]);
                } else if (t >= 90) {
                    GraphicsUtil.setColor(g, LOST_COLOR, BACKGROUND_COLOR);
                    for (int i = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89)); 
                            i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                        g.putString(x + 3, y + 1 + i, "                    ");
                    }
                }
                if (t >= 110) {
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
                    for (int i = 7; i >= 0; --i) {
                        g.putString(x + 6, y + 7 + i, "              ");
                    }
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, LINE_COLOR);
                    g.setCharacter(x + 5, y + 6, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
                    g.setCharacter(x + 20, y + 6, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
                    g.setCharacter(x + 5, y + 15, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
                    g.setCharacter(x + 20, y + 15, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
                    for (int i = 13; i >= 0; --i) {
                        g.setCharacter(x + 6 + i, y + 6, Symbols.SINGLE_LINE_HORIZONTAL);
                        g.setCharacter(x + 6 + i, y + 15, Symbols.SINGLE_LINE_HORIZONTAL);
                    }
                    for (int i = 7; i >= 0; --i) {
                        g.setCharacter(x + 5, y + 7 + i, Symbols.SINGLE_LINE_VERTICAL);
                        g.setCharacter(x + 20, y + 7 + i, Symbols.SINGLE_LINE_VERTICAL);
                    }
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, END_TITLE_COLOR);
                    g.putString(x + 9, y + 8, "Game Over");
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
                    g.setCharacter(x + 8, y + 11, '[');
                    g.setCharacter(x + 14, y + 11, ']');
                    g.setCharacter(x + 7, y + 13, '[');
                    g.setCharacter(x + 14, y + 13, ']');
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
                    g.putString(x + 9, y + 11, "Enter");
                    g.putString(x + 8, y + 13, "Ctrl+C");
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, state.isContinueSelected() 
                            ? MenuItemRenderer.SELECTED_COLOR : MenuItemRenderer.DESCRIPTION_COLOR);
                    g.setCharacter(x + 16, y + 11, Symbols.ARROW_RIGHT);
                    g.setCharacter(x + 17, y + 13, 'X');
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
            final int bx = x + 2 * offset.x;
            final int by = y + offset.y;
            g.putString(bx, by, "  ");            
        }
    }    
}
