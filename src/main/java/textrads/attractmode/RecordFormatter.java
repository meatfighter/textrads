package textrads.attractmode;

import textrads.db.Record;

public class RecordFormatter extends AbstractRecordFormatter<Record> {

    @Override
    public String getHeaders() {
        return "Rank  Initials      Score  Level";
    }
    
    @Override
    public String format(final int place, final Record record) {
        return String.format("%4s     %s    %9d  %5d", PLACES[place], record.getInitials(), record.getScore(), 
                record.getLevel());
    }    
}
