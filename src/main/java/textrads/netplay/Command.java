package textrads.netplay;

public interface Command {
    byte END = -1;
    byte HEARTBEAT = -2;
    byte EVENTS = -3;
}
