package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPageStore;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NPageStoreMem implements NPageStore {
    private final int pageSize;
    private final List<byte[]> pages = new ArrayList<>();
    private final long[] userData = new long[6]; // index 1 to 5 to match userData1-5

    private long totalPages = 0;
    private long firstFreePage = -1;

    public NPageStoreMem(int pageSize) {
        this.pageSize = pageSize;
        // Allocate page 0 for header/metadata
        pages.add(new byte[pageSize]);
        totalPages = 1;
        for (int i = 0; i < userData.length; i++) {
            userData[i] = -1;
        }
        userData[3] = 0; // Default size is 0
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public long allocatePage() throws IOException {
        if (firstFreePage != -1) {
            long allocated = firstFreePage;
            byte[] page = pages.get((int) allocated);
            // Read next free page ID from the first 8 bytes of the page
            ByteBuffer buf = ByteBuffer.wrap(page);
            firstFreePage = buf.getLong(0);
            return allocated;
        } else {
            long allocated = totalPages;
            pages.add(new byte[pageSize]);
            totalPages++;
            return allocated;
        }
    }

    @Override
    public void readPage(long pageId, ByteBuffer buffer) throws IOException {
        if (pageId < 0 || pageId >= totalPages) {
            throw new IOException("Invalid page ID: " + pageId);
        }
        byte[] page = pages.get((int) pageId);
        int toRead = Math.min(buffer.remaining(), pageSize);
        buffer.put(page, 0, toRead);
    }

    @Override
    public void writePage(long pageId, ByteBuffer buffer) throws IOException {
        if (pageId < 0 || pageId >= totalPages) {
            throw new IOException("Invalid page ID: " + pageId);
        }
        byte[] page = pages.get((int) pageId);
        int toWrite = Math.min(buffer.remaining(), pageSize);
        buffer.get(page, 0, toWrite);
    }

    @Override
    public void freePage(long pageId) throws IOException {
        if (pageId < 1 || pageId >= totalPages) {
            throw new IOException("Invalid page ID to free: " + pageId);
        }
        byte[] page = pages.get((int) pageId);
        ByteBuffer buf = ByteBuffer.wrap(page);
        buf.putLong(0, firstFreePage);
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
        // No-op for in-memory store
    }

    @Override
    public void close() throws IOException {
        pages.clear();
    }
}
