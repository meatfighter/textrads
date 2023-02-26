package textrads;

import textrads.db.ExtendedRecord;

public class ExtendedRecordDifficultyFormatter extends AbstractRecordFormatter<ExtendedRecord> {

    @Override
    public String format(final int place, final ExtendedRecord record) {
        //                    Rank  Initials  Difficulty  Starting Level  Completion Time  Score
        return String.format(" 1st  AAA               10              12     DDD:HH:MM:SS  %9d", record.getTime(), record.getScore());
    }    
}
