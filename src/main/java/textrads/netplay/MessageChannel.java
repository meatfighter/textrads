package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import textrads.app.Textrads;
import textrads.util.ThreadUtil;

public class MessageChannel {
    
    private static final long WAIT_MILLIS = Math.round(1000.0 / Textrads.FRAMES_PER_SECOND);
    
    public static final long HEARTBEAT_MILLIS = TimeUnit.SECONDS.toMillis(10);
    
    private static final int DEFAULT_OUT_QUEUE_PLAYERS = 2;
    
    private static enum State {
        NEW,
        RUNNING,
        CANCELLED,
        TERMINATED,
    }
    
    private final Socket socket;
    private final TerminatedListener terminatedListener;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final MessageQueue outQueue;
    private final Thread outQueueThread;
    
    private final MessageQueue inQueue;
    private final Thread inQueueThread;
    
    private final Object stateMonitor = new Object();
    private State state = State.NEW;
    
    public MessageChannel(final Socket socket) throws IOException {
        this(socket, null, DEFAULT_OUT_QUEUE_PLAYERS);
    }
    
    public MessageChannel(final Socket socket, final TerminatedListener terminatedListener) throws IOException {
        this(socket, terminatedListener, DEFAULT_OUT_QUEUE_PLAYERS);
    }
    
    public MessageChannel(final Socket socket, final TerminatedListener terminatedListener, final int outPlayers) 
            throws IOException {        
        
        this.socket = socket;
        this.terminatedListener = terminatedListener;
        
        inQueue = new MessageQueue();
        outQueue = new MessageQueue(outPlayers);
        
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        
        outQueueThread = new Thread(this::runOutQueue);
        inQueueThread = new Thread(this::runInQueue);
    }
    
    public void start() {
        
        synchronized (stateMonitor) {
            if (state != State.NEW) {
                return;
            }
            outQueue.start();        
            inQueue.start();
            outQueueThread.start();
            inQueueThread.start();
            state = State.RUNNING;
        }
    }
    
    public void stop() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
            state = State.CANCELLED;
        }
        
        closeSocket();
        outQueue.stop();
        inQueue.stop();
        ThreadUtil.interrupt(outQueueThread);
        ThreadUtil.interrupt(inQueueThread);
    }
    
    private void closeSocket() {
        try {   
            if (socket != null) {
                socket.close();
            }
        } catch (final IOException ignored) {            
        }
    } 

    public Message getWriteMessage() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return null;
            }
        }
        
        if (outQueue.isFull()) {
            stop();
            return null;
        }      
        
        return outQueue.getWriteMessage();
    }
    
    public void incrementWriteIndex() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
        }
        
        if (outQueue.isFull()) {
            stop();
            return;
        }
        
        outQueue.incrementWriteIndex();
    }
    
    public int waitForMessage() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return 0;
            }
        }        
                    
        if (inQueue.isEmpty()) {
            inQueue.waitForMessage(WAIT_MILLIS);            
        }            

        return inQueue.size();
    }

    public Message getReadMessage() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return null;
            }
        }
        
        return inQueue.getReadMessage();
    }
        
    public void incrementReadIndex() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return;
            }
        }        
        
        inQueue.incrementReadIndex();
    }
    
    public boolean isRunning() {
        synchronized (stateMonitor) {
            return state == State.RUNNING;
        }
    }
    
    public boolean isTerminated() {
        synchronized (stateMonitor) {
            return state == State.TERMINATED;
        }
    }
        
    private void runOutQueue() {
        try {
            while (true) {
                synchronized (stateMonitor) {
                    if (state != State.RUNNING) {
                        return;
                    }
                } 
                outQueue.waitForMessage(HEARTBEAT_MILLIS);
                try {
                    if (outQueue.isEmpty()) {
                        out.write(Message.Type.HEARTBEAT);
                    } else {
                        outQueue.getReadMessage().write(out);        
                        outQueue.incrementReadIndex();
                    }
                    out.flush();
                } catch (final IOException ignored) {
                    break;
                }
            }
        } finally {
            stop();            
            ThreadUtil.join(inQueueThread);
            synchronized (stateMonitor) {
                state = State.TERMINATED;
            }
            if (terminatedListener != null) {
                terminatedListener.handleTerminated(this);
            }
        }                
    }
    
    private void runInQueue() {        
        try {
            while (true) {                
                synchronized (stateMonitor) {
                    if (state != State.RUNNING) {
                        return;
                    }
                }                      
                try {
                    final byte type = in.readByte();
                    if (type == Message.Type.HEARTBEAT) {
                        continue;
                    }
                    if (inQueue.isFull()) {
                        break;
                    }
                    inQueue.getWriteMessage().read(in, type);
                    inQueue.incrementWriteIndex();   
                } catch (final IOException ignored) {
                    break;
                }                                
            }
        } finally {
            stop();
        }
    }
}
