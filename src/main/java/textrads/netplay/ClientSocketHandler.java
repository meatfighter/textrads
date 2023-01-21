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
            while (true) {
                final Byte event = InputEventSource.poll();
                if (event == null) {
                    break;
                }
                if (!wroteEvents) {
                    wroteEvents = true;
                    buffer.write(Command.EVENTS);
                }
            }
            if (wroteEvents) {
                buffer.write(Command.END);
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
                try {
                    if (inBuffer.getMaxWriteLength() <= 0) {
                        break;
                    }
                    final byte[] inBufferData = inBuffer.getData();
                    inBuffer.incrementWriteIndex(in.read(inBufferData, inBuffer.getWriteIndex(), 
                            inBuffer.getMaxWriteLength()));
                    for (int i = inBuffer.getWriteIndex() - 1, end = inBuffer.getReadIndex(); i >= end; --i) {
                        if (inBufferData[i] == Command.END) {
                            Buffer writer = null;
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
                            break;
                        }
                    }
                    inBuffer.setReadIndex(inBuffer.getWriteIndex());   
                } catch (final IOException e) {
                    break;
                } finally {
                    inPipe.returnWriter();
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
