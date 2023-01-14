package textrads;

public final class Tetrominoes {
    
    public static final int T_TYPE = 0;
    public static final int J_TYPE = 1;
    public static final int Z_TYPE = 2;
    public static final int O_TYPE = 3;
    public static final int S_TYPE = 4;
    public static final int L_TYPE = 5;
    public static final int I_TYPE = 6;
       
    public static final int[][] TD = { { 0, 0 }, { 0,  1 }, { -1, 0 }, { 1, 0 } };
    public static final int[][] TL = { { 0, 0 }, { 0, -1 }, { -1, 0 }, { 0, 1 } };
    public static final int[][] TU = { { 0, 0 }, { 0, -1 }, { -1, 0 }, { 1, 0 } };
    public static final int[][] TR = { { 0, 0 }, { 0, -1 }, {  1, 0 }, { 0, 1 } };
    
    public static final int[][] JD = { { 0, 0 }, {  1,  1 }, { -1,  0 }, { 1, 0 } };
    public static final int[][] JL = { { 0, 0 }, {  0, -1 }, { -1,  1 }, { 0, 1 } };
    public static final int[][] JU = { { 0, 0 }, { -1, -1 }, { -1,  0 }, { 1, 0 } };
    public static final int[][] JR = { { 0, 0 }, {  0, -1 }, {  1, -1 }, { 0, 1 } };
    
    public static final int[][] ZD = { { 0, 0 }, { 1,  1 }, { -1,  0 }, {  0, 1 } };
    public static final int[][] ZL = { { 0, 0 }, { 0, -1 }, { -1,  0 }, { -1, 1 } };
    public static final int[][] ZU = { { 0, 0 }, { 0, -1 }, { -1, -1 }, {  1, 0 } };
    public static final int[][] ZR = { { 0, 0 }, { 1, -1 }, {  1,  0 }, {  0, 1 } };
    
    public static final int[][] OD = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    public static final int[][] OL = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    public static final int[][] OU = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    public static final int[][] OR = { { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } };
    
    public static final int[][] SD = { { 0, 0 }, {  1,  0 }, { -1, 1 }, { 0,  1 } };
    public static final int[][] SL = { { 0, 0 }, { -1, -1 }, { -1, 0 }, { 0,  1 } };
    public static final int[][] SU = { { 0, 0 }, {  0, -1 }, { -1, 0 }, { 1, -1 } };
    public static final int[][] SR = { { 0, 0 }, {  0, -1 }, {  1, 1 }, { 1,  0 } };
    
    public static final int[][] LD = { { 0, 0 }, { -1,  1 }, { -1,  0 }, { 1, 0 } };
    public static final int[][] LL = { { 0, 0 }, {  0, -1 }, { -1, -1 }, { 0, 1 } } ;
    public static final int[][] LU = { { 0, 0 }, {  1, -1 }, { -1,  0 }, { 1, 0 } };
    public static final int[][] LR = { { 0, 0 }, {  0, -1 }, {  0,  1 }, { 1, 1 } };
    
    public static final int[][] ID = { { 0,  0 }, { -1,  0 }, { 1,  0 }, { 2,  0 } };
    public static final int[][] IL = { { 0,  0 }, {  0, -2 }, { 0, -1 }, { 0,  1 } };
    public static final int[][] IU = { { 0, -1 }, { -1, -1 }, { 1, -1 }, { 2, -1 } };
    public static final int[][] IR = { { 1,  0 }, {  1, -2 }, { 1, -1 }, { 1,  1 } };

    public static final int[][][] T = { TD, TL, TU, TR };  
    public static final int[][][] J = { JD, JL, JU, JR };
    public static final int[][][] Z = { ZD, ZL, ZU, ZR };
    public static final int[][][] O = { OD, OL, OU, OR };
    public static final int[][][] S = { SD, SL, SU, SR };
    public static final int[][][] L = { LD, LL, LU, LR };
    public static final int[][][] I = { ID, IL, IU, IR };
    
    public static final int[][][][] TETROMINOES = { T, J, Z, O, S, L, I };
    
    public static final int[][] CW = { { 1, 1 }, { -1, 1 }, { -1, -1 }, { 1, -1 } };
    public static final int[][] CCW = { { -1, 1 }, { -1, -1 }, { 1, -1 }, { 1, 1 } };
    
    private Tetrominoes() {        
    }
}
