package textrads;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;

public final class Tetromino {

    public static final int T_TYPE = 0;
    public static final int J_TYPE = 1;
    public static final int Z_TYPE = 2;
    public static final int O_TYPE = 3;
    public static final int S_TYPE = 4;
    public static final int L_TYPE = 5;
    public static final int I_TYPE = 6;

    private static final int[][] TD = { { 0, 0 }, { 0,  1 }, { -1, 0 }, { 1, 0 } };
    private static final int[][] TL = { { 0, 0 }, { 0, -1 }, { -1, 0 }, { 0, 1 } };
    private static final int[][] TU = { { 0, 0 }, { 0, -1 }, { -1, 0 }, { 1, 0 } };
    private static final int[][] TR = { { 0, 0 }, { 0, -1 }, {  1, 0 }, { 0, 1 } };
    
    private static final int[][] JD = { { 0, 0 }, {  1,  1 }, { -1,  0 }, { 1, 0 } };
    private static final int[][] JL = { { 0, 0 }, {  0, -1 }, { -1,  1 }, { 0, 1 } };
    private static final int[][] JU = { { 0, 0 }, { -1, -1 }, { -1,  0 }, { 1, 0 } };
    private static final int[][] JR = { { 0, 0 }, {  0, -1 }, {  1, -1 }, { 0, 1 } };
    
    private static final int[][] ZD = { { 0, 0 }, { 1,  1 }, { -1,  0 }, {  0, 1 } };
    private static final int[][] ZL = { { 0, 0 }, { 0, -1 }, { -1,  0 }, { -1, 1 } };
    private static final int[][] ZU = { { 0, 0 }, { 0, -1 }, { -1, -1 }, {  1, 0 } };
    private static final int[][] ZR = { { 0, 0 }, { 1, -1 }, {  1,  0 }, {  0, 1 } };
    
    private static final int[][] OD = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    private static final int[][] OL = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    private static final int[][] OU = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    private static final int[][] OR = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    
    private static final int[][] SD = { { 0, 0 }, {  1,  0 }, { -1, 1 }, { 0,  1 } };
    private static final int[][] SL = { { 0, 0 }, { -1, -1 }, { -1, 0 }, { 0,  1 } };
    private static final int[][] SU = { { 0, 0 }, {  0, -1 }, { -1, 0 }, { 1, -1 } };
    private static final int[][] SR = { { 0, 0 }, {  0, -1 }, {  1, 1 }, { 1,  0 } };
    
    private static final int[][] LD = { { 0, 0 }, { -1,  1 }, { -1,  0 }, { 1, 0 } };
    private static final int[][] LL = { { 0, 0 }, {  0, -1 }, { -1, -1 }, { 0, 1 } } ;
    private static final int[][] LU = { { 0, 0 }, {  1, -1 }, { -1,  0 }, { 1, 0 } };
    private static final int[][] LR = { { 0, 0 }, {  0, -1 }, {  0,  1 }, { 1, 1 } };
    
    private static final int[][] ID = { { 0,  0 }, { -1,  0 }, { 1,  0 }, { 2,  0 } };
    private static final int[][] IL = { { 0,  0 }, {  0, -2 }, { 0, -1 }, { 0,  1 } };
    private static final int[][] IU = { { 0, -1 }, { -1, -1 }, { 1, -1 }, { 2, -1 } };
    private static final int[][] IR = { { 1,  0 }, {  1, -2 }, { 1, -1 }, { 1,  1 } };

    private static final int[][][] T = { TD, TL, TU, TR };  
    private static final int[][][] J = { JD, JL, JU, JR };
    private static final int[][][] Z = { ZD, ZL, ZU, ZR };
    private static final int[][][] O = { OD, OL, OU, OR };
    private static final int[][][] S = { SD, SL, SU, SR };
    private static final int[][][] L = { LD, LL, LU, LR };
    private static final int[][][] I = { ID, IL, IU, IR };
    
    private static final int[][][][] BLOCK_OFFSETS = { T, J, Z, O, S, L, I };
    
    private static final int[][] CW_OFFSETS = { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
    private static final int[][] CCW_OFFSETS = { { -1, 1 }, { -1, -1 }, { 1, -1 }, { 1, 1 } };
    
    public static final Tetromino[][] TETROMINOES = new Tetromino[7][4];
    public static final Offset[] CW = new Offset[4];
    public static final Offset[] CCW = new Offset[4];
    
    static {
        for (int type = 0; type < 7; ++type) {
            for (int rotation = 0; rotation < 4; ++rotation) {
                TETROMINOES[type][rotation] = new Tetromino(BLOCK_OFFSETS[type][rotation]);
            }
        }
        for (int rotation = 0; rotation < 4; ++rotation) {
            CW[rotation] = new Offset(CW_OFFSETS[rotation]);
            CCW[rotation] = new Offset(CCW_OFFSETS[rotation]);
        }
    }
    
    public final Offset[] offsets = new Offset[4];    
    public final boolean[][] validPosition = new boolean[PLAYFIELD_HEIGHT + 4][PLAYFIELD_WIDTH + 4];
    public final int minOffsetY;
    public final int maxOffsetY;

    private Tetromino(final int[][] blocks) {
        
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (int i = blocks.length - 1; i >= 0; --i) {
            final Offset offset = offsets[i] = new Offset(blocks[i]);
            minY = min(minY, offset.y);
            maxY = max(maxY, offset.y);
        }
        this.minOffsetY = minY;
        this.maxOffsetY = maxY;
        
        for (int y = PLAYFIELD_HEIGHT + 1; y >= -2; --y) {
            for (int x = PLAYFIELD_WIDTH + 1; x >= -2; --x) {
                validPosition[y + 2][x + 2] = true;
                for (final Offset offset : offsets) {
                    final int X = offset.x + x;
                    final int Y = offset.y + y;
                    if (X < 0 || X >= PLAYFIELD_WIDTH || Y >= PLAYFIELD_HEIGHT) {
                        validPosition[y + 2][x + 2] = false;
                        break;
                    }
                }
            }
        }
    }

    public int getLockHeight(final int y) {
        return PLAYFIELD_HEIGHT - (y + maxOffsetY);
    }    
}
