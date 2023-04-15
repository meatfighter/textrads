package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import textrads.app.Textrads;
import textrads.util.ThreadUtil;

public class SocketHandler {
    
    private static final long MAX_WAIT_NANOS = Math.round(1.0E9 / Textrads.FRAMES_PER_SECOND);
    
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

    private final Queue outQueue = new Queue(1);
    private final Thread outQueueThread;
    
    private final Queue inQueue = new Queue();
    private final Thread inQueueThread;
    
    private final Object stateMonitor = new Object();
    private State state = State.NEW;
    
    public SocketHandler(final Socket socket, final TerminatedListener terminatedListener) throws IOException {        
        this.socket = socket;
        this.terminatedListener = terminatedListener;
        
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
            final Socket s = socket;
            if (s != null) {
                s.close();
            }
        } catch (final IOException ignored) {            
        }
    } 

    public QueueElement getWriteElement() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return null;
            }
        }
        
        if (outQueue.isFull()) {
            stop();
            return null;
        }      
        
        return outQueue.getWriteElement();
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
    
    public int waitForData() {
        
        final long startTime = System.nanoTime();
        while (true) {
            
            synchronized (stateMonitor) {
                if (state != State.RUNNING) {
                    return 0;
                }
            }
            
            if (!inQueue.isEmpty()) {
                return inQueue.size();
            }            
            
            final long remainingNanos = MAX_WAIT_NANOS - (System.nanoTime() - startTime);
            if (remainingNanos <= 0L) {
                return 0;
            }
            
            try {            
                inQueue.waitForData(remainingNanos / 1_000_000L);
            } catch (final InterruptedException ignored) {
            }
        }
    }

    public QueueElement getReadElement() {
        
        synchronized (stateMonitor) {
            if (state != State.RUNNING) {
                return null;
            }
        }
        
        return inQueue.getReadElement();
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
                try {
                    if (outQueue.isEmpty()) {
                        outQueue.waitForData(Server.HEARTBEAT_PERIOD);
                    }
                    if (outQueue.isEmpty()) {
                        out.write(QueueElement.Type.HEARTBEAT);
                    } else {
                        outQueue.getReadElement().write(out);        
                        outQueue.incrementReadIndex();
                    }
                    out.flush();
                } catch (final InterruptedException ignored) {
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
            terminatedListener.handleTerminated(this);
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
                    if (type == QueueElement.Type.HEARTBEAT) {
                        continue;
                    }
                    if (inQueue.isFull()) {
                        break;
                    }
                    inQueue.getWriteElement().read(in, type);
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
