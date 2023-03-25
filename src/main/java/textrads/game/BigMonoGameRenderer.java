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
import textrads.ui.common.Colors;
import textrads.ui.menu.MenuItemRenderer;
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
        
        final GameState gameState = state.getGameState();
        final byte gameMode = gameState.getMode();

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
        final int floorHeight = state.getFloorHeight();
        if (floorHeight > 0) {
            GraphicsUtil.setColor(g, GARBAGE_COLOR, BACKGROUND_COLOR);
            for (int i = floorHeight - 1; i >= 0; --i) {
                g.putString(x + 4, y + 2 * (MonoGameState.PLAYFIELD_HEIGHT - i) - 1, 
                        "                                        ");
                g.putString(x + 4, y + 2 * (MonoGameState.PLAYFIELD_HEIGHT - i), 
                        "                                        ");
            }
        }
        if (gameMode != GameState.Mode.INVISIBLE) {
            for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1 - floorHeight; i >= 0; --i) {
                final byte[] row = playfield[i];
                for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                    final byte block = row[j];
                    if (block != MonoGameState.EMPTY_BLOCK) {
                        GraphicsUtil.setColor(g, BLOCK_COLORS[block], BACKGROUND_COLOR);
                        g.putString(x + 4 + 4 * j, y + 1 + 2 * i, "    ");
                        g.putString(x + 4 + 4 * j, y + 2 + 2 * i, "    ");
                    }
                }
            }
        }
        
        final List<Byte> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final int type = nexts.get(i);
            drawTetromino(g, x + ((type == Tetromino.I_TYPE || type == Tetromino.O_TYPE) ? 50 : 52), 
                    y + 1 + 6 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case MonoGameState.Mode.COUNTDOWN: {
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
            case MonoGameState.Mode.TETROMINO_FALLING:
                drawTetromino(g, x + 4 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case MonoGameState.Mode.CLEARING_LINES: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    GraphicsUtil.setColor(g, FLASH_COLOR, BACKGROUND_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        g.putString(x + 4, y + 2 * lineY + 1, "                                        ");
                        g.putString(x + 4, y + 2 * lineY + 2, "                                        ");
                    }
                }
                break;
            }
            case MonoGameState.Mode.END: {
                final int t = state.getEndTimer();
                if (!state.isWon()) {
                    if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                        drawTetromino(g, x + 4 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                                state.getTetrominoType(), state.getTetrominoRotation(),
                                BLOCK_COLORS[state.getTetrominoType() + 1]);                    
                    } else if (t >= 90) {
                        GraphicsUtil.setColor(g, LOST_COLOR, BACKGROUND_COLOR);
                        for (int i = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89)); 
                                i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                            g.putString(x + 4, y + 1 + 2 * i, "                                        ");
                            g.putString(x + 4, y + 2 + 2 * i, "                                        ");
                        }
                    }
                }
                if (gameMode == GameState.Mode.VS_AI && gameState.getIndex(state) != 0) {
                    break;
                }                
                if ((state.isWon() && gameMode != GameState.Mode.VS_AI) || t >= 110) {
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, BACKGROUND_COLOR);
                    for (int i = 7; i >= 0; --i) {
                        g.putString(x + 13, y + 17 + i, "                      ");
                    }
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, LINE_COLOR);
                    g.setCharacter(x + 12, y + 16, Symbols.SINGLE_LINE_TOP_LEFT_CORNER);
                    g.setCharacter(x + 35, y + 16, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER);
                    g.setCharacter(x + 12, y + 25, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
                    g.setCharacter(x + 35, y + 25, Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER);
                    for (int i = 21; i >= 0; --i) {
                        g.setCharacter(x + 13 + i, y + 16, Symbols.SINGLE_LINE_HORIZONTAL);
                        g.setCharacter(x + 13 + i, y + 25, Symbols.SINGLE_LINE_HORIZONTAL);
                    }
                    for (int i = 7; i >= 0; --i) {
                        g.setCharacter(x + 12, y + 17 + i, Symbols.SINGLE_LINE_VERTICAL);
                        g.setCharacter(x + 35, y + 17 + i, Symbols.SINGLE_LINE_VERTICAL);
                    }
                    
                    if (gameMode == GameState.Mode.VS_AI) {
                        if (state.isWon()) {
                            if (state.getWins() == 3) {
                                GraphicsUtil.setColor(g, BACKGROUND_COLOR, 
                                        ((gameState.getSuccessIndex() >> 3) & 1) == 0 ? Colors.WHITE : Colors.GOLD);
                                g.putString(x + 20, y + 18, "Success!");
                            } else {
                                GraphicsUtil.setColor(g, BACKGROUND_COLOR, END_TITLE_COLOR);
                                g.putString(x + 19, y + 18, "Round Won");
                            }
                        } else {
                            GraphicsUtil.setColor(g, BACKGROUND_COLOR, END_TITLE_COLOR);
                            if (state.getOpponent().getWins() == 3) {
                                g.putString(x + 19, y + 18, "Game Over");
                            } else {
                                g.putString(x + 19, y + 18, "Round Lost");
                            }                            
                        }
                    } else if (gameMode == GameState.Mode.THREE_MINUTES && state.isWon()) {
                        GraphicsUtil.setColor(g, BACKGROUND_COLOR, END_TITLE_COLOR);
                        g.putString(x + 20, y + 18, "Time Up");
                    } else if ((gameMode == GameState.Mode.FORTY_LINES || gameMode == GameState.Mode.GARBAGE_HEAP) 
                            && state.isWon()) {
                        GraphicsUtil.setColor(g, BACKGROUND_COLOR, 
                                ((gameState.getSuccessIndex() >> 3) & 1) == 0 ? Colors.WHITE : Colors.GOLD);
                        g.putString(x + 20, y + 18, "Success!");
                    } else {
                        GraphicsUtil.setColor(g, BACKGROUND_COLOR, END_TITLE_COLOR);
                        g.putString(x + 19, y + 18, "Game Over");
                    }                    
                    
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, MenuItemRenderer.BUTTON_COLOR);
                    g.setCharacter(x + 16, y + 21, '[');
                    g.setCharacter(x + 22, y + 21, ']');
                    g.setCharacter(x + 15, y + 23, '[');
                    g.setCharacter(x + 22, y + 23, ']');
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, MenuItemRenderer.ACCELERATOR_COLOR);
                    g.putString(x + 17, y + 21, "Enter");
                    g.putString(x + 16, y + 23, "Ctrl+C");
                    GraphicsUtil.setColor(g, BACKGROUND_COLOR, state.isContinueSelected() 
                            ? MenuItemRenderer.SELECTED_COLOR : MenuItemRenderer.DESCRIPTION_COLOR);
                    g.putString(x + 24, y + 21, "Continue");
                    g.putString(x + 24, y + 23, "Exit");
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