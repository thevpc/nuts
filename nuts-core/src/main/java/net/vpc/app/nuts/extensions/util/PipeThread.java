package net.vpc.app.nuts.extensions.util;

import java.io.IOException;
import java.io.OutputStream;

public class PipeThread extends Thread implements StopMonitor {

    private final NutsNonBlockingInputStream in;
    private final OutputStream out;
    private final Object lock = new Object();
    private long pipedBytesCount = 0;
    private boolean requestStop = false;
    private boolean stopped = false;

    public PipeThread(String name, NutsNonBlockingInputStream in, OutputStream out) {
        super(name);
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean shouldStop() {
        return requestStop;
    }

    public void requestStop() {
        requestStop = true;
        if (!stopped) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
//            for (int i = 0; i < 100; i++) {
//                if (stopped) {
//                    return;
//                }
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    //e.printStackTrace();
//                }
//            }
//            throw new RuntimeException("Unable to stop");
    }

    @Override
    public void run() {
        try {
//            CoreIOUtils.copy(in, out, false, false, this);

            byte[] bytes = new byte[10240];
            int count;
            while (true) {
                if (this.shouldStop()) {
                    break;
                }
                if (in.hasMoreBytes()) {
                    count = in.readNonBlocking(bytes, 500);
                    if(count>0) {
                        pipedBytesCount += count;
                        out.write(bytes, 0, count);
                    }
//                        System.out.println("push "+count);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        stopped = true;
        synchronized (lock) {
            lock.notify();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public NutsNonBlockingInputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }
}
