package textrads.netplay;

import textrads.ByteList;

public class EventQueueElement {
    
    private static final int DEFAULT_PLAYERS = 2;

    private final ByteList[] events;
    
    private byte[] data;
    
    public EventQueueElement() {
        this(DEFAULT_PLAYERS);
    }
    
    public EventQueueElement(final int players) {
        events = new ByteList[players];
        for (int i = players - 1; i >= 0; --i) {
            events[i] = new ByteList();
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }
    
    public ByteList[] getEvents() {
        return events;
    }
    
    public ByteList getEvents(final int player) {
        return events[player];
    }
    
    public void clear() {
        data = null;
        for (int i = events.length - 1; i >= 0; --i) {
            events[i].clear();
        }
    }
}
