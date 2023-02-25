package textrads.db;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

public class RecordList<T extends Record> {

    public static final int COUNT = 10;
    public static final long EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(1);
    
    private final RecordMaker<T> recordMaker;
    private final Comparator<T> comparator;
    private final T[] records;
    
    public RecordList(final RecordMaker<T> recordMaker, final Comparator<T> comparator) {
        this.recordMaker = recordMaker;
        this.comparator = comparator;
    }
}
