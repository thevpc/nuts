package net.thevpc.nuts.runtime.standalone.util.collections;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Logger;

/**
 * Manages fixed-size pages on disk, providing logical read/write of variable-length byte arrays.
 * Implements block chaining for data that exceeds the block payload size.
 * Implements a free-list for reclaiming deleted blocks.
 */
public class NBFixedBlockFile implements Closeable {
    private static final Logger LOG = Logger.getLogger(NBFixedBlockFile.class.getName());
    private static final long MAGIC = 0x4E424C4B46494C45L; // "NBLKFILE"

    private RandomAccessFile file;
    private int blockSize;
    private int payloadSize;
    private long totalBlocks;
    private long firstFreeBlock;

    private long userData1; // e.g., root block ID
    private long userData2; // e.g., first leaf block ID
    private long userData3; // e.g., tree size
    private long userData4; // e.g., max degree (m)
    private long userData5; // e.g., allow duplicates (1/0)

    public NBFixedBlockFile(File f, int blockSize) throws IOException {
        boolean init = !f.exists() || f.length() == 0;
        this.file = new RandomAccessFile(f, "rw");
        if (init) {
            this.blockSize = blockSize;
            this.payloadSize = blockSize - 8; // 8 bytes for next block pointer
            this.totalBlocks = 0;
            this.firstFreeBlock = -1;
            this.userData1 = -1;
            this.userData2 = -1;
            this.userData3 = 0;
            this.userData4 = -1;
            this.userData5 = -1;
            writeHeader();
        } else {
            readHeader();
            if (this.blockSize != blockSize) {
                // If it already exists, we adapt to its block size
                this.payloadSize = this.blockSize - 8;
            }
        }
    }

    private void writeHeader() throws IOException {
        file.seek(0);
        file.writeLong(MAGIC);
        file.writeInt(blockSize);
        file.writeLong(totalBlocks);
        file.writeLong(firstFreeBlock);
        file.writeLong(userData1);
        file.writeLong(userData2);
        file.writeLong(userData3);
        file.writeLong(userData4);
        file.writeLong(userData5);
    }

    private void readHeader() throws IOException {
        file.seek(0);
        long magic = file.readLong();
        if (magic != MAGIC) {
            throw new IOException("Invalid magic number for NBlockFile");
        }
        this.blockSize = file.readInt();
        this.totalBlocks = file.readLong();
        this.firstFreeBlock = file.readLong();
        this.userData1 = file.readLong();
        this.userData2 = file.readLong();
        this.userData3 = file.readLong();
        this.userData4 = file.readLong();
        this.userData5 = file.readLong();
    }

    public void flushHeader() throws IOException {
        writeHeader();
    }

    public long getUserData1() { return userData1; }
    public void setUserData1(long userData1) { this.userData1 = userData1; }
    public long getUserData2() { return userData2; }
    public void setUserData2(long userData2) { this.userData2 = userData2; }
    public long getUserData3() { return userData3; }
    public void setUserData3(long userData3) { this.userData3 = userData3; }
    public long getUserData4() { return userData4; }
    public void setUserData4(long userData4) { this.userData4 = userData4; }
    public long getUserData5() { return userData5; }
    public void setUserData5(long userData5) { this.userData5 = userData5; }

    private long allocateBlock() throws IOException {
        if (firstFreeBlock != -1) {
            long allocated = firstFreeBlock;
            file.seek(blockOffset(allocated));
            long nextFree = file.readLong();
            firstFreeBlock = nextFree;
            return allocated;
        } else {
            long allocated = totalBlocks;
            totalBlocks++;
            // Don't need to write header totalBlocks immediately unless flushing
            return allocated;
        }
    }

    public void freeBlockChain(long blockId) throws IOException {
        long current = blockId;
        while (current != -1) {
            file.seek(blockOffset(current));
            long next = file.readLong();
            
            // push to free stack
            file.seek(blockOffset(current));
            file.writeLong(firstFreeBlock);
            firstFreeBlock = current;
            
            current = next;
        }
    }

    private long blockOffset(long blockId) {
        // 128 bytes of header space
        return 128 + (blockId * blockSize);
    }

    public long writeData(byte[] data) throws IOException {
        int offset = 0;
        long headBlock = -1;
        long prevBlock = -1;

        if (data.length == 0) {
            long b = allocateBlock();
            file.seek(blockOffset(b));
            file.writeLong(-1);
            file.writeInt(0);
            return b;
        }

        while (offset < data.length) {
            long currentBlock = allocateBlock();
            if (headBlock == -1) {
                headBlock = currentBlock;
            }
            if (prevBlock != -1) {
                file.seek(blockOffset(prevBlock));
                file.writeLong(currentBlock);
            }

            int toWrite = Math.min(payloadSize - (prevBlock == -1 ? 4 : 0), data.length - offset);
            
            file.seek(blockOffset(currentBlock));
            file.writeLong(-1); // next block is -1 initially
            if (prevBlock == -1) {
                file.writeInt(data.length); // write total length only in the first block
            }
            file.write(data, offset, toWrite);
            
            offset += toWrite;
            prevBlock = currentBlock;
        }
        return headBlock;
    }

    public byte[] readData(long headBlock) throws IOException {
        if (headBlock == -1) return null;

        file.seek(blockOffset(headBlock));
        long nextBlock = file.readLong();
        int totalLength = file.readInt();
        
        byte[] data = new byte[totalLength];
        int bytesReadTotal = 0;
        
        int toRead = Math.min(payloadSize - 4, totalLength);
        file.readFully(data, bytesReadTotal, toRead);
        bytesReadTotal += toRead;
        
        long currentBlock = nextBlock;
        while (currentBlock != -1 && bytesReadTotal < totalLength) {
            file.seek(blockOffset(currentBlock));
            currentBlock = file.readLong();
            int readAmount = Math.min(payloadSize, totalLength - bytesReadTotal);
            file.readFully(data, bytesReadTotal, readAmount);
            bytesReadTotal += readAmount;
        }
        
        return data;
    }

    public void updateData(long headBlock, byte[] data) throws IOException {
        freeBlockChain(headBlock);
        // Writing will allocate new blocks and ideally reuse the ones we just freed
        long newHead = writeData(data);
        if (newHead != headBlock) {
            // Because we pushed them to a LIFO stack, newHead SHOULD equal headBlock if we wrote the same or fewer blocks
            // If it doesn't, we still have to return the new head, but wait... updateData should keep the same head!
            // Wait, we can't change the headBlock easily because other structures point to it.
            throw new IllegalStateException("Internal error: Freeing and reallocating changed head block ID.");
        }
    }
    
    public void updateDataSafe(long headBlock, byte[] data) throws IOException {
        // Collect existing blocks
        long current = headBlock;
        int offset = 0;
        long prev = -1;
        
        while (offset < data.length) {
            if (current == -1) {
                current = allocateBlock();
                if (prev != -1) {
                    file.seek(blockOffset(prev));
                    file.writeLong(current);
                }
            }
            
            int toWrite = Math.min(payloadSize - (prev == -1 ? 4 : 0), data.length - offset);
            file.seek(blockOffset(current));
            
            // Assume we might need another block
            long nextBlock;
            if (offset + toWrite < data.length) {
                nextBlock = file.readLong(); // keep existing next if any
            } else {
                nextBlock = file.readLong();
                file.seek(blockOffset(current));
                file.writeLong(-1); // terminate here
                if (nextBlock != -1) {
                    freeBlockChain(nextBlock); // Free the rest
                }
                nextBlock = -1;
            }
            
            file.seek(blockOffset(current) + 8);
            if (prev == -1) {
                file.writeInt(data.length);
            }
            file.write(data, offset, toWrite);
            
            offset += toWrite;
            prev = current;
            current = nextBlock;
        }
        
        if (offset == 0 && data.length == 0) {
            file.seek(blockOffset(headBlock));
            long nextInfo = file.readLong();
            if (nextInfo != -1) {
                freeBlockChain(nextInfo);
            }
            file.seek(blockOffset(headBlock));
            file.writeLong(-1);
            file.writeInt(0);
        }
    }

    @Override
    public void close() throws IOException {
        flushHeader();
        file.close();
    }
}
