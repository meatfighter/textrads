package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import textrads.InputEventSource;

public class ClientSocketHandler {
           
    private final Client client;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    private final Pipe outPipe = new Pipe();
    private final Thread outPipeThread;
    
    private final Buffer inBuffer = new Buffer();
    private final Pipe inPipe = new Pipe();
    private final Thread inPipeThread;
    
    private final Object monitor = new Object();
    private boolean running;
    private boolean cancelled;
    
    private boolean handlingEvents;
    
    public ClientSocketHandler(final Client client, final Socket socket) throws IOException {
        this.client = client;
        this.socket = socket;
        in = socket.getInputStream();
        out = socket.getOutputStream();
        outPipeThread = new Thread(this::runOutPipe);
        inPipeThread = new Thread(this::runInPipe);
    }
    
    public void start() {
        
        synchronized (monitor) {
            if (running) {
                return;
            }
            running = true;
        }
        
        outPipeThread.start();
        inPipeThread.start();
    }
    
    public void stop() {
        
        synchronized (monitor) {
            if (!running || cancelled) {
                return;
            }
            cancelled = true;
        }
        
        closeSocket();
        outPipeThread.interrupt();
        inPipeThread.interrupt();
    }
    
    public void update() {
        writeEvents();
        readEvents();
    }
    
    private void readEvents() {
        try {
            Buffer reader = null;
            do {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        return;
                    }
                }
                try {
                    reader = inPipe.getReader();
                } catch (final InterruptedException ignored) {                    
                }
            } while (reader == null);
            
            final byte[] data = reader.getData();
            while (true) {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        return;
                    }
                }
                
                if (reader.isEmpty()) {
                    break;
                }
                                
                final int startIndex;
                outer: {
                    for (int i = reader.getReadIndex(), end = reader.getWriteIndex(); i < end; ++i) {
                        if (data[i] != Command.HEARTBEAT) {
                            startIndex = i;
                            break outer;
                        }
                    }
                    throw new IOException("No start.");
                }
                
                final int endIndex;
                outer: {
                    for (int i = startIndex + 1, end = reader.getWriteIndex(); i < end; ++i) {
                        if (data[i] == Command.END) {
                            endIndex = i;
                            break outer;
                        }                   
                    }
                    throw new IOException("No end.");
                }
                
                switch (data[startIndex]) {
                    case Command.HEARTBEAT:
                        break;
                    case Command.EVENTS:
                        break;
                    default:
                        throw new IOException("Invalid command.");
                }
            }
        } catch (final IOException ignored) {
            stop();
        }
    }
    
    private void writeEvents() {
        Buffer buffer = null;
        try {
            do {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        return;
                    }
                } 
                try {
                    buffer = outPipe.borrowWriter();
                } catch (final InterruptedException ignored) {
                }
            } while (buffer == null);
            
            boolean wroteEvents = false;
            final OutputStream bufferOut = buffer.getOutputStream();
            while (true) {
                final Byte event = InputEventSource.poll();
                if (event == null) {
                    break;
                }
                if (!wroteEvents) {
                    wroteEvents = true;
                    bufferOut.write(Command.EVENTS);
                }
            }
            if (wroteEvents) {
                bufferOut.write(Command.END);
            }            
        } catch (final IOException ignored) {
            stop();
        } finally {
            if (buffer != null) {
                outPipe.returnWriter();
            }
        }
    }
    
    private void runOutPipe() {
        try {
            while (true) {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        break;
                    }
                } 
                try {
                    final Buffer buffer = outPipe.getReader();
                    out.write(buffer.getData(), buffer.getReadIndex(), buffer.getSize());
                    out.flush();
                } catch (final InterruptedException ignored) {
                } catch (final IOException ignored) {
                    break;
                }
            }
        } finally {
            stop();
        }                
    }
    
    private void runInPipe() {        
        try {
            outer: while (true) {
                
                synchronized (monitor) {
                    if (!running || cancelled) {
                        break;
                    }
                }       
                
                Buffer writer = null;
                try {
                    
                    // If inBuffer is out of room, break the connection.
                    final int maxLength = inBuffer.getMaxWriteLength();
                    if (maxLength <= 0) {
                        break;
                    }
                    
                    // Read available bytes into inBuffer.
                    final byte[] inBufferData = inBuffer.getData();
                    final int writeIndex = inBuffer.getWriteIndex();
                    inBuffer.incrementWriteIndex(in.read(inBufferData, writeIndex, maxLength));                    
                    if (writeIndex == inBuffer.getWriteIndex()) {
                        continue;
                    }
                    
                    // If inBuffer contains an end of block marker, append the block to the inPipe, and remove it.
                    for (int i = inBuffer.getWriteIndex() - 1; i >= writeIndex; --i) {
                        if (inBufferData[i] == Command.END) {                            
                            do {
                                synchronized (monitor) {
                                    if (!running || cancelled) {
                                        break outer;
                                    }
                                } 
                                try {
                                    writer = inPipe.borrowWriter();
                                } catch (final InterruptedException ignored) {                                    
                                }
                            } while (writer == null);
                            writer.write(inBuffer, 0, i + 1);
                            writer.shift();
                            continue outer;
                        }
                    }
                } catch (final IOException e) {
                    break;
                } finally {
                    if (writer != null) {
                        inPipe.returnWriter();
                    }
                }
            }
        } finally {
            stop();
        }
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
}
