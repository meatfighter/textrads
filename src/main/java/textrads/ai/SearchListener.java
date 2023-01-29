package textrads.ai;

public interface SearchListener {
    void locked(int tetrominoX, int tetrominoY, int tetrominoRotation, float framesPerGravityDrop, byte framesPerLock, 
            float framesPerMove);
}
