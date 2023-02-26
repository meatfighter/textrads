package textrads;

import static textrads.AbstractRecordFormatter.PLACES;
import textrads.db.ExtendedRecord;
import textrads.util.GraphicsUtil;

public class ExtendedRecordHeightFormatter extends AbstractRecordFormatter<ExtendedRecord> {

    @Override
    public String getHeaders() {
        return "Rank  Initials  Height  Starting Level  Completion Time      Score";
    }    
    
    @Override
    public String format(final int place, final ExtendedRecord record) {
        return String.format("%4s     %s    %6d  %14d  %15s  %9d", PLACES[place], record.getInitials(), 
                record.getChallenge(), record.getLevel(), GraphicsUtil.formatTime(record.getTime()), record.getScore());
    }
}
