package textrads;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;

public class BigMonoGameRenderer extends MonoGameRenderer {

    @Override
    public Dimensions getDimensions(final boolean attackBar) {
        return null; // TODO
    }

    @Override
    public void render(final TextGraphics g, final MonoGameState state, final int x, final int y, 
            final boolean attackBar) {
        
        g.setBackgroundColor(EMPTY_COLOR);
        g.setForegroundColor(LINE_COLOR);
        
        int ox = x;
        if (attackBar) {            
            for (int i = 1; i < 41; ++i) {
                g.putString(ox, y + i, "\u2503");            
            }
            g.putString(ox, y, "\u257B");
            g.putString(ox, y + 41, "\u2517");
            g.putString(ox + 1, y + 41, "\u2501");
            g.putString(ox + 2, y + 41, "\u2501");
            
            g.setBackgroundColor(ATTACK_COLOR);
            for (int i = state.getAttackRows() - 1; i >= 0; --i) {
                g.putString(ox + 1, y + 40 - 2 * i, "  ");
                g.putString(ox + 1, y + 39 - 2 * i, "  ");
            }
            
            g.setBackgroundColor(EMPTY_COLOR);
            ox += 3;
        }
        
        for (int i = 1; i < 41; ++i) {
            g.putString(ox, y + i, "\u2503");            
        }
        for (int i = 0; i < 40; ++i) {
            g.putString(ox + i + 1, y + 41, "\u2501");
        }
        for (int i = 1; i < 29; ++i) {
            g.putString(ox + 41, y + i, "\u2503");
            g.putString(ox + 60, y + i, "\u2503");
        }
        for (int i = 30; i < 42; ++i) {
            g.putString(ox + 41, y + i, "\u2503");
        }
        for (int i = 42; i < 60; ++i) {
            g.putString(ox + i, y, "\u2501");
            g.putString(ox + i, y + 29, "\u2501");
        }
        g.putString(ox, y, "\u257B");
        g.putString(ox, y + 41, attackBar ? "\u253B" : "\u2517");
        g.putString(ox + 41, y + 41, "\u251B");
        g.putString(ox + 41, y, "\u250F");
        g.putString(ox + 60, y, "\u2513");
        g.putString(ox + 41, y + 29, "\u2523");
        g.putString(ox + 60, y + 29, "\u251B");
                
        final byte[][] playfield = state.getPlayfield();
        for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(ox + 1 + 4 * j, y + 1 + 2 * i, "    ");
                g.putString(ox + 1 + 4 * j, y + 2 + 2 * i, "    ");
            }
        }
        
        final List<Integer> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final int type = nexts.get(i);
            drawTetromino(g, ox + ((type == Tetrominoes.I_TYPE || type == Tetrominoes.O_TYPE) ? 47 : 49), 
                    y + 1 + 6 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case TETROMINO_FALLING:
                drawTetromino(g, ox + 1 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case CLEARING_LINES: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    g.setBackgroundColor(FLASH_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                            g.putString(ox + 1 + 4 * j, y + 2 * lineY + 1, "    ");
                            g.putString(ox + 1 + 4 * j, y + 2 * lineY + 2, "    ");
                        }
                    }
                }
                break;
            }
            case GAME_OVER: {
                final int t = state.getGameOverTimer();
                if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                    drawTetromino(g, ox + 1 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
                            state.getTetrominoType(), state.getTetrominoRotation(),
                            BLOCK_COLORS[state.getTetrominoType() + 1]);                    
                } else if (t >= 90) {
                    g.setBackgroundColor(GAME_OVER_COLOR);
                    final int start = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89));
                    for (int i = start; i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {                            
                            g.putString(ox + 1 + 4 * j, y + 1 + 2 * i, "    ");
                            g.putString(ox + 1 + 4 * j, y + 2 + 2 * i, "    ");
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
        g.setBackgroundColor(color);
        final int[][] blocks = Tetrominoes.TETROMINOES[type][rotation];
        for (int i = blocks.length - 1; i >= 0; --i) {
            final int[] coordinates = blocks[i];
            final int bx = x + 4 * coordinates[0];
            final int by = y + 2 * coordinates[1];
            g.putString(bx, by, "    ");
            g.putString(bx, by + 1, "    ");
        }
    }    
}