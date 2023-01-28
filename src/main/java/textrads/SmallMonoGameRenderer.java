package textrads;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.util.RenderUtil;

public class SmallMonoGameRenderer extends MonoGameRenderer {
    
    private final Dimensions DIMENSIONS = new Dimensions(35, 22);
    
    @Override
    public Dimensions getDimensions() {
        return DIMENSIONS;
    }

    @Override
    public void render(final TextGraphics g, final MonoGameState state, final int x, final int y, 
            final boolean showWins) {
        
        g.setBackgroundColor(EMPTY_COLOR);
        g.setForegroundColor(LINE_COLOR);
                    
        for (int i = 1; i < 21; ++i) {
            g.setCharacter(x, y + i, Symbols.SINGLE_LINE_VERTICAL);            
        }
        g.setCharacter(x, y, Symbols.SINGLE_LINE_TOP_LEFT_CORNER); 
        g.setCharacter(x + 1, y, Symbols.SINGLE_LINE_HORIZONTAL);
        g.setCharacter(x, y + 21, Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER);
        g.setCharacter(x + 1, y + 21, Symbols.SINGLE_LINE_HORIZONTAL);

        g.setBackgroundColor(ATTACK_COLOR);
        for (int i = state.getAttackRows() - 1; i >= 0; --i) {
            g.setCharacter(x + 1, y + 20 - i, ' ');
        }

        g.setBackgroundColor(EMPTY_COLOR);
        
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
        
        g.setForegroundColor(LABEL_COLOR);
        g.putString(x + 24, y + 16, "Score");
        g.putString(x + 24, y + 17, "Time");
        g.putString(x + 24, y + 18, "Level");
        g.putString(x + 24, y + 19, "Lines");
        if (showWins) {
            g.putString(x + 24, y + 20, "Wins");
        }

        g.setForegroundColor(VALUE_COLOR);
        RenderUtil.putIntRight(g, x + 33, y + 16, state.getScore());
        RenderUtil.putStringRight(g, x + 33, y + 17, RenderUtil.formatTime(state.getGameState().getUpdates()));
        RenderUtil.putIntRight(g, x + 33, y + 18, state.getLevel());
        RenderUtil.putIntRight(g, x + 33, y + 19, state.getLines());
        if (showWins) {
            RenderUtil.putIntRight(g, x + 33, y + 20, state.getWins());
        }
        
        final byte[][] playfield = state.getPlayfield();
        for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(x + 3 + 2 * j, y + 1 + i, "  ");
            }
        }
        
        final List<Byte> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final byte type = nexts.get(i);
            drawTetromino(g, x + ((type == Tetromino.I_TYPE || type == Tetromino.O_TYPE) ? 27 : 28), 
                    y + 1 + 3 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case MonoGameState.TETROMINO_FALLING_MODE:
                drawTetromino(g, x + 3 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case MonoGameState.CLEARING_LINES_MODE: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    g.setBackgroundColor(FLASH_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                            g.putString(x + 3 + 2 * j, y + lineY + 1, "  ");
                        }
                    }
                }
                break;
            }
            case MonoGameState.GAME_OVER_MODE: {
                final int t = state.getGameOverTimer();
                if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                    drawTetromino(g, x + 3 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                            state.getTetrominoType(), state.getTetrominoRotation(),
                            BLOCK_COLORS[state.getTetrominoType() + 1]);
                } else if (t >= 90) {
                    g.setBackgroundColor(GAME_OVER_COLOR);
                    final int start = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89));
                    for (int i = start; i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {                            
                            g.putString(x + 3 + 2 * j, y + 1 + i, "  ");
                        }
                    }
                }
                break;
            }            
        }
    }

    private void drawTetromino(final TextGraphics g, final int x, final int y, final int type, 
            final int rotation) {
        drawTetromino(g, x, y, type, rotation, BLOCK_COLORS[type + 1]);
    }
    
    private void drawTetromino(final TextGraphics g, final int x, final int y, final int type, 
            final int rotation, final TextColor color) {
        g.setBackgroundColor(color);
        for (final Offset offset : Tetromino.TETROMINOES[type][rotation].offsets) {
            final int bx = x + 2 * offset.x;
            final int by = y + offset.y;
            g.putString(bx, by, "  ");            
        }
    }    
}
