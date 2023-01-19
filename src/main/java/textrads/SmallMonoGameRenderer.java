package textrads;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;
import textrads.util.RenderUtil;

public class SmallMonoGameRenderer extends MonoGameRenderer {
    
    private final Dimensions DIMENSIONS_WITH_ATTACK_BAR = new Dimensions(35, 22);
    private final Dimensions DIMENSIONS_WITHOUT_ATTACK_BAR = new Dimensions(33, 22);
    
    @Override
    public Dimensions getDimensions(final boolean attackBar) {
        return attackBar ? DIMENSIONS_WITH_ATTACK_BAR : DIMENSIONS_WITHOUT_ATTACK_BAR;
    }

    @Override
    public void render(final TextGraphics g, final MonoGameState state, final int x, final int y, 
            final boolean attackBar) {
        
        g.setBackgroundColor(EMPTY_COLOR);
        g.setForegroundColor(LINE_COLOR);
        
        int ox = x;
        if (attackBar) {            
            for (int i = 1; i < 21; ++i) {
                g.putString(ox, y + i, "\u2503");            
            }
            g.putString(ox, y, "\u257B");
            g.putString(ox, y + 21, "\u2517");
            g.putString(ox + 1, y + 21, "\u2501");
            
            g.setBackgroundColor(ATTACK_COLOR);
            for (int i = state.getAttackRows() - 1; i >= 0; --i) {
                g.putString(ox + 1, y + 20 - i, " ");
            }
            
            g.setBackgroundColor(EMPTY_COLOR);
            ox += 2;
        }
        
        for (int i = 1; i < 21; ++i) {
            g.putString(ox, y + i, "\u2503");            
        }
        for (int i = 0; i < 20; ++i) {
            g.putString(ox + i + 1, y + 21, "\u2501");
        }
        for (int i = 1; i < 15; ++i) {
            g.putString(ox + 21, y + i, "\u2503");
            g.putString(ox + 32, y + i, "\u2503");
        }
        for (int i = 16; i < 21; ++i) {
            g.putString(ox + 21, y + i, "\u2503");
        }
        for (int i = 21; i < 32; ++i) {
            g.putString(ox + i, y, "\u2501");
            g.putString(ox + i, y + 15, "\u2501");
        }
        g.putString(ox, y, "\u257B");
        g.putString(ox, y + 21, attackBar ? "\u253B" : "\u2517");
        g.putString(ox + 21, y + 21, "\u251B");
        g.putString(ox + 21, y, "\u250F");
        g.putString(ox + 32, y, "\u2513");
        g.putString(ox + 21, y + 15, "\u2523");
        g.putString(ox + 32, y + 15, "\u251B");
        
        g.setForegroundColor(LABEL_COLOR);
        g.putString(ox + 22, y + 16, "Score");
        g.putString(ox + 22, y + 17, "Time");
        g.putString(ox + 22, y + 18, "Level");
        g.putString(ox + 22, y + 19, "Lines");
        g.putString(ox + 22, y + 20, "Wins");

        g.setForegroundColor(VALUE_COLOR);
        RenderUtil.putIntRight(g, ox + 31, y + 16, state.getScore());
        RenderUtil.putIntRight(g, ox + 31, y + 18, state.getLevel());
        RenderUtil.putIntRight(g, ox + 31, y + 19, state.getLines());
        
        final byte[][] playfield = state.getPlayfield();
        for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(ox + 1 + 2 * j, y + 1 + i, "  ");
            }
        }
        
        final List<Byte> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final byte type = nexts.get(i);
            drawTetromino(g, ox + ((type == Tetrominoes.I_TYPE || type == Tetrominoes.O_TYPE) ? 25 : 26), 
                    y + 1 + 3 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case MonoGameState.TETROMINO_FALLING_MODE:
                drawTetromino(g, ox + 1 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case MonoGameState.CLEARING_LINES_MODE: {
                final int timer = state.getLineClearTimer();
                if (timer >= 36 || timer >= 6 && timer <= 20) {
                    g.setBackgroundColor(FLASH_COLOR);
                    for (final int lineY : state.getLineYs()) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                            g.putString(ox + 1 + 2 * j, y + lineY + 1, "  ");
                        }
                    }
                }
                break;
            }
            case MonoGameState.GAME_OVER_MODE: {
                final int t = state.getGameOverTimer();
                if (t < 15 || (t >= 30 && t < 45) || (t >= 60 && t < 75)) {
                    drawTetromino(g, ox + 1 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                            state.getTetrominoType(), state.getTetrominoRotation(),
                            BLOCK_COLORS[state.getTetrominoType() + 1]);
                } else if (t >= 90) {
                    g.setBackgroundColor(GAME_OVER_COLOR);
                    final int start = Math.max(0, MonoGameState.PLAYFIELD_HEIGHT - (t - 89));
                    for (int i = start; i < MonoGameState.PLAYFIELD_HEIGHT; ++i) {
                        for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {                            
                            g.putString(ox + 1 + 2 * j, y + 1 + i, "  ");
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
        final int[][] blocks = Tetrominoes.TETROMINOES[type][rotation];
        for (int i = blocks.length - 1; i >= 0; --i) {
            final int[] coordinates = blocks[i];
            final int bx = x + 2 * coordinates[0];
            final int by = y + coordinates[1];
            g.putString(bx, by, "  ");            
        }
    }    
}
