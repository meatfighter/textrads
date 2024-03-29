package textrads.netplay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import textrads.app.Textrads;
import textrads.util.IOUtil;
import textrads.util.ThreadUtil;

public class MessageChannel {
    
    private static final long WAIT_MILLIS = Math.round(1000.0 / Textrads.FRAMES_PER_SECOND);
    
    public static final long HEARTBEAT_MILLIS = TimeUnit.SECONDS.toMillis(10);
    
    private static final int DEFAULT_OUT_QUEUE_PLAYERS = 2;
    
    private static final String HANDSHAKE_STR = "Textrads";
    
    public static enum HandshakeStatus {
        PENDING,
        SUCCESS,
        FAIL,
    }
    
    private static enum State {
        NEW,
        RUNNING,
        CANCELLED,
        TERMINATED,
    }
    
    private final Socket socket;
    private final StatusListener statusListener;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final MessageQueue outQueue;
    private final Thread outQueueThread;
    
    private final MessageQueue inQueue;
    private final Thread inQueueThread;
    
    private final Object monitor = new Object();
    private State state = State.NEW;

    private HandshakeStatus handshakeStatus = HandshakeStatus.PENDING;
    
    public MessageChannel(final Socket socket, final InputStream in, final OutputStream out) {
        this(socket, in, out, null, DEFAULT_OUT_QUEUE_PLAYERS);
    }
    
    public MessageChannel(final Socket socket, final InputStream in, final OutputStream out, 
            final StatusListener statusListener) {
        this(socket, in, out, statusListener, DEFAULT_OUT_QUEUE_PLAYERS);
    }
    
    public MessageChannel(final Socket socket, final InputStream in, final OutputStream out,
            final StatusListener statusListener, final int outPlayers) {        

        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(in));
        this.out = new DataOutputStream(new BufferedOutputStream(out));
        this.statusListener = statusListener;
        
        inQueue = new MessageQueue();
        outQueue = new MessageQueue(outPlayers);
        
        outQueueThread = new Thread(this::runOutQueue);
        inQueueThread = new Thread(this::runInQueue);
    }
    
    public void start() {
        
        synchronized (monitor) {
            if (state != State.NEW) {
                return;
            }
            state = State.RUNNING;            
            outQueue.start();
            inQueue.start();
            outQueueThread.start();
            inQueueThread.start();

            sendHandshake();
        }
    }
    
    public void stop() {
        synchronized (monitor) {
            if (state != State.RUNNING) {
                return;
            }
            state = State.CANCELLED;
        }
        
        IOUtil.close(socket);
        outQueue.stop();
        inQueue.stop();
        ThreadUtil.interrupt(outQueueThread);
        ThreadUtil.interrupt(inQueueThread);
    }
    
    private void sendHandshake() {
        try {
            final Message message = outQueue.getWriteMessage();
            message.setType(Message.Type.HANDSHAKE);
            message.setData(IOUtil.toByteArray(HANDSHAKE_STR));
            outQueue.incrementWriteIndex();
        } catch (final IOException ignored) {
            stop();
        }
    }
    
    public void write(final byte type) {
        write(type, null);
    }
    
    public void write(final byte type, final Serializable obj) {
        final Message message = getWriteMessage();
        if (message == null) {
            stop();
            return;
        }
        message.setType(type);
        if (obj != null) {
            try {
                message.setData(IOUtil.toByteArray(obj));
            } catch (final IOException ignored) {
                stop();
                return;
            }
        }
        incrementWriteIndex();
    }    

    public Message getWriteMessage() {
        
        synchronized (monitor) {
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
        
        synchronized (monitor) {
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
    
    public int getAvailableMessages() {
        synchronized (monitor) {
            if (state != State.RUNNING) {
                return 0;
            }
        }
        
        return inQueue.size();
    }
    
    public int waitForMessage() {
        
        synchronized (monitor) {
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
        
        synchronized (monitor) {
            if (state != State.RUNNING) {
                return null;
            }
        }
        
        return inQueue.getReadMessage();
    }
        
    public void incrementReadIndex() {
        
        synchronized (monitor) {
            if (state != State.RUNNING) {
                return;
            }
        }        
        
        inQueue.incrementReadIndex();
    }
    
    public boolean isRunning() {
        synchronized (monitor) {
            return state == State.RUNNING;
        }
    }
    
    public boolean isTerminated() {
        synchronized (monitor) {
            return state == State.TERMINATED;
        }
    }
        
    private void runOutQueue() {
        try {
            while (true) {
                synchronized (monitor) {
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
            synchronized (monitor) {
                state = State.TERMINATED;
            }
            if (statusListener != null) {
                statusListener.statusChanged(this);
            }
        }                
    }
    
    private void runInQueue() {        
        try {
            while (true) {                
                synchronized (monitor) {
                    if (state != State.RUNNING) {
                        return;
                    }
                }                      
                try {
                    final byte type = in.readByte();
                    switch (type) {
                        case Message.Type.HEARTBEAT:
                            continue;
                        case Message.Type.HANDSHAKE: {
                            if (HANDSHAKE_STR.equals(IOUtil.fromByteArray(IOUtil.readByteArray(in)))) {
                                synchronized (monitor) {
                                    handshakeStatus = HandshakeStatus.SUCCESS;
                                }
                                if (statusListener != null) {
                                    statusListener.statusChanged(this);
                                }
                            } else {
                                synchronized (monitor) {
                                    handshakeStatus = HandshakeStatus.FAIL;
                                }
                                if (statusListener != null) {
                                    statusListener.statusChanged(this);
                                }
                                return;
                            }
                            continue;
                        }
                    }
                    if (inQueue.isFull()) {
                        break;
                    }
                    inQueue.getWriteMessage().read(in, type);
                    inQueue.incrementWriteIndex();   
                } catch (final IOException | ClassNotFoundException ignored) {
                    break;
                }                                
            }
        } finally {
            stop();
        }
    }

    public HandshakeStatus getHandshakeStatus() {
        synchronized (monitor) {
            return handshakeStatus;
        }
    }
}
