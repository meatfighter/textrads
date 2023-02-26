package textrads;

import textrads.db.Record;

public abstract class AbstractRecordFormatter<T extends Record> {
    
    protected static final String[] PLACES = { "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th", "10th" };
        
    public abstract String getHeaders();
    public abstract String format(int place, T record);
}
