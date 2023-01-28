package textrads.ai;

import textrads.Offset;
import static textrads.MonoGameState.PLAYFIELD_HEIGHT;
import static textrads.MonoGameState.PLAYFIELD_WIDTH;
import textrads.Tetromino;
import static textrads.Tetromino.TETROMINOES;

public interface Playfield {

    static boolean[][] createPlayfield() {
        return new boolean[PLAYFIELD_HEIGHT][PLAYFIELD_WIDTH];
    }

    static void clearPlayfield(final boolean[][] playfield) {
        for (int y = PLAYFIELD_HEIGHT; y >= 0; --y) {
            for (int x = PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                playfield[y][x] = false;
            }
        }
    }

    static void copy(final boolean[][] sourcePlayfield, final boolean[][] destinationPlayfield) {

        if (sourcePlayfield != destinationPlayfield) {
            for (int i = PLAYFIELD_HEIGHT - 1; i >= 0; --i) {
                System.arraycopy(sourcePlayfield[i], 0, destinationPlayfield[i], 0, PLAYFIELD_WIDTH);
            }
        }
    }

    static int lock(final boolean[][] playfield, final int type, final int x,
            final int y, final int orientation) {
        return lock(playfield, playfield, type, x, y, orientation);
    }

    static int lock(final boolean[][] sourcePlayfield, final boolean[][] destinationPlayfield, final int type, 
            final int x, final int y, final int orientation) {

        int clearedLines = 0;

        copy(sourcePlayfield, destinationPlayfield);

        final Tetromino tetromino = TETROMINOES[type][orientation];
        final Offset[] offsets = tetromino.offsets;
        for (int i = 3; i >= 0; --i) {
            final Offset offset = offsets[i];
            final int Y = y + offset.y;
            if (Y >= 0) {
                destinationPlayfield[Y][x + offset.x] = true;
            }
        }
        outer:
        for (int i = tetromino.minOffsetY; i <= tetromino.maxOffsetY; ++i) {
            final int Y = y + i;
            if (Y >= 0) {
                for (int j = PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                    if (!destinationPlayfield[Y][j]) {
                        continue outer;
                    }
                }
                for (int j = Y; j >= 1; --j) {
                    System.arraycopy(destinationPlayfield[j - 1], 0, destinationPlayfield[j], 0, PLAYFIELD_WIDTH);
                }
                for (int j = PLAYFIELD_WIDTH - 1; j >= 0; --j) {
                    destinationPlayfield[0][j] = false;
                }
                ++clearedLines;
            }
        }

        return clearedLines;
    }
}
