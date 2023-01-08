package textrads;

public class Tetrominoes {
    
    // T J Z O S L I
    
    public static final int[][] TD = { { 0, 0 }, { -1, 0 }, { 0,  1 }, { 1, 0 } };
    public static final int[][] TL = { { 0, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 } };
    public static final int[][] TU = { { 0, 0 }, { -1, 0 }, { 0, -1 }, { 1, 0 } };
    public static final int[][] TR = { { 0, 0 }, {  1, 0 }, { 0, -1 }, { 0, 1 } };

    public static final int[][][] T = { TD, TL, TU, TR };    
}
