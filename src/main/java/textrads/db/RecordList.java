package textrads.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class RecordList<T extends Record> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final int COUNT = 10;
    public static final long EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(1);
    
    public static final Supplier<RecordList> RECORD_LIST_SUPPLIER = () -> new RecordList(Record.RECORD_MAKER);
    
    public static final Supplier<RecordList> EXTENDED_RECORD_LIST_SUPPLIER 
            = () -> new RecordList(ExtendedRecord.RECORD_MAKER);
    
    public static final Function<RecordList, RecordList> TODAYS_INITIALIZATION_TASK 
            = recordList -> recordList.removeExpired(); 
    
    private final RecordMaker<T> recordMaker;
    private final List<T> records;
    
    private RecordList(final RecordMaker<T> recordMaker) {
        this(recordMaker, new ArrayList<>());
        for (int i = 0; i < COUNT; ++i) {
            records.add(recordMaker.make(i));
        }
    }
    
    private RecordList(final RecordMaker<T> recordMaker, final List<T> records) {
        this.recordMaker = recordMaker;
        this.records = records;
    }
    
    public RecordList insert(final int index, final T record) {
        final List<T> rs = new ArrayList<>(records);
        rs.add(index, record);
        rs.remove(rs.size() - 1);
        return new RecordList(recordMaker, rs);
    }
    
    public int findIndex(final T record) {
        for (int i = 0; i < COUNT; ++i) {
            if (record.compareTo(records.get(i)) < 0) {
                return i;
            }
        }
        return COUNT;
    }
    
    public RecordList removeExpired() {
        
        if (!findExpired()) {
            return this;
        }
        
        final long now = System.currentTimeMillis();
        final List<T> rs = new ArrayList<>(records);
        for (final Iterator<T> i = rs.iterator(); i.hasNext(); ) {
            final T record = i.next();
            if (now - record.getTimestamp() > EXPIRATION_MILLIS) {
                i.remove();
            }
        }
        for (int i = 0; i < COUNT; ++i) {
            rs.add(recordMaker.make(i));
        } 
        Collections.sort(rs);
        while (rs.size() > COUNT) {
            rs.remove(rs.size() - 1);
        }
        return new RecordList(recordMaker, rs);
    }
    
    private boolean findExpired() {
        final long now = System.currentTimeMillis();
        for (final T record : records) {
            if (now - record.getTimestamp() > EXPIRATION_MILLIS) {
                return true;
            }
        }
        return false;
    }
}
