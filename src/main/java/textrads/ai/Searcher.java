package textrads.ai;

import static java.lang.Math.round;
import textrads.Offset;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import textrads.InputEvent;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;

import textrads.Tetromino;

import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import static textrads.MonoGameState.SPAWN_ROTATION;
import static textrads.MonoGameState.SPAWN_X;
import static textrads.MonoGameState.SPAWN_Y;
import static textrads.Tetromino.TETROMINOES;

public class Searcher {

    private final Coordinate[][][] matrix = Coordinate.createMatrix();

    private Tetromino[] tetrominoes;
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

    public void search(final int type, final boolean[][] playfield, final float framesPerGravityDrop, 
            final byte framesPerLock, final float framesPerMove) {

        tetrominoes = TETROMINOES[type];
        Coordinate.resetMatrix(matrix, type);
        Coordinate front = matrix[SPAWN_Y][SPAWN_X][SPAWN_ROTATION];
        
        if (!testPosition(playfield, front)) {
            return; // failed to spawn
        }
        
        front.inputEvent = InputEvent.NOTHING_PRESSED;
        front.dropFailed = false;
        front.gravityDropTimer = framesPerGravityDrop;
        front.lockTimer = framesPerLock;
        front.moveTimer = framesPerMove;
        
        Coordinate rear = front;
        
        // TODO: removed from test()
//        if (coordinate.previous != null) {
//            return false;
//        }
//        coordinate.previous = previous;
//        coordinate.inputEvent = inputEvent;

        do {
            
            final int tetrominoX = front.x;
            final int tetrominoY = front.y;
            final int tetrominoRotation = front.rotation;            
            float gravityDropTimer = front.gravityDropTimer;
            int lockTimer = front.lockTimer;
            boolean dropFailed = front.dropFailed;
            float moveTimer = front.moveTimer;
            
            outer: while (true) {
                if (moveTimer <= 0) {
                    
                    moveTimer += framesPerMove;
                    
                    // shift left
                    inner: if (tetrominoX != 0) { 
                        final Coordinate c = matrix[tetrominoY][tetrominoX - 1][tetrominoRotation];
                        if (!(c.previous == null && testPosition(playfield, c))) {
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;                         
                        c.inputEvent = InputEvent.SHIFT_LEFT_PRESSED;                         
                        c.gravityDropTimer = gravityDropTimer;
                        c.lockTimer = lockTimer;
                        c.dropFailed = dropFailed;
                        c.moveTimer = moveTimer;                         
                    }
                    
                    // shift right
                    inner: if (tetrominoX != PLAYFIELD_WIDTH - 1) { 
                        final Coordinate c = matrix[tetrominoY][tetrominoX + 1][tetrominoRotation];
                        if (!(c.previous == null && testPosition(playfield, c))) {
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;                         
                        c.inputEvent = InputEvent.SHIFT_RIGHT_PRESSED;                         
                        c.gravityDropTimer = gravityDropTimer;
                        c.lockTimer = lockTimer;
                        c.dropFailed = dropFailed;
                        c.moveTimer = moveTimer;                         
                    }

                    if (type != Tetromino.O_TYPE) {
                        
                        // rotate CCW
                        inner: {
                            final int rotation = (tetrominoRotation == 0) ? 3 : tetrominoRotation - 1;
                            Coordinate c = matrix[tetrominoY][tetrominoX][rotation];
                            if (testPosition(playfield, c)) {
                                if (c.previous != null) {
                                    break inner;
                                }
                            } else {
                                final Offset o = Tetromino.CCW[tetrominoRotation];
                                c = matrix[tetrominoY + o.y][tetrominoX + o.x][rotation];
                                if (!(c.previous == null && testPosition(playfield, c))) {
                                    break inner;
                                }
                            }
                            c.previous = front;
                            rear = rear.next = c;                         
                            c.inputEvent = InputEvent.ROTATE_CCW_PRESSED;                         
                            c.gravityDropTimer = gravityDropTimer;
                            c.lockTimer = lockTimer;
                            c.dropFailed = dropFailed;
                            c.moveTimer = moveTimer;
                        }
                        
                        // rotate CW
                        inner: {
                            final int rotation = (tetrominoRotation == 3) ? 0 : tetrominoRotation + 1;
                            Coordinate c = matrix[tetrominoY][tetrominoX][rotation];
                            if (testPosition(playfield, c)) {
                                if (c.previous != null) {
                                    break inner;
                                }
                            } else {
                                final Offset o = Tetromino.CW[tetrominoRotation];
                                c = matrix[tetrominoY + o.y][tetrominoX + o.x][rotation];
                                if (!(c.previous == null && testPosition(playfield, c))) {
                                    break inner;
                                }
                            }
                            c.previous = front;
                            rear = rear.next = c;                         
                            c.inputEvent = InputEvent.ROTATE_CW_PRESSED;                         
                            c.gravityDropTimer = gravityDropTimer;
                            c.lockTimer = lockTimer;
                            c.dropFailed = dropFailed;
                            c.moveTimer = moveTimer;
                        }                        
                    }
                    
                    // soft drop
                    inner: if (tetrominoY != PLAYFIELD_HEIGHT - 1) {
                        final int y = tetrominoY + 1;
                        final Coordinate c = matrix[y][tetrominoX][tetrominoRotation];
                        if (testPosition(playfield, c)) {
                            if (c.previous != null) {
                                break inner;
                            }
                        } else {
                            if (listener != null) {
                                listener.locked(tetrominoX, y, tetrominoRotation);
                            }
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;                         
                        c.inputEvent = InputEvent.SOFT_DROP_PRESSED;                         
                        c.gravityDropTimer = framesPerGravityDrop;
                        c.lockTimer = framesPerLock;
                        c.dropFailed = false;
                        c.moveTimer = moveTimer;
                    } else {
                        if (listener != null) {
                            listener.locked(tetrominoX, tetrominoY, tetrominoRotation);
                        }
                    }
                    
                    break;
                }
                
                
            }
            
            front = front.next;
        } while (front != null);

        do {
            if (front.down) {
                { // no actions / no buttons / burn a frame / matrix[0]          
                    Coordinate neighbor = matrix[0][front.y][front.x][front.rotation];
                    if (test(playfield, neighbor, front, false, false, false, false, false)) {
                        rear = rear.next = neighbor;
                    }
                }
                final Coordinate p = front.previous;
                if (!(p.left || p.right || p.A || p.B)) {
                    if (front.x != 0) { // left
                        Coordinate neighbor = matrix[1][front.y][front.x - 1][front.rotation];
                        if (test(playfield, neighbor, front, false, true, false, false, false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    if (front.x != PLAYFIELD_WIDTH - 1) { // right
                        Coordinate neighbor = matrix[1][front.y][front.x + 1][front.rotation];
                        if (test(playfield, neighbor, front, false, false, true, false, false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    { // rotate right
                        final Coordinate neighbor = matrix[1][front.y][front.x][(front.rotation == tetrominoes.length - 1) ? 0
                                : (front.rotation + 1)];
                        if (test(playfield, neighbor, front, false, false, false, true, false)) {
                            rear = rear.next = neighbor;
                        }
                    }
                    { // rotate left
                        final Coordinate neighbor = matrix[1][front.y][front.x][(front.rotation == 0) ? (tetrominoes.length - 1)
                                : (front.rotation - 1)];
                        if (test(playfield, neighbor, front, false, false, false, false, true)) {
                            rear = rear.next = neighbor;
                        }
                    }
                }
            } else {
                boolean locked = true;
                if (front.y != playfield.length - 1) {
                    final Coordinate neighbor
                            = matrix[1][front.y + 1][front.x][front.rotation];
                    if (test(playfield, neighbor, front, true, false, false, false, false)) {
                        rear = rear.next = neighbor;
                        locked = false;
                    }
                }
                if (locked && listener != null) {
                    listener.locked(front.x, front.y, front.rotation);
                }
            }

            front = front.next;
        } while (front != null);
    }

    private boolean testPosition(final boolean[][] playfield, final Coordinate coordinate) {

        final int y = coordinate.y;
        if (y < 0) {
            return false;
        }
                       
        final Tetromino tetromino = tetrominoes[coordinate.rotation];
        final int x = coordinate.x; 
        if (!tetromino.validPosition[y + 2][x + 2]) {
            return false;
        }

        final Offset[] offsets = tetromino.offsets;
        for (int i = 3; i >= 0; --i) {
            final Offset offset = offsets[i];
            final int by = y + offset.y;
            if (by >= 0 && playfield[by][x + offset.x]) {
                return false;
            }
        }

        return true;
    }
}