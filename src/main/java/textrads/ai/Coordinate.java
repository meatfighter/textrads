package textrads.ai;

import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;

public class Coordinate {

    // depth, y, x, orientation
    public static Coordinate[][][][] createMatrix() {
        final Coordinate[][][][] matrix 
                = new Coordinate[2][PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH][4];
        for (int k = 1; k >= 0; --k) {
            for (int y = PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
                for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                    for (int o = 3; o >= 0; --o) {
                        matrix[k][y][x][o] = new Coordinate(k, x, y, o);
                    }
                }
            }
        }
        return matrix;
    }

    public static void resetMatrix(final Coordinate[][][][] matrix, int orientations) {

        --orientations;

        for (int k = 1; k >= 0; --k) {
            final Coordinate[][][] ck = matrix[k];
            for (int y = PLAYFIELD_HEIGHT - 1; y >= 0; --y) {
                final Coordinate[][] cy = ck[y];
                for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                    final Coordinate[] cx = cy[x];
                    for (int o = orientations; o >= 0; --o) {
                        final Coordinate c = cx[o];
                        c.previous = c.next = null;
                    }
                }
            }
        }
    }

    public final boolean keyPressed;
    public final int x;
    public final int y;
    public final int orientation;

    public Coordinate previous;
    public Coordinate next;

    public boolean down;
    public boolean left;
    public boolean right;
    public boolean A;
    public boolean B;

    public Coordinate(final int k, final int x, final int y, final int orientation) {
        this.keyPressed = (k != 0);
        this.x = x;
        this.y = y;
        this.orientation = orientation;
    }
}
