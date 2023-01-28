package textrads.ai;

import textrads.Offset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import static textrads.MonoGameState.SPAWN_ROTATION;
import static textrads.MonoGameState.SPAWN_X;
import static textrads.MonoGameState.SPAWN_Y;

public class Searcher {

    private final Coordinate[][][][] matrix = Coordinate.createMatrix();

    private Tetrimino[] tetriminos;
    private SearchListener listener;

    public void setSearchListener(final SearchListener listener) {
        this.listener = listener;
    }

    public void getMoves(final int x, final int y, final int orientation, final List<Coordinate> moves) {

        moves.clear();
        Coordinate coordinate = matrix[1][y][x][orientation];
        if (coordinate.previous == null) {
            coordinate = matrix[0][y][x][orientation];
        }
        while (true) {
            if (coordinate.previous == coordinate) {
                break;
            } else {
                moves.add(coordinate);
                coordinate = coordinate.previous;
            }
        }
        for (final Iterator<Coordinate> i = moves.iterator(); i.hasNext();) {
            coordinate = i.next();
            if (coordinate.left || coordinate.right || coordinate.A || coordinate.B) {
                break;
            } else {
                i.remove();
            }
        }
        Collections.reverse(moves);
    }

    public void search(final int type, final boolean[][] playfield) {

        tetriminos = Tetrimino.TETRIMINOS[playfield.length][type];
        Coordinate.resetMatrix(matrix, tetriminos.length);
        Coordinate front = matrix[1][SPAWN_Y][SPAWN_X][SPAWN_ROTATION];
        if (!test(playfield, front, front, true, false, false, false, false)) {
            return;
        }
        Coordinate rear = front;

        do {
            if (front.down) {
                { // no actions / no buttons / burn a frame / matrix[0]          
                    Coordinate neighbor = matrix[0][front.y][front.x][front.orientation];
                    if (test(playfield, neighbor, front, false, false, false, false,
                            false)) {
                        rear = rear.next = neighbor;
                    }
                }
                final Coordinate p = front.previous;
                if (!(level29 && (p.left || p.right || p.A || p.B))) {
                    if (front.x != 0) { // left
                        Coordinate neighbor = matrix[1][front.y][front.x - 1][front.orientation];
                        if (test(playfield, neighbor, front, false, true, false, false,
                                false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    if (front.x != PLAYFIELD_WIDTH - 1) { // right
                        Coordinate neighbor = matrix[1][front.y][front.x + 1][front.orientation];
                        if (test(playfield, neighbor, front, false, false, true, false,
                                false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    { // rotate right
                        final Coordinate neighbor = matrix[1][front.y][front.x][(front.orientation == tetriminos.length - 1) ? 0
                                : (front.orientation + 1)];
                        if (test(playfield, neighbor, front, false, false, false, true,
                                false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    { // rotate left
                        final Coordinate neighbor = matrix[1][front.y][front.x][(front.orientation == 0) ? (tetriminos.length - 1)
                                : (front.orientation - 1)];
                        if (test(playfield, neighbor, front, false, false, false, false,
                                true)) {
                            rear = rear.next = neighbor;
                        }
                    }
                }
            } else {
                boolean locked = true;
                if (front.y != playfield.length - 1) {
                    final Coordinate neighbor
                            = matrix[1][front.y + 1][front.x][front.orientation];
                    if (test(playfield, neighbor, front, true, false, false, false,
                            false)) {
                        rear = rear.next = neighbor;
                        locked = false;
                    }
                }
                if (locked && listener != null) {
                    listener.placed(front.x, front.y, front.orientation);
                }
            }

            front = front.next;
        } while (front != null);
    }

    private boolean test(
            final boolean[][] playfield,
            final Coordinate coordinate,
            final Coordinate previous,
            final boolean down,
            final boolean left,
            final boolean right,
            final boolean A,
            final boolean B) {

        if (coordinate.previous != null) {
            return false;
        }

        final int x = coordinate.x;
        final int y = coordinate.y;
        final int orientation = coordinate.orientation;

        final Tetrimino tetrimino = tetriminos[orientation];
        if (!tetrimino.validPosition[y][x]) {
            return false;
        }

        final Offset[] offsets = tetrimino.offsets;
        for (int i = 3; i >= 0; --i) {
            final Offset offset = offsets[i];
            final int X = x + offset.x;
            final int Y = y + offset.y;
            if (Y >= 0 && playfield[Y][X]) {
                return false;
            }
        }

        coordinate.previous = previous;
        coordinate.down = down;
        coordinate.left = left;
        coordinate.right = right;
        coordinate.A = A;
        coordinate.B = B;

        return true;
    }
}
