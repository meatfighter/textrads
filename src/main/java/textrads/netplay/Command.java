package textrads.netplay;

public interface Command {
    byte END = 0;
    byte HEARTBEAT = 1;
    byte EVENTS = 2;
}
