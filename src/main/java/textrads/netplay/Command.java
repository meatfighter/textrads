package textrads.netplay;

public interface Command {
    byte HEARTBEAT = 0;
    byte GAME_STATE = 1;
    byte INPUT_EVENTS = 2;
}
