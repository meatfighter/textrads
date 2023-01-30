package textrads.ai;

public interface SearchListener {
    void locked(int tetrominoX, int tetrominoY, int tetrominoRotation, int dropFailed, float framesPerGravityDrop, 
            byte framesPerLock, float framesPerMove);
}
