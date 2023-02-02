package textrads.ai;

import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import textrads.Tetromino;

public class Coordinate {

    public static Coordinate[][][][] createMatrix() {
        final Coordinate[][][][] matrix = new Coordinate[2][4][PLAYFIELD_HEIGHT + 4][PLAYFIELD_WIDTH + 4];
        for (int dropFailed = 1; dropFailed >= 0; --dropFailed) {
            for (int rotation = 3; rotation >= 0; --rotation) {
                for (int y = PLAYFIELD_HEIGHT + 3; y >= 0; --y) {
                    for (int x = PLAYFIELD_WIDTH + 3; x >= 0; --x) {
                        matrix[dropFailed][rotation][y][x] = new Coordinate(x - 2, y - 2, rotation, dropFailed);
                    }
                }
            }
        }
        return matrix;
    }

    public static void resetMatrix(final Coordinate[][][][] matrix, final int type) {
        final int rotationMax = (type == Tetromino.O_TYPE) ? 0 : 3;
        for (int dropFailed = 1; dropFailed >= 0; --dropFailed) {
            final Coordinate[][][] cd = matrix[dropFailed];
            for (int rotation = rotationMax; rotation >= 0; --rotation) {
                final Coordinate[][] cr = cd[rotation];
                for (int y = PLAYFIELD_HEIGHT + 3; y >= 0; --y) {
                    final Coordinate[] cy = cr[y];
                    for (int x = PLAYFIELD_WIDTH + 3; x >= 0; --x) {
                        final Coordinate c = cy[x];
                        c.previous = c.next = null;
                    }
                }
            }
        }
    }

    public final int x;
    public final int y;
    public final int rotation;
    public final int dropFailed;

    public Coordinate previous;      // visted marker
    public Coordinate next;

    public byte inputEvent;          // input that got the tetromino here
    public float gravityDropTimer;   // when the tetromino arrived here
    public int lockTimer;            // when the tetromino arrived here
    public float moveTimer;          // when the tetromino arrived here

    public Coordinate(final int x, final int y, final int rotation, final int dropFailed) {
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.dropFailed = dropFailed;
    }
    
    @Override
    public String toString() {
        return String.format("%d %d %d %d", x, y, rotation, dropFailed);
    }
}
