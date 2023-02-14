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
    
    private final Coordinate[][][][] matrix = Coordinate.createMatrix();

    private Tetromino[] tetrominoes;
    private SearchListener listener;    

    public void setSearchListener(final SearchListener listener) {
        this.listener = listener;
    }

    public void getMoves(final int x, final int y, final int rotation, final int dropFailed, 
            final List<Coordinate> moves) {
        
        moves.clear();
        Coordinate coordinate = matrix[dropFailed][rotation][y + 2][x + 2];
        do {
            if (coordinate.inputEvent != InputEvent.NOTHING_PRESSED) {
                moves.add(coordinate);
            }
            coordinate = coordinate.previous;
        } while (coordinate != coordinate.previous);        
        Collections.reverse(moves);
    }
    
    public void search(final int type, final boolean[][] playfield, final float framesPerGravityDrop, 
            final byte framesPerLock, final float framesPerMove) {

        tetrominoes = TETROMINOES[type];
        Coordinate.resetMatrix(matrix, type);        

        Coordinate head = matrix[0][SPAWN_ROTATION][SPAWN_Y + 2][SPAWN_X + 2];
        if (!testPosition(playfield, head)) {
            return; // failed to spawn
        }
        
        head.inputEvent = InputEvent.NOTHING_PRESSED;
        head.gravityDropTimer = framesPerGravityDrop;
        head.lockTimer = framesPerLock;
        head.moveTimer = framesPerMove;
        head.previous = head;
        head.state = Coordinate.State.START_MOVE;
                
        Coordinate tail = head;
        
        do {
            
            final int tetrominoX = tail.x;
            final int tetrominoY = tail.y;
            final int tetrominoRotation = tail.rotation;
            final int dropFailed = tail.dropFailed;
            
            float gravityDropTimer = tail.gravityDropTimer;
            int lockTimer = tail.lockTimer;
            float moveTimer = tail.moveTimer;
            Coordinate.State state = tail.state;
            
            outer: while (true) {
                
                switch (state) {
                    
                    case START_MOVE:
                        --moveTimer;
                        state = Coordinate.State.CONTINUE_MOVE;
                        break;
                        
                    case CONTINUE_MOVE:
                        if (moveTimer > 0) {
                            state = Coordinate.State.START_UPDATE;
                            break;
                        }
                        
                        moveTimer += framesPerMove;
                        
                        // shift left
                        inner: { 
                            final Coordinate c = matrix[dropFailed][tetrominoRotation][tetrominoY + 2][tetrominoX + 1];
                            if (c.previous != null || !testPosition(playfield, c)) {
                                break inner;
                            }
                            c.previous = tail;
                            head = head.next = c;
                            c.inputEvent = InputEvent.SHIFT_LEFT_PRESSED;                         
                            c.gravityDropTimer = gravityDropTimer;
                            c.lockTimer = lockTimer;
                            c.moveTimer = moveTimer;
                            c.state = state;
                        }

                        // shift right
                        inner: {                       
                            final Coordinate c = matrix[dropFailed][tetrominoRotation][tetrominoY + 2][tetrominoX + 3];
                            if (c.previous != null || !testPosition(playfield, c)) {
                                break inner;
                            }
                            c.previous = tail;
                            head = head.next = c;
                            c.inputEvent = InputEvent.SHIFT_RIGHT_PRESSED;                         
                            c.gravityDropTimer = gravityDropTimer;
                            c.lockTimer = lockTimer;
                            c.moveTimer = moveTimer;
                            c.state = state;
                        }  
                        
                        if (type != Tetromino.O_TYPE) {

                            // rotate CCW
                            inner: {
                                final int rotation = (tetrominoRotation == 0) ? 3 : tetrominoRotation - 1;
                                Coordinate c = matrix[dropFailed][rotation][tetrominoY + 2][tetrominoX + 2];
                                if (!testPosition(playfield, c)) {
                                    final Offset o = Tetromino.CCW[tetrominoRotation];
                                    c = matrix[dropFailed][rotation][tetrominoY + o.y + 2][tetrominoX + o.x + 2];                                
                                    if (!testPosition(playfield, c)) {
                                        break inner;
                                    }                                
                                }
                                if (c.previous != null) {
                                    break inner;
                                }
                                c.previous = tail;
                                head = head.next = c;
                                c.inputEvent = InputEvent.ROTATE_CCW_PRESSED;                         
                                c.gravityDropTimer = gravityDropTimer;
                                c.lockTimer = lockTimer;
                                c.moveTimer = moveTimer;
                                c.state = state;
                            }

                            // rotate CW
                            inner: {
                                final int rotation = (tetrominoRotation == 3) ? 0 : tetrominoRotation + 1;
                                Coordinate c = matrix[dropFailed][rotation][tetrominoY + 2][tetrominoX + 2];
                                if (!testPosition(playfield, c)) {
                                    final Offset o = Tetromino.CW[tetrominoRotation];
                                    c = matrix[dropFailed][rotation][tetrominoY + o.y + 2][tetrominoX + o.x + 2];
                                    if (!testPosition(playfield, c)) {
                                        break inner;
                                    }                                
                                }
                                if (c.previous != null) {
                                    break inner;
                                }
                                c.previous = tail;
                                head = head.next = c;
                                c.inputEvent = InputEvent.ROTATE_CW_PRESSED;                         
                                c.gravityDropTimer = gravityDropTimer;
                                c.lockTimer = lockTimer;
                                c.moveTimer = moveTimer;
                                c.state = state;
                            }                        
                        }  
                        
                        // soft drop
                        inner: {
                            final Coordinate c = matrix[0][tetrominoRotation][tetrominoY + 3][tetrominoX + 2];
                            if (!testPosition(playfield, c)) {
                                listener.locked(tetrominoX, tetrominoY, tetrominoRotation, dropFailed, 
                                        framesPerGravityDrop, framesPerLock, framesPerMove);
                                break inner;
                            }
                            if (c.previous != null) {
                                break inner;
                            }
                            c.previous = tail;
                            head = head.next = c;
                            c.inputEvent = InputEvent.SOFT_DROP_PRESSED;                         
                            c.gravityDropTimer = framesPerGravityDrop;
                            c.lockTimer = framesPerLock;
                            c.moveTimer = moveTimer;
                            c.state = state;
                        }                        
                        
                        break outer;
                        
                    case START_UPDATE:                        
                        if (dropFailed == 0) {
                            --gravityDropTimer; 
                        }
                        state = Coordinate.State.CONTINUE_UPDATE;                        
                        break;
                        
                    case CONTINUE_UPDATE:
                                        
                        if (dropFailed == 1) {
                            final Coordinate c = matrix[0][tetrominoRotation][tetrominoY + 3][tetrominoX + 2];
                            if (testPosition(playfield, c)) {
                                if (c.previous == null) {                       
                                    c.previous = tail;
                                    head = head.next = c; 
                                    c.inputEvent = InputEvent.NOTHING_PRESSED;                         
                                    c.gravityDropTimer = gravityDropTimer + framesPerGravityDrop;
                                    c.lockTimer = framesPerLock;
                                    c.moveTimer = moveTimer;
                                    c.state = Coordinate.State.START_MOVE;
                                }
                                break outer;
                            } else if (--lockTimer < 0) {
                                listener.locked(tetrominoX, tetrominoY, tetrominoRotation, dropFailed, 
                                        framesPerGravityDrop, framesPerLock, framesPerMove);
                                break outer;
                            }                            
                        } else if (gravityDropTimer <= 0) {                            
                            Coordinate c = matrix[0][tetrominoRotation][tetrominoY + 3][tetrominoX + 2];
                            if (testPosition(playfield, c)) {
                                if (c.previous == null) {
                                    c.previous = tail;
                                    head = head.next = c;
                                    c.inputEvent = InputEvent.NOTHING_PRESSED;                         
                                    c.gravityDropTimer = gravityDropTimer + framesPerGravityDrop;
                                    c.lockTimer = framesPerLock;
                                    c.moveTimer = moveTimer;
                                    c.state = state;
                                }
                            } else {                                
                                c = matrix[1][tetrominoRotation][tetrominoY + 2][tetrominoX + 2];
                                if (c.previous == null) {                                    
                                    c.previous = tail;
                                    head = head.next = c;
                                    c.inputEvent = InputEvent.NOTHING_PRESSED;                         
                                    c.gravityDropTimer = gravityDropTimer;
                                    c.lockTimer = lockTimer;
                                    c.moveTimer = moveTimer;
                                    c.state = state;
                                }
                            }
                            break outer;
                        } else {
                            state = Coordinate.State.START_MOVE;
                        }
                        
                        break;
                }
            } 
            
            tail = tail.next;
            
        } while (tail != null);
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