package textrads.attractmode;

import java.util.List;
import textrads.Textrads;
import textrads.db.Record;
import textrads.db.RecordList;

public class RecordsState {
    
    private static final double FLASHES_PER_SECOND = 9.0;
    private static final float FLASHES_PER_FRAME = (float) (FLASHES_PER_SECOND / Textrads.FRAMES_PER_SECOND);
    
    private final String[] recordStrings = new String[RecordList.COUNT];

    private String title;
    private String headers;

    private int flashIndex;
    private float flashCounter;
    
    public void init(final String title, final RecordList<? super Record> recordList, 
            final AbstractRecordFormatter formatter) {
        
        this.title = title;
        headers = formatter.getHeaders();
        final List<Record> records = recordList.getRecords();
        for (int i = 0; i < RecordList.COUNT; ++i) {
            recordStrings[i] = formatter.format(i, records.get(i));
        }
        
        flashIndex = RecordList.COUNT;
        flashCounter = 1f;
    }
    
    public String[] getRecordStrings() {
        return recordStrings;
    }

    public String getTitle() {
        return title;
    }

    public String getHeaders() {
        return headers;
    }
    
    public void update() {
        flashCounter -= FLASHES_PER_FRAME;
        if (flashCounter <= 0f) {
            flashCounter += 1f;
            if (++flashIndex > RecordList.COUNT) {
                flashIndex = 0;
            } 
        }
    }
    
    public int getFlashIndex() {
        return flashIndex;
    }
}
