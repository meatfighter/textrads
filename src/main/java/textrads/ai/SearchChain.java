package textrads.ai;

import textrads.Offset;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import static textrads.MonoGameState.SPAWN_X;
import static textrads.MonoGameState.SPAWN_Y;
import static textrads.Tetromino.TETROMINOES;
import static textrads.ai.Playfield.createPlayfield;
import static textrads.ai.Playfield.lock;

public class SearchChain {

    private final boolean[][] playfield1;
    private final boolean[][] playfield2;

    private final Searcher searcher1;
    private final Searcher searcher2;
    private final SeedFiller seedFiller;

    private final int[] columnMinY = new int[PLAYFIELD_WIDTH];

    private double[] weights;

    private boolean[][] playfield;
    private int type1;
    private int type2;

    private int lockHeight1;
    private int lockHeight2;

    private int linesCleared1;
    private int linesCleared2;

    private int x1;
    private int y1;
    private int o1;

    private boolean bestFound;
    private double bestScore;
    private int bestX;
    private int bestY;
    private int bestO;

    public SearchChain() {

        playfield1 = createPlayfield();
        playfield2 = createPlayfield();
        searcher1 = new Searcher();
        searcher2 = new Searcher();
        seedFiller = new SeedFiller();

        searcher1.setSearchListener((tetrominoX, tetrominoY, tetrominoRotation, framesPerGravityDrop, framesPerLock, 
                framesPerMove) -> {
            this.x1 = tetrominoX;
            this.y1 = tetrominoY;
            this.o1 = tetrominoRotation;
            lockHeight1 = TETROMINOES[type1][tetrominoRotation].getLockHeight(tetrominoY);
            linesCleared1 = lock(playfield, playfield1, type1, tetrominoX, tetrominoY, tetrominoRotation);
            if (seedFiller.canClearMoreLines(playfield1)) {
                searcher2.search(type2, playfield1, framesPerGravityDrop, framesPerLock, framesPerMove);
            }
        });

        searcher2.setSearchListener((tetrominoX, tetrominoY, tetrominoRotation, framesPerGravityDrop, framesPerLock, 
                framesPerMove) -> {
            lockHeight2 = TETROMINOES[type2][tetrominoRotation].getLockHeight(tetrominoY);
            linesCleared2 = lock(playfield1, playfield2, type2, tetrominoX, tetrominoY, tetrominoRotation);
            if (canAllTypesSpawn(playfield2) && seedFiller.canClearMoreLines(playfield2)) {
                evaluate();
            }
        });
    }

    public void setWeights(final double[] weights) {
        this.weights = weights;
    }

    public boolean isBestFound() {
        return bestFound;
    }

    public int getX() {
        return bestX;
    }

    public int getY() {
        return bestY;
    }

    public int getOrientation() {
        return bestO;
    }

    public void getMoves(final List<Byte> moves) {
        searcher1.getMoves(bestX, bestY, bestO, moves);
    }

    public void search(final int currentType, final int nextType, final boolean[][] playfield, 
            final float framesPerGravityDrop, final byte framesPerLock, final float framesPerMove) {

        this.playfield = playfield;
        this.type1 = currentType;
        this.type2 = nextType;

        bestFound = false;
        bestScore = Double.MAX_VALUE;        
        
        if (seedFiller.canClearMoreLines(playfield)) {
            searcher1.search(currentType, playfield, framesPerGravityDrop, framesPerLock, framesPerMove);
        }
    }

    private boolean canAllTypesSpawn(final boolean[][] playfield) {
        for (int i = 6; i >= 0; --i) {
            final Offset[] offsets = TETROMINOES[i][0].offsets;
            for (int j = 3; j >= 0; --j) {
                final Offset o = offsets[j];
                if (playfield[SPAWN_Y + o.y][SPAWN_X + o.x]) {
                    return false;
                }
            }
        }
        return true;
    }

    private void evaluate() {

        final int totalLinesCleared = linesCleared1 + linesCleared2;

        final int totalLockHeight = lockHeight1 + lockHeight2;

        outer: for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            for (int y = 0; y < PLAYFIELD_HEIGHT; ++y) {
                if (playfield2[y][x]) {
                    columnMinY[x] = y;
                    continue outer;
                }
            }
            columnMinY[x] = PLAYFIELD_HEIGHT;
        }

        int maxColumnY = 0;
        int minColumnY = PLAYFIELD_HEIGHT;
        int totalColumnHeights = 0;
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            if (columnMinY[x] < minColumnY) {
                minColumnY = columnMinY[x];
            }
            if (columnMinY[x] > maxColumnY) {
                maxColumnY = columnMinY[x];
            }
            totalColumnHeights += PLAYFIELD_HEIGHT - columnMinY[x];
        }
        int columnHeightSpread = maxColumnY - minColumnY;

        int totalWellCells = 0;
        int totalDeepWells = 0;
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            int wellCells = 0;
            switch (x) {
                case 0: {
                    final int minY = columnMinY[x + 1];
                    for (int y = columnMinY[x] - 1; y >= minY; --y) {
                        if (playfield2[y][x + 1]) {
                            ++wellCells;
                        }
                    }
                    break;
                }
                case PLAYFIELD_WIDTH - 1: {
                    final int minY = columnMinY[x - 1];
                    for (int y = columnMinY[x] - 1; y >= minY; --y) {
                        if (playfield2[y][x - 1]) {
                            ++wellCells;
                        }
                    }
                    break;
                }
                default: {
                    final int minY = min(columnMinY[x - 1], columnMinY[x + 1]);
                    for (int y = columnMinY[x] - 1; y >= minY; --y) {
                        if (playfield2[y][x - 1] && playfield2[y][x + 1]) {
                            ++wellCells;
                        }
                    }
                    break;
                }
            }
            totalWellCells += wellCells;
            if (wellCells >= 3) {
                ++totalDeepWells;
            }
        }

        int totalColumnHoles = 0;
        int totalWeightedColumnHoles = 0;
        int totalColumnTransitions = 0;
        int totalColumnHoleDepths = 0;
        int minColumnHoleDepth = PLAYFIELD_HEIGHT;
        int maxColumnHoleDepth = 0;
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            final int minY = columnMinY[x] + 1;
            for (int y = PLAYFIELD_HEIGHT - 1; y >= minY; --y) {
                final boolean oy = playfield2[y][x];
                final boolean my = playfield2[y - 1][x];
                if (!oy && my) {
                    ++totalColumnHoles;
                    totalWeightedColumnHoles += y + 1;
                    final int depth = y - columnMinY[x];
                    totalColumnHoleDepths += depth;
                    if (depth < minColumnHoleDepth) {
                        minColumnHoleDepth = depth;
                    }
                    if (depth > maxColumnHoleDepth) {
                        maxColumnHoleDepth = depth;
                    }
                }
                if (oy != my) {
                    ++totalColumnTransitions;
                }
            }
        }

        int totalRowTransitions = 0;
        for (int y = PLAYFIELD_HEIGHT - 1; y >= minColumnY; --y) {
            final boolean[] row = playfield2[y];
            if (!row[0]) {
                ++totalRowTransitions;
            }
            if (!row[PLAYFIELD_WIDTH - 1]) {
                ++totalRowTransitions;
            }
            for (int x = PLAYFIELD_WIDTH - 1; x >= 1; --x) {
                if (row[x] != row[x - 1]) {
                    ++totalRowTransitions;
                }
            }
        }

        final int pileHeight = PLAYFIELD_HEIGHT - minColumnY;

        int totalSolidCells = 0;
        int totalWeightedSolidCells = 0;
        for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            final int minY = columnMinY[x];
            for (int y = PLAYFIELD_HEIGHT - 1; y >= minY; --y) {
                if (playfield2[y][x]) {
                    ++totalSolidCells;
                    totalWeightedSolidCells += PLAYFIELD_HEIGHT - y;
                }
            }
        }

        int columnHeightVariance = 0;
        for (int x = PLAYFIELD_WIDTH - 1; x >= 1; --x) {
            columnHeightVariance += abs(columnMinY[x] - columnMinY[x - 1]);
        }

        double score
                = weights[0] * totalLinesCleared
                + weights[1] * totalLockHeight
                + weights[2] * totalWellCells
                + weights[3] * totalDeepWells
                + weights[4] * totalColumnHoles
                + weights[5] * totalWeightedColumnHoles
                + weights[6] * totalColumnHoleDepths
                + weights[7] * minColumnHoleDepth
                + weights[8] * maxColumnHoleDepth
                + weights[9] * totalColumnTransitions
                + weights[10] * totalRowTransitions
                + weights[11] * totalColumnHeights
                + weights[12] * pileHeight
                + weights[13] * columnHeightSpread
                + weights[14] * totalSolidCells
                + weights[15] * totalWeightedSolidCells
                + weights[16] * columnHeightVariance;

        for (int y = min(6, PLAYFIELD_HEIGHT - 1); y >= minColumnY; --y) {
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                if (playfield2[y][x]) {
                    score += 7 - y;
                }
            }
        }

        if (linesCleared1 == 4 || linesCleared2 == 4) {
            score -= 1.0E9;
        }

        if (score < bestScore) {
            bestFound = true;
            bestScore = score;
            bestX = x1;
            bestY = y1;
            bestO = o1;
        }
    }
}
