package textrads.ai;

import java.util.Collections;
import java.util.List;

import textrads.InputEvent;
import textrads.Offset;
import textrads.Tetromino;

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

    private final Coordinate[][][][] matrix = Coordinate.createMatrix();

    private Tetromino[] tetrominoes;
    private SearchListener listener;

    public void setSearchListener(final SearchListener listener) {
        this.listener = listener;
    }

    public void getMoves(final int x, final int y, final int rotation, final int dropFailed, final List<Byte> moves) {
        
        moves.clear();
        Coordinate coordinate = matrix[dropFailed][rotation][y + 2][x + 2];
        do {
            if (coordinate.inputEvent != InputEvent.NOTHING_PRESSED) {
                moves.add(coordinate.inputEvent);
            }
            coordinate = coordinate.previous;
        } while (coordinate != coordinate.previous);        
        Collections.reverse(moves);
    }

    public void search(final int type, final boolean[][] playfield, final float framesPerGravityDrop, 
            final byte framesPerLock, final float framesPerMove) {

        tetrominoes = TETROMINOES[type];
        Coordinate.resetMatrix(matrix, type);
        Coordinate front = matrix[0][SPAWN_ROTATION][SPAWN_Y + 2][SPAWN_X + 2];
        
        if (!testPosition(playfield, front)) {
            return; // failed to spawn
        }
        
        front.inputEvent = InputEvent.NOTHING_PRESSED;
        front.gravityDropTimer = framesPerGravityDrop;
        front.lockTimer = framesPerLock;
        front.moveTimer = framesPerMove;
        front.previous = front;
        
        Coordinate rear = front;

        do {
            
            // TODO ARE THESE FINALS REQUIRED
            final int tetrominoX = front.x; // TODO REMOVE +2 AND -2's
            final int tetrominoY = front.y;
            final int tetrominoRotation = front.rotation;
            final int dropFailed = front.dropFailed;
            
            TimerEvent timerEvent = TimerEvent.MOVE;
            int timePassed = 0;
            
            if (front.moveTimer <= 0f) {
                // default values
            } else if (front.dropFailed == 1 && front.lockTimer < 0) {
                timerEvent = TimerEvent.LOCK;
            } else if (front.dropFailed == 0 && front.gravityDropTimer <= 0f) {
                timerEvent = TimerEvent.GRAVITY_DROP;
            } else {
                timePassed = 1 + (int) front.moveTimer;
                if (front.dropFailed == 1) {
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
                    inner: { 
                        if (!testPosition(playfield, tetrominoX - 1, tetrominoY, tetrominoRotation)) {
                            break inner;
                        }                       
                        final Coordinate c = matrix[(dropFailed == 1) 
                                ? (testPosition(playfield, tetrominoX - 1, tetrominoY + 1, tetrominoRotation) ? 0 : 1) 
                                : 0][tetrominoRotation][tetrominoY + 2][tetrominoX + 1];
                        if (c.previous != null) {
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c; 
                        c.inputEvent = InputEvent.SHIFT_LEFT_PRESSED;                         
                        c.gravityDropTimer = front.gravityDropTimer - timePassed;
                        c.lockTimer = dropFailed == 1 ? front.lockTimer - timePassed : framesPerLock;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;
                    }
                    
                    // shift right
                    inner: { 
                        if (!testPosition(playfield, tetrominoX + 1, tetrominoY, tetrominoRotation)) {
                            break inner;
                        }                       
                        final Coordinate c = matrix[(dropFailed == 1) 
                                ? (testPosition(playfield, tetrominoX + 1, tetrominoY + 1, tetrominoRotation) ? 0 : 1) 
                                : 0][tetrominoRotation][tetrominoY + 2][tetrominoX + 3];
                        if (c.previous != null) {
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c;
                        c.inputEvent = InputEvent.SHIFT_RIGHT_PRESSED;                         
                        c.gravityDropTimer = front.gravityDropTimer - timePassed;
                        c.lockTimer = dropFailed == 1 ? front.lockTimer - timePassed : framesPerLock;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;                         
                    }

                    if (type != Tetromino.O_TYPE) {
                        
                        // rotate CCW
                        inner: {
                            final int rotation = (tetrominoRotation == 0) ? 3 : tetrominoRotation - 1;
                            final Coordinate c;
                            if (testPosition(playfield, tetrominoX, tetrominoY, rotation)) {
                                c = matrix[(dropFailed == 1) ? (testPosition(playfield, tetrominoX, tetrominoY + 1, 
                                        rotation) ? 0 : 1) : 0][rotation][tetrominoY + 2][tetrominoX + 2];                                
                            } else {
                                final Offset o = Tetromino.CCW[tetrominoRotation];
                                if (!testPosition(playfield, tetrominoX + o.x, tetrominoY + o.y, rotation)) {
                                    break inner;
                                }
                                c = matrix[(dropFailed == 1) ? (testPosition(playfield, tetrominoX + o.x, 
                                        tetrominoY + o.y + 1, rotation) ? 0 : 1) : 0][rotation][tetrominoY + o.y + 2]
                                        [tetrominoX + o.x + 2];
                            }
                            if (c.previous != null) {
                                break inner;
                            }
                            c.previous = front;
                            rear = rear.next = c; 
                            c.inputEvent = InputEvent.ROTATE_CCW_PRESSED;                         
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;
                            c.lockTimer = dropFailed == 1 ? front.lockTimer - timePassed : framesPerLock;
                            c.moveTimer = front.moveTimer + framesPerMove - timePassed;
                        }
                        
                        // rotate CW
                        inner: {
                            final int rotation = (tetrominoRotation == 3) ? 0 : tetrominoRotation + 1;
                            final Coordinate c;
                            if (testPosition(playfield, tetrominoX, tetrominoY, rotation)) {
                                c = matrix[(dropFailed == 1) ? (testPosition(playfield, tetrominoX, tetrominoY + 1, 
                                        rotation) ? 0 : 1) : 0][rotation][tetrominoY + 2][tetrominoX + 2];                                
                            } else {
                                final Offset o = Tetromino.CW[tetrominoRotation];
                                if (!testPosition(playfield, tetrominoX + o.x, tetrominoY + o.y, rotation)) {
                                    break inner;
                                }
                                c = matrix[(dropFailed == 1) ? (testPosition(playfield, tetrominoX + o.x, 
                                        tetrominoY + o.y + 1, rotation) ? 0 : 1) : 0][rotation][tetrominoY + o.y + 2]
                                        [tetrominoX + o.x + 2];
                            }
                            if (c.previous != null) {
                                break inner;
                            }
                            c.previous = front;
                            rear = rear.next = c;
                            c.inputEvent = InputEvent.ROTATE_CW_PRESSED;                         
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;
                            c.lockTimer = dropFailed == 1 ? front.lockTimer - timePassed : framesPerLock;
                            c.moveTimer = front.moveTimer + framesPerMove - timePassed;
                        }                        
                    }
                    
                    // soft drop
                    inner: {
                        final Coordinate c = matrix[0][tetrominoRotation][tetrominoY + 3][tetrominoX + 2];
                        if (testPosition(playfield, c)) {
                            if (c.previous != null) {
                                break inner;
                            }
                        } else {
                            if (listener != null) {
                                listener.locked(tetrominoX, tetrominoY, tetrominoRotation, dropFailed, 
                                        framesPerGravityDrop, framesPerLock, framesPerMove);
                            }
                            break inner;
                        }
                        c.previous = front;
                        rear = rear.next = c; 
                        c.inputEvent = InputEvent.SOFT_DROP_PRESSED;                         
                        c.gravityDropTimer = framesPerGravityDrop;
                        c.lockTimer = framesPerLock;
                        c.moveTimer = front.moveTimer + framesPerMove - timePassed;
                    } 
                    
                    break;

                case GRAVITY_DROP:

                    inner: {
                        Coordinate c = matrix[0][tetrominoRotation][tetrominoY + 3][tetrominoX + 2];
                        if (testPosition(playfield, c)) {
                            if (c.previous != null) {
                                break inner;
                            }                                   
                            c.gravityDropTimer = front.gravityDropTimer + framesPerGravityDrop - timePassed;
                        } else {
                            c = matrix[1][tetrominoRotation][tetrominoY + 2][tetrominoX + 2];
                            if (c.previous != null) {
                                break inner;
                            }
                            c.gravityDropTimer = front.gravityDropTimer - timePassed;
                        }
                        c.previous = front;
                        rear = rear.next = c;
                        c.inputEvent = InputEvent.NOTHING_PRESSED; 
                        c.lockTimer = framesPerLock;
                        c.moveTimer = front.moveTimer - timePassed;
                    } 

                    break;
                    
                case LOCK:
                    
                    if (listener != null) {
                        listener.locked(tetrominoX, tetrominoY, tetrominoRotation, dropFailed, framesPerGravityDrop, 
                                framesPerLock, framesPerMove);
                    }
                    
                    break;
            }
                        
            front = front.next;
        } while (front != null);
    }
    
    private boolean testPosition(final boolean[][] playfield, final int tetrominoX, final int tetrominoY, 
            final int tetrominoRotation) {

        if (tetrominoY < 0) {
            return false;
        }
                       
        final Tetromino tetromino = tetrominoes[tetrominoRotation];
        if (!tetromino.validPosition[tetrominoY + 2][tetrominoX + 2]) {
            return false;
        }

        final Offset[] offsets = tetromino.offsets;
        for (int i = 3; i >= 0; --i) {
            final Offset offset = offsets[i];
            final int by = tetrominoY + offset.y;
            if (by >= 0 && playfield[by][tetrominoX + offset.x]) {
                return false;
            }
        }

        return true;
    }    

    private boolean testPosition(final boolean[][] playfield, final Coordinate coordinate) {
        return testPosition(playfield, coordinate.x, coordinate.y, coordinate.rotation);
    }
}