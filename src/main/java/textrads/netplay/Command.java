package textrads.netplay;

public interface Command {
    byte HEARTBEAT = 0;
    byte STATE = 1;
    byte EVENTS = 2;
}
