package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.io.NPageStore;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NBFixedBlockFile implements Closeable {
//    private static final int HEADER_ROOT_ID = 1;
//    private static final int HEADER_FIRST_LEAF_ID = 2;
//    private static final int HEADER_SIZE = 3;
//    private static final int HEADER_ORDER = 4;
//    private static final int HEADER_ALLOW_DUPLICATES = 5;

    private final NPageStore store;
    private final int pageSize;
    private final int payloadSize;

    public NBFixedBlockFile(NPageStore store) {
        this.store = store;
        this.pageSize = store.pageSize();
        this.payloadSize = pageSize - 8;
    }

    public long getUserData1() { return store.getUserData(1); }
    public void setUserData1(long val) { store.setUserData(1, val); }
    public long getUserData2() { return store.getUserData(2); }
    public void setUserData2(long val) { store.setUserData(2, val); }
    public long getUserData3() { return store.getUserData(3); }
    public void setUserData3(long val) { store.setUserData(3, val); }
    public long getUserData4() { return store.getUserData(4); }
    public void setUserData4(long val) { store.setUserData(4, val); }
    public long getUserData5() { return store.getUserData(5); }
    public void setUserData5(long val) { store.setUserData(5, val); }

    public void flushHeader() throws IOException {
        store.flush();
    }

    public void freeBlockChain(long blockId) throws IOException {
        long current = blockId;
        while (current != -1 && current != 0) {
            ByteBuffer buf = ByteBuffer.allocate(store.pageSize());
            store.readPage(current, buf);
            buf.flip();
            long next = buf.getLong(0);
            if (next <= 0) {
                next = -1;
            }
            store.freePage(current);
            current = next;
        }
    }

    public long writeData(byte[] data) throws IOException {
        return writeData(data, 0, data.length);
    }

    public long writeData(byte[] data, int dataOffset, int dataLength) throws IOException {
        int offset = dataOffset;
        int endOffset = dataOffset + dataLength;
        long headBlock = -1;
        long prevBlock = -1;

        if (dataLength == 0) {
            long b = store.allocatePage();
            ByteBuffer buf = ByteBuffer.allocate(pageSize);
            buf.putLong(-1);
            buf.putInt(0);
            buf.flip();
            store.writePage(b, buf);
            return b;
        }

        while (offset < endOffset) {
            long currentBlock = store.allocatePage();
            if (headBlock == -1) {
                headBlock = currentBlock;
            }
            if (prevBlock != -1) {
                ByteBuffer prevBuf = ByteBuffer.allocate(pageSize);
                store.readPage(prevBlock, prevBuf);
                prevBuf.flip();
                prevBuf.putLong(0, currentBlock);
                prevBuf.flip();
                store.writePage(prevBlock, prevBuf);
            }

            int toWrite = Math.min(payloadSize - (prevBlock == -1 ? 4 : 0), endOffset - offset);

            ByteBuffer buf = ByteBuffer.allocate(pageSize);
            buf.putLong(-1);
            if (prevBlock == -1) {
                buf.putInt(dataLength);
            }
            buf.put(data, offset, toWrite);
            buf.flip();
            store.writePage(currentBlock, buf);

            offset += toWrite;
            prevBlock = currentBlock;
        }
        return headBlock;
    }

    public byte[] readData(long headBlock) throws IOException {
        if (headBlock == -1) return null;

        ByteBuffer buf = ByteBuffer.allocate(pageSize);
        store.readPage(headBlock, buf);
        buf.flip();
        long nextBlock = buf.getLong();
        if (nextBlock <= 0) {
            nextBlock = -1;
        }
        int totalLength = buf.getInt();

        byte[] data = new byte[totalLength];
        int bytesReadTotal = 0;

        int toRead = Math.min(payloadSize - 4, totalLength);
        buf.get(data, bytesReadTotal, toRead);
        bytesReadTotal += toRead;

        long currentBlock = nextBlock;
        while (currentBlock != -1 && bytesReadTotal < totalLength) {
            buf.clear();
            store.readPage(currentBlock, buf);
            buf.flip();
            currentBlock = buf.getLong();
            if (currentBlock <= 0) {
                currentBlock = -1;
            }
            int readAmount = Math.min(payloadSize, totalLength - bytesReadTotal);
            buf.get(data, bytesReadTotal, readAmount);
            bytesReadTotal += readAmount;
        }

        return data;
    }

    public void updateDataSafe(long headBlock, byte[] data) throws IOException {
        updateDataSafe(headBlock, data, 0, data.length);
    }

    public void updateDataSafe(long headBlock, byte[] data, int dataOffset, int dataLength) throws IOException {
        long current = headBlock;
        int offset = dataOffset;
        int endOffset = dataOffset + dataLength;
        long prev = -1;

        while (offset < endOffset) {
            if (current == -1) {
                current = store.allocatePage();
                if (prev != -1) {
                    ByteBuffer prevBuf = ByteBuffer.allocate(pageSize);
                    store.readPage(prev, prevBuf);
                    prevBuf.flip();
                    prevBuf.putLong(0, current);
                    prevBuf.flip();
                    store.writePage(prev, prevBuf);
                }
            }

            int toWrite = Math.min(payloadSize - (prev == -1 ? 4 : 0), endOffset - offset);
            ByteBuffer buf = ByteBuffer.allocate(pageSize);
            store.readPage(current, buf);
            buf.flip();

            long nextBlock;
            if (offset + toWrite < endOffset) {
                nextBlock = buf.getLong(0);
                if (nextBlock <= 0) {
                    nextBlock = -1;
                }
            } else {
                nextBlock = buf.getLong(0);
                if (nextBlock <= 0) {
                    nextBlock = -1;
                }
                buf.putLong(0, -1L);
                if (nextBlock != -1) {
                    freeBlockChain(nextBlock);
                }
                nextBlock = -1;
            }

            buf.position(8);
            if (prev == -1) {
                buf.putInt(dataLength);
            }
            buf.put(data, offset, toWrite);
            buf.flip();
            store.writePage(current, buf);

            offset += toWrite;
            prev = current;
            current = nextBlock;
        }

        if (offset == dataOffset && dataLength == 0) {
            ByteBuffer buf = ByteBuffer.allocate(pageSize);
            store.readPage(headBlock, buf);
            buf.flip();
            long nextInfo = buf.getLong(0);
            if (nextInfo != -1) {
                freeBlockChain(nextInfo);
            }
            buf.putLong(0, -1L);
            buf.putInt(8, 0);
            buf.flip();
            store.writePage(headBlock, buf);
        }
    }

    @Override
    public void close() throws IOException {
        flushHeader();
        store.close();
    }
}
