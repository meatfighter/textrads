package textrads;

public class Tetrominoes {
    
    // T J Z O S L I

    public static final int[][][] T = {
        { { 0, 0 }, { -1, 0 }, { 0,  1 }, { 1, 0 }, }, // td
        { { 0, 0 }, { -1, 0 }, { 0, -1 }, { 0, 1 }, }, // tl
        { { 0, 0 }, { -1, 0 }, { 0, -1 }, { 1, 0 }, }, // tu
        { { 0, 0 }, {  1, 0 }, { 0, -1 }, { 0, 1 }, }, // tr
    };
}
