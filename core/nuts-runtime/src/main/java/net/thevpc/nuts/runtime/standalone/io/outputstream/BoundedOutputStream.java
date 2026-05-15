package net.thevpc.nuts.runtime.standalone.io.outputstream;

import java.io.ByteArrayOutputStream;

public class BoundedOutputStream extends ByteArrayOutputStream {
    private final int maxBytes;
    private final int maxLines;
    private int lineCount;
    private boolean truncated;
    private boolean lastWasCR;

    public BoundedOutputStream(int maxBytes, int maxLines) {
        this.maxBytes = maxBytes;
        this.maxLines = maxLines;
        this.lineCount = 0;
        this.truncated = false;
        this.lastWasCR = false;
    }

    @Override
    public synchronized void write(int b) {
        if (truncated) return;
        if (maxBytes > 0 && count >= maxBytes) {
            truncated = true;
            return;
        }
        if (maxLines > 0) {
            if (b == '\n') {
                // \r\n counts as one line, \n alone counts as one line
                if (!lastWasCR) lineCount++;
                lastWasCR = false;
                if (lineCount >= maxLines) {
                    super.write(b);
                    truncated = true;
                    return;
                }
            } else if (b == '\r') {
                lineCount++;
                lastWasCR = true;
                if (lineCount >= maxLines) {
                    super.write(b);
                    truncated = true;
                    return;
                }
            } else {
                lastWasCR = false;
            }
        }
        super.write(b);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) {
        if (truncated) return;
        int end = off + len;
        for (int i = off; i < end && !truncated; i++) {
            write(b[i] & 0xFF);
        }
    }

    public boolean isTruncated() {
        return truncated;
    }

    public int getLineCount() {
        return lineCount;
    }

    @Override
    public synchronized String toString() {
        return super.toString();
    }

    public synchronized String toString(String charset) throws java.io.UnsupportedEncodingException {
        return super.toString(charset);
    }

    public synchronized String toStringWithSummary() {
        String s = super.toString();
        if (truncated) {
            s += "\n... [truncated: " + count + " bytes, " + lineCount + " lines]";
        }
        return s;
    }
}
