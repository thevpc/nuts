package net.thevpc.nuts.ext.ssh;

import java.io.IOException;
import java.io.InputStream;

public abstract class DynamicInputStream extends InputStream {
    private final byte[] buffer;
    private int index;
    private int max;
    private boolean nomore;

    public DynamicInputStream(int bufferSize) {
        this.buffer = new byte[bufferSize <= 0 ? 1024 : bufferSize];
    }

    public int getAvailableCount() {
        return max - index;
    }

    @Override
    public int read() throws IOException {
        if (index < max) {
            return buffer[index++];
        }
        if (available() <= 0) {
            return -1;
        }
        return buffer[index++];
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (index >= max) {
            if (available() <= 0) {
                return -1;
            }
        }
        int v = Math.min(max - index, len);
        System.arraycopy(buffer, index, b, off, v);
        index += v;
        return v;
    }

    @Override
    public int available() throws IOException {
        int x = max - index;
        if (x <= 0) {
            if (nomore) {
                return 0;
            }
            if(!requestMore()){
                nomore=true;
            }
            x = max - index;
            if (x <= 0) {
                nomore = true;
                return 0;
            }
        }
        return x;
    }

    private void shrink() {
        if (index > 0) {
            int len = max - index;
            System.arraycopy(buffer, index, buffer, 0, len);
            index = 0;
            max = len;
        }
    }

    public int push(byte[] other) {
        return push(other,0,other.length);
    }

    public int push(byte[] other, int from, int count) {
        if (count + max < buffer.length) {
            System.arraycopy(other, from,buffer, max, count);
            max+=count;
            return count;
        } else {
            shrink();
            int count2 = Math.min(buffer.length - max, count);
            System.arraycopy(other, from, buffer, max, count2);
            max += count2;
            return count2;
        }
    }

    protected abstract boolean requestMore() throws IOException;
}
