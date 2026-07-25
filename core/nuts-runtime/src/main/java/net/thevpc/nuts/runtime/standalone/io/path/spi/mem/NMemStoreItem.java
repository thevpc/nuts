package net.thevpc.nuts.runtime.standalone.io.path.spi.mem;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NMemStoreItem {
    NMemoryPathStore store;
    boolean dir;
    String name;
    String path;
    NMemStoreItem parent;
    final List<NMemStoreItem> children = new ArrayList<>();
    // --- content storage ---
    private byte[] data = new byte[0];
    private int size = 0;
    private long lastModified = System.currentTimeMillis();


    public NMemStoreItem(boolean dir, String name, NMemStoreItem parent, NMemoryPathStore store) {
        this.dir = dir;
        this.store = store;
        if (name.isEmpty()) {
            if (parent != null) {
                throw new NIllegalArgumentException(NMsg.ofC("empty name"));
            }
            this.name = "";
            this.path = "/";
            this.parent = null;
        } else {
            this.name = name;
            this.path = parent == null ? ("/" + name) :
                    parent.path.equals("/") ? ("/" + name) :
                    (parent.path + "/" + name);
            this.parent = parent;
        }
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public NMemStoreItem child(String name) {
        for (NMemStoreItem child : children) {
            if (child.name.equals(name)) {
                return child;
            }
        }
        return null;
    }

    public synchronized int read(long pos, byte[] buffer, int offset, int len) {
        if (dir) {
            throw new IllegalStateException("cannot read directory: " + path);
        }
        if (pos < 0) {
            throw new IllegalArgumentException("negative position: " + pos);
        }
        if (pos >= size) {
            return -1; // EOF
        }
        int n = (int) Math.min(len, size - pos);
        System.arraycopy(data, (int) pos, buffer, offset, n);
        return n;
    }

    public synchronized int write(long pos, byte[] buffer, int offset, int len) {
        if (dir) {
            throw new IllegalStateException("cannot write directory: " + path);
        }
        if (pos < 0) {
            throw new IllegalArgumentException("negative position: " + pos);
        }
        int end = (int) (pos + len);
        ensureCapacity(end);
        System.arraycopy(buffer, offset, data, (int) pos, len);
        if (end > size) {
            size = end;
        }
        lastModified = System.currentTimeMillis();
        return len;
    }

    public synchronized void reset() {
        size = 0;
        data = new byte[0];
        lastModified = System.currentTimeMillis();
    }

    public synchronized void truncate(long newSize) {
        if (newSize < 0) {
            throw new IllegalArgumentException("negative size: " + newSize);
        }
        if (newSize < size) {
            // zero out the tail so stale bytes aren't exposed by a later grow
            Arrays.fill(data, (int) newSize, size, (byte) 0);
        } else {
            ensureCapacity((int) newSize);
        }
        size = (int) newSize;
        lastModified = System.currentTimeMillis();
    }

    public long lastModified() {
        return lastModified;
    }

    public synchronized long size() {
        return size;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > data.length) {
            int newCapacity = Math.max(data.length * 2, minCapacity);
            data = Arrays.copyOf(data, newCapacity);
        }
    }

    /**
     * returns unsigned byte value 0..255, or -1 at/after EOF
     */
    public synchronized int read(long pos) {
        if (dir) {
            throw new IllegalStateException("cannot read directory: " + path);
        }
        if (pos < 0) {
            throw new IllegalArgumentException("negative position: " + pos);
        }
        if (pos >= size) {
            return -1; // EOF
        }
        return data[(int) pos] & 0xFF;
    }

    /**
     * writes a single byte at pos, growing the file if needed
     */
    public synchronized int write(long pos, byte v) {
        if (dir) {
            throw new IllegalStateException("cannot write directory: " + path);
        }
        if (pos < 0) {
            throw new IllegalArgumentException("negative position: " + pos);
        }
        int end = (int) (pos + 1);
        ensureCapacity(end);
        data[(int) pos] = v;
        if (end > size) {
            size = end;
        }
        lastModified = System.currentTimeMillis();
        return 1;
    }
}
