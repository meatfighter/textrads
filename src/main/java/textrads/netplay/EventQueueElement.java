package textrads.netplay;

import textrads.ByteList;

public class EventQueueElement {

    private final ByteList[] events;
    
    public EventQueueElement(final int players) {
        events = new ByteList[players];
        for (int i = players - 1; i >= 0; --i) {
            events[i] = new ByteList();
        }
    }
    
    public ByteList[] getEvents() {
        return events;
    }
    
    public ByteList getEvents(final int player) {
        return events[player];
    }
}
