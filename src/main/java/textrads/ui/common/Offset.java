package textrads.ui.common;

public class Offset {

    public final int x;
    public final int y;

    public Offset(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Offset(final int[] coordinates) {
        x = coordinates[0];
        y = coordinates[1];
    }
}