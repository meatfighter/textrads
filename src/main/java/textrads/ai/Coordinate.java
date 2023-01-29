package textrads.ai;

import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import textrads.Tetromino;

public class Coordinate {

    public static Coordinate[][][] createMatrix() {
        final Coordinate[][][] matrix = new Coordinate[PLAYFIELD_HEIGHT + 4][PLAYFIELD_WIDTH + 4][4];
        for (int y = PLAYFIELD_HEIGHT + 3; y >= 0; --y) {
            for (int x = PLAYFIELD_WIDTH + 3; x >= 0; --x) {
                for (int rotation = 3; rotation >= 0; --rotation) {
                    matrix[y][x][rotation] = new Coordinate(x - 2, y - 2, rotation);
                }
            }
        }
        return matrix;
    }

    public static void resetMatrix(final Coordinate[][][] matrix, final int type) {

        final int rotationMax = type == Tetromino.O_TYPE ? 3 : 0;

        for (int y = PLAYFIELD_HEIGHT + 3; y >= 0; --y) {
            final Coordinate[][] cy = matrix[y];
            for (int x = PLAYFIELD_WIDTH + 3; x >= 0; --x) {
                final Coordinate[] cx = cy[x];
                for (int rotation = rotationMax; rotation >= 0; --rotation) {
                    final Coordinate c = cx[rotation];
                    c.previous = c.next = null;
                }
            }
        }
    }

    public final int x;
    public final int y;
    public final int rotation;

    public Coordinate previous;      // visted marker
    public Coordinate next;

    public byte inputEvent;          // input that got the tetromino here
    public float gravityDropTimer;   // when the tetromino arrived here
    public int lockTimer;            // when the tetromino arrived here
    public boolean dropFailed;       // when the tetromino arrived here
    public float moveTimer;          // when the tetromino arrived here

    public Coordinate(final int x, final int y, final int rotation) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
    }
}
