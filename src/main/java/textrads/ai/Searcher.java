package textrads.ai;

import java.util.Collections;
import java.util.List;

import textrads.InputEvent;
import textrads.Offset;
import textrads.Tetromino;

import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.SPAWN_ROTATION;
import static textrads.MonoGameState.SPAWN_X;
import static textrads.MonoGameState.SPAWN_Y;
import static textrads.Tetromino.TETROMINOES;

public final class Searcher {
    
    private static enum TimerEvent {
        MOVE,
        GRAVITY_DROP,
        LOCK,        
    }

    private final Coordinate[][][] matrix = Coordinate.createMatrix();

    private Tetromino[] tetrominoes;
    private SearchListener listener;

    public void setSearchListener(final SearchListener listener) {
        this.listener = listener;
    }

    public void getMoves(final int x, final int y, final int rotation, final List<Byte> moves) {
        moves.clear();
        Coordinate coordinate = matrix[y][x][rotation];
        do {
            if (coordinate.inputEvent != InputEvent.NOTHING_PRESSED) {
                moves.add(coordinate.inputEvent);
            }
            coordinate = coordinate.previous;
        } while (coordinate != null);        
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

        do {
            
            final int tetrominoX = front.x;
            final int tetrominoY = front.y;
            final int tetrominoRotation = front.rotation; 
            
            TimerEvent timerEvent = TimerEvent.MOVE;
            int timePassed = 0;
            
            if (front.moveTimer <= 0f) {
                // default values
            } else if (front.dropFailed && front.lockTimer < 0) {
                timerEvent = TimerEvent.LOCK;
            } else if (!front.dropFailed && front.gravityDropTimer <= 0f) {
                timerEvent = TimerEvent.GRAVITY_DROP;
            } else {
                timePassed = 1 + (int) front.moveTimer;
                if (front.dropFailed) {
                    if (front.lockTimer < timePassed) {
                        timerEvent = TimerEvent.LOCK;
                        timePassed = front.lockTimer;
                    }
                } else {
                    final int tp = 1 + (int) front.gravityDropTimer;
                    if (tp < timePassed) {
                        timerEvent = TimerEvent.GRAVITY_DROP;
                        timePassed = tp;
                    }
                }
            }
            
            switch (timerEvent) {
                case MOVE:
                    
                    // shift left
                    inner: if (tetrominoX != 0) { 
                        final Coordinate c = matrix[tetrominoY][tetrominoX - 1][tetrominoRotation];
                        if (!(c.previous == null && testPosition(playfield, c))) {
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;                         
                        c.inputEvent = InputEvent.SHIFT_LEFT_PRESSED;                         
                        c.gravityDropTimer = front.gravityDropTimer - timePassed;
                        c.lockTimer = front.dropFailed ? front.lockTimer - timePassed : framesPerLock;
                        c.dropFailed = front.dropFailed;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;                         
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
                        c.gravityDropTimer = front.gravityDropTimer - timePassed;
                        c.lockTimer = front.dropFailed ? front.lockTimer - timePassed : framesPerLock;
                        c.dropFailed = front.dropFailed;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;                         
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
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;
                            c.lockTimer = front.dropFailed ? front.lockTimer - timePassed : framesPerLock;
                            c.dropFailed = front.dropFailed;
                            c.moveTimer = front.moveTimer + framesPerMove - timePassed;
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
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;
                            c.lockTimer = front.dropFailed ? front.lockTimer - timePassed : framesPerLock;
                            c.dropFailed = front.dropFailed;
                            c.moveTimer = front.moveTimer + framesPerMove - timePassed;
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
                                listener.locked(tetrominoX, y, tetrominoRotation, framesPerGravityDrop, framesPerLock, 
                                        framesPerMove);
                            }
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;                         
                        c.inputEvent = InputEvent.SOFT_DROP_PRESSED;                         
                        c.gravityDropTimer = framesPerGravityDrop;
                        c.lockTimer = framesPerLock;
                        c.dropFailed = false;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;
                    } else {
                        if (listener != null) {
                            listener.locked(tetrominoX, tetrominoY, tetrominoRotation, framesPerGravityDrop, 
                                    framesPerLock, framesPerMove);
                        }
                    }
                    
                    break;

                case GRAVITY_DROP:

                    inner: if (tetrominoY != PLAYFIELD_HEIGHT - 1) {
                        Coordinate c = matrix[tetrominoY + 1][tetrominoX][tetrominoRotation];
                        if (testPosition(playfield, c)) {
                            if (c.previous != null) {
                                break inner;
                            }       
                            c.previous = front;
                            rear = rear.next = c;                                                                             
                            c.gravityDropTimer = front.gravityDropTimer + framesPerGravityDrop - timePassed;
                            c.dropFailed = false;
                        } else {
                            c = matrix[tetrominoY][tetrominoX][tetrominoRotation];
                            rear = rear.next = c;                        
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;                            
                            c.dropFailed = true;                            
                        }
                        c.inputEvent = InputEvent.NOTHING_PRESSED; 
                        c.lockTimer = framesPerLock;
                        c.moveTimer = front.moveTimer - timePassed;
                    } else {
                        final Coordinate c = matrix[tetrominoY][tetrominoX][tetrominoRotation];
                        rear = rear.next = c;                        
                        c.gravityDropTimer = front.gravityDropTimer - timePassed;                            
                        c.dropFailed = true;
                        c.inputEvent = InputEvent.NOTHING_PRESSED; 
                        c.lockTimer = framesPerLock;
                        c.moveTimer = front.moveTimer - timePassed;
                    }

                    break;
                    
                case LOCK:
                    
                    if (listener != null) {
                        listener.locked(tetrominoX, tetrominoY, tetrominoRotation, framesPerGravityDrop, framesPerLock, 
                                framesPerMove);
                    }
                    
                    break;
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