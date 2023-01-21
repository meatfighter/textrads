package textrads.netplay;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientSocketHandler {
        
    private final Client client;
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    private final Pipe outPipe = new Pipe();
    private final Thread outPipeThread;
    
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
                    final Buffer pipe = outPipe.getReader();
                    out.write(pipe.getData(), pipe.getReadIndex(), pipe.getSize());
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
            while (true) {
                synchronized (monitor) {
                    if (!running || cancelled) {
                        break;
                    }
                }                
                try {
                    final Buffer pipe = inPipe.borrowWriter();
                    pipe.incrementWriteIndex(in.read(pipe.getData(), pipe.getWriteIndex(), pipe.getMaxWriteLength()));
                } catch (final IOException ignored) {
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
