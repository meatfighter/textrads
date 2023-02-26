package textrads;

import textrads.db.ExtendedRecord;
import textrads.util.GraphicsUtil;

public class ExtendedRecordDifficultyFormatter extends AbstractRecordFormatter<ExtendedRecord> {

    @Override
    public String getHeaders() {
        return "Rank  Initials  Difficulty  Starting Level  Completion Time      Score";
    }    
    
    @Override
    public String format(final int place, final ExtendedRecord record) {
        return String.format("%4s     %s    %10d  %14d  %15s  %9d", PLACES[place], record.getInitials(), 
                record.getChallenge(), record.getLevel(), GraphicsUtil.formatTime(record.getTime()), record.getScore());
    }    
}
