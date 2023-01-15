package textrads;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import java.util.List;

public class DefaultMonoGameRenderer implements MonoGameRenderer {

    public static final TextColor EMPTY_COLOR = new TextColor.Indexed(0);
    public static final TextColor LINE_COLOR = new TextColor.Indexed(15);
    public static final TextColor FLASH_COLOR = new TextColor.Indexed(15);
    public static final TextColor ATTACK_COLOR = new TextColor.Indexed(160);
    
    public static final TextColor T_COLOR = new TextColor.Indexed(133);
    public static final TextColor J_COLOR = new TextColor.Indexed(61);
    public static final TextColor Z_COLOR = new TextColor.Indexed(167);
    public static final TextColor O_COLOR = new TextColor.Indexed(179);
    public static final TextColor S_COLOR = new TextColor.Indexed(149);
    public static final TextColor L_COLOR = new TextColor.Indexed(173);
    public static final TextColor I_COLOR = new TextColor.Indexed(36);
    
    public static final TextColor GARBAGE_COLOR = new TextColor.Indexed(238);
    
    public static final TextColor[] BLOCK_COLORS = {
        EMPTY_COLOR,   // 0
        T_COLOR,       // 1
        J_COLOR,       // 2
        Z_COLOR,       // 3
        O_COLOR,       // 4
        S_COLOR,       // 5
        L_COLOR,       // 6
        I_COLOR,       // 7
        GARBAGE_COLOR, // 8
    };    

    @Override
    public Dimensions getDimensions(final boolean big, final boolean attackBar) {
        return null; // TODO
    }

    @Override
    public void render(final TextGraphics g, final MonoGameState state, final int x, final int y, final boolean big, 
            final boolean attackBar) {
        
        if (big) {
            renderBig(g, state, x, y, attackBar);
        } else {
            renderSmall(g, state, x, y, attackBar);
        }
    }    
    
    private void renderSmall(final TextGraphics g, final MonoGameState state, final int x, final int y, 
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
                
        final int[][] playfield = state.getPlayfield();
        for (int i = MonoGameState.PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
            for (int j = MonoGameState.PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                g.setBackgroundColor(BLOCK_COLORS[playfield[i][j]]);
                g.putString(ox + 1 + 2 * j, y + 1 + i, "  ");
            }
        }
        
        final List<Integer> nexts = state.getNexts();
        for (int i = 0; i < 5; ++i) {
            final int type = nexts.get(i);
            drawSmallTetromino(g, ox + ((type == Tetrominoes.I_TYPE || type == Tetrominoes.O_TYPE) ? 25 : 26), 
                    y + 1 + 3 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case TETROMINO_FALLING:
                drawSmallTetromino(g, ox + 1 + 2 * state.getTetrominoX(), y + 1 + state.getTetrominoY(), 
                        state.getTetrominoType(), state.getTetrominoRotation(),
                        state.getLockTimer() < 2 ? FLASH_COLOR : BLOCK_COLORS[state.getTetrominoType() + 1]);                
                break;
            case CLEARING_LINES: {
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
        }
    }
    
    private void renderBig(final TextGraphics g, final MonoGameState state, final int x, final int y, 
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
                
        final int[][] playfield = state.getPlayfield();
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
            drawBigTetromino(g, ox + ((type == Tetrominoes.I_TYPE || type == Tetrominoes.O_TYPE) ? 47 : 49), 
                    y + 1 + 6 * i, type, 0);
        }
        
        switch (state.getMode()) {
            case TETROMINO_FALLING:
                drawBigTetromino(g, ox + 1 + 4 * state.getTetrominoX(), y + 1 + 2 * state.getTetrominoY(), 
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
        }
    }  
    
    private void drawBigTetromino(final TextGraphics g, final int x, final int y, final int type, final int rotation) {
        drawBigTetromino(g, x, y, type, rotation, BLOCK_COLORS[type + 1]);
    }
    
    private void drawBigTetromino(final TextGraphics g, final int x, final int y, final int type, final int rotation, 
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
    
    private void drawSmallTetromino(final TextGraphics g, final int x, final int y, final int type, 
            final int rotation) {
        drawSmallTetromino(g, x, y, type, rotation, BLOCK_COLORS[type + 1]);
    }
    
    private void drawSmallTetromino(final TextGraphics g, final int x, final int y, final int type, 
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
