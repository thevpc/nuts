package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPageStore;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NPageStoreFile implements NPageStore {
    private static final long MAGIC = 0x4E424C4B46494C45L; // "NBLKFILE"

    private final RandomAccessFile file;
    private final FileChannel channel;
    private final int pageSize;

    private long totalPages = 0;
    private long firstFreePage = -1;
    private final long[] userData = new long[6]; // index 1 to 5 to match userData1-5

    // Reusable buffers to eliminate garbage collector allocations
    private final ByteBuffer pointerBuf;
    private final ByteBuffer zeroPageBuf;
    private final ByteBuffer headerBuf;

    public NPageStoreFile(File f, int pageSize) throws IOException {
        boolean init = !f.exists() || f.length() == 0;
        this.file = new RandomAccessFile(f, "rw");
        this.channel = this.file.getChannel();
        this.pageSize = pageSize;

        this.pointerBuf = ByteBuffer.allocateDirect(8);
        this.zeroPageBuf = ByteBuffer.allocateDirect(pageSize);
        this.headerBuf = ByteBuffer.allocateDirect(pageSize);

        for (int i = 0; i < userData.length; i++) {
            userData[i] = -1;
        }
        userData[3] = 0; // Default size is 0

        if (init) {
            this.totalPages = 1; // page 0 is header page
            this.firstFreePage = -1;
            // Preallocate and write header page 0
            writeHeaderToBuffer(headerBuf);
            channel.write(headerBuf, 0);
        } else {
            readHeader();
        }
    }

    private void writeHeaderToBuffer(ByteBuffer buf) {
        buf.clear();
        buf.putLong(MAGIC);
        buf.putInt(pageSize);
        buf.putLong(totalPages);
        buf.putLong(firstFreePage);
        buf.putLong(userData[1]);
        buf.putLong(userData[2]);
        buf.putLong(userData[3]);
        buf.putLong(userData[4]);
        buf.putLong(userData[5]);
        while (buf.hasRemaining()) {
            buf.put((byte) 0);
        }
        buf.flip();
    }

    private void readHeader() throws IOException {
        headerBuf.clear();
        headerBuf.limit(68); // Read exactly 68 bytes for metadata fields
        channel.read(headerBuf, 0);
        headerBuf.flip();
        long magic = headerBuf.getLong();
        if (magic != MAGIC) {
            throw new IOException("Invalid magic number for PageStore file");
        }
        int storedPageSize = headerBuf.getInt();
        if (storedPageSize != pageSize) {
            // Adapt to the page size stored in the file if it differs
        }
        this.totalPages = headerBuf.getLong();
        this.firstFreePage = headerBuf.getLong();
        this.userData[1] = headerBuf.getLong();
        this.userData[2] = headerBuf.getLong();
        this.userData[3] = headerBuf.getLong();
        this.userData[4] = headerBuf.getLong();
        this.userData[5] = headerBuf.getLong();
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public long allocatePage() throws IOException {
        if (firstFreePage != -1) {
            long allocated = firstFreePage;
            pointerBuf.clear();
            channel.read(pointerBuf, pageOffset(allocated));
            pointerBuf.flip();
            firstFreePage = pointerBuf.getLong();
            return allocated;
        } else {
            long allocated = totalPages;
            totalPages++;
            // Write empty page to preallocate space
            zeroPageBuf.clear();
            channel.write(zeroPageBuf, pageOffset(allocated));
            return allocated;
        }
    }

    private long pageOffset(long pageId) {
        return pageId * pageSize;
    }

    @Override
    public void readPage(long pageId, ByteBuffer buffer) throws IOException {
        if (pageId < 0 || pageId >= totalPages) {
            throw new IOException("Invalid page ID: " + pageId);
        }
        int limit = buffer.limit();
        buffer.limit(buffer.position() + pageSize);
        channel.read(buffer, pageOffset(pageId));
        buffer.limit(limit);
    }

    @Override
    public void writePage(long pageId, ByteBuffer buffer) throws IOException {
        if (pageId < 0 || pageId >= totalPages) {
            throw new IOException("Invalid page ID: " + pageId);
        }
        int limit = buffer.limit();
        buffer.limit(buffer.position() + pageSize);
        channel.write(buffer, pageOffset(pageId));
        buffer.limit(limit);
    }

    @Override
    public void freePage(long pageId) throws IOException {
        if (pageId < 1 || pageId >= totalPages) {
            throw new IOException("Invalid page ID to free: " + pageId);
        }
        pointerBuf.clear();
        pointerBuf.putLong(firstFreePage);
        pointerBuf.flip();
        channel.write(pointerBuf, pageOffset(pageId));
        firstFreePage = pageId;
    }

    @Override
    public long getUserData(int index) {
        if (index < 1 || index >= userData.length) {
            return -1;
        }
        return userData[index];
    }

    @Override
    public void setUserData(int index, long value) {
        if (index >= 1 && index < userData.length) {
            userData[index] = value;
        }
    }

    @Override
    public void flush() throws IOException {
        writeHeaderToBuffer(headerBuf);
        channel.write(headerBuf, 0);
        channel.force(true);
    }

    @Override
    public void close() throws IOException {
        flush();
        channel.close();
        file.close();
    }
}
