package textrads.ai;

// Flood fills the playfield starting from the Tetrimino seed point to

import textrads.MonoGameState;
import static textrads.ai.Playfield.copy;
import static textrads.ai.Playfield.createPlayfield;

// determine if it is still possible to clear lines.  It is based on 
// Paul S. Heckbert's A SEED FILL ALGORITHM in Graphics Gems.
public class SeedFiller {

    public static final int STACK_CAPACITY = 256;

    // Filled horizontal segment of scanline y for xl <= x <= xr.
    // Parent segment was on line y - dy, where dy = 1 or -1.
    private static final class Segment {

        public int y;
        public int xl;
        public int xr;
        public int dy;
    }

    private final Segment[] stack = new Segment[STACK_CAPACITY];
    private final boolean[][] playfield;

    private int sp;     // Stack pointer

    public SeedFiller(final int height) {
        playfield = createPlayfield();
        for (int i = stack.length - 1; i >= 0; --i) {
            stack[i] = new Segment();
        }
    }

    private boolean push(final int y, final int xl, final int xr, final int dy) {

        // Halt early if a full row of the playfield is filled.
        if (xr - xl + 1 == MonoGameState.PLAYFIELD_WIDTH) {
            return true;
        }

        if (y + dy >= 0 && y + dy < playfield.length) {
            final Segment segment = stack[sp++];
            segment.y = y;
            segment.xl = xl;
            segment.xr = xr;
            segment.dy = dy;
        }

        return false; // It did not fill a full row.
    }

    private Segment pop() {
        return stack[--sp];
    }

    // false - it is absolutely impossible to clear additional lines; 
    // i.e., no set of moves will avoid Game Over.  
    // 
    // true - it may be possible to clear additiona lines, but not necessarily.
    public boolean canClearMoreLines(final boolean[][] playfield) {

        // If the first row is empty, there is no need to do a flood fill.
        if (isFirstRowEmpty(playfield)) {
            return true;
        }

        // Do the flood fill.
        copy(playfield, this.playfield);
        if (fill(MonoGameState.SPAWN_X, MonoGameState.SPAWN_Y)) {
            return true; // The fill halted early after detecting a full row.
        }

        // Check if the flood fill populated any full rows.
        outer:
        for (int y = 0; y < playfield.length; ++y) {
            final boolean[] row = this.playfield[y];
            for (int x = MonoGameState.PLAYFIELD_WIDTH - 1; x >= 0; --x) {
                if (!row[x]) {
                    continue outer;
                }
            }
            return true;
        }

        return false;
    }

    private boolean isFirstRowEmpty(final boolean[][] playfield) {
        final boolean[] row = playfield[0];
        for (int x = MonoGameState.PLAYFIELD_WIDTH - 1; x >= 0; --x) {
            if (row[x]) {
                return false;
            }
        }
        return true;
    }

    // Halts early and returns true if it filled a full row (see push).
    private boolean fill(int x, int y) {

        if (playfield[y][x] || x < 0 || x >= MonoGameState.PLAYFIELD_WIDTH || y < 0 
                || y >= MonoGameState.PLAYFIELD_HEIGHT) {
            return false;
        }

        sp = 0;

        push(y, x, x, 1); // needed in some cases
        push(y + 1, x, x, -1); // seed segment (popped first)

        while (sp > 0) {
            // pop segment off stack and fill a neighboring scan line
            final Segment s = pop();
            final int dy = s.dy;
            y = s.y + dy;
            final int xl = s.xl;
            final int xr = s.xr;

            final boolean[] row = playfield[y];

            // segment of scan line y - dy for xl <= x <= xr was previously filled,
            // now explore adjacent pixels in scan line y
            x = xl;
            while (x >= 0 && !row[x]) {
                playfield[y][x--] = true;
            }

            int left;
            if (x >= xl) {
                for (++x; x <= xr && row[x]; ++x) {                    
                }
                left = x;
                if (x > xr) {
                    continue;
                }
            } else {
                left = x + 1;
                if (left < xl && push(y, left, xl - 1, -dy)) { // leak on left? 
                    return true;
                }
                x = xl + 1;
            }

            do {
                while (x < MonoGameState.PLAYFIELD_WIDTH && !row[x]) {
                    playfield[y][x++] = true;
                }

                if (push(y, left, x - 1, dy)
                        || (x > xr + 1 && push(y, xr + 1, x - 1, -dy))) { // leak on right?
                    return true;
                }

                for (++x; x <= xr && row[x]; ++x) {                    
                }
                left = x;
            } while (x <= xr);
        }

        return false;
    }
}
