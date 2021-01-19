package net.thevpc.nuts.runtime.bundles.nanodb;

import java.io.*;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

public class DBIndexValueFileStore implements DBIndexValueStore {

    public static final String NANODB_INDEX_STORE_0_8_1 = "nanodb-index-store-0.8.1";
    private File file;
    private DataOutputStream out;
    private NanoDBIndex index;
    private Object indexKey;

    public DBIndexValueFileStore(NanoDBIndex index, Object indexKey) {
        this.index=index;
        this.indexKey=indexKey;
    }

    public File getFile() {
        if (file == null) {
            File indexFile = ((NanoDBDefaultIndex) this.index).getFile();
            String indexFileName = indexFile.getName();
            if(indexFileName.endsWith(".index")){
                file = new File(indexFile.getParentFile(), indexFileName.substring(0,indexFileName.length()-".index".length())
                        + "." + String.valueOf(indexKey) + ".index-store");
            }else {
                file = new File(indexFile.getParentFile(), indexFileName
                        + "." + String.valueOf(indexKey) + ".index-store");
            }
        }
        return file;
    }

    public void add(long position) {
        if (out == null) {
            try {
                File pf = getFile().getParentFile();
                if (pf != null) {
                    pf.mkdirs();
                }
                out = new DataOutputStream(new FileOutputStream(getFile(), true));
                out.writeUTF(NANODB_INDEX_STORE_0_8_1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        try {
            out.writeLong(position);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try {
            out.close();
            out = null;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void addAll(long[] positions) {
        if (out == null) {
            try {
                File pf = getFile().getParentFile();
                if (pf != null) {
                    pf.mkdirs();
                }
                out = new DataOutputStream(new FileOutputStream(getFile(), true));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            for (long position : positions) {
                out.writeLong(position);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try {
            out.close();
            out = null;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public LongStream stream() {
        final PrimitiveIterator.OfLong iterator = new PrimitiveIterator.OfLong() {
            DataInputStream in;
            long nextValue;
            String header;

            {
                try {
                    in = new DataInputStream(new FileInputStream(getFile()));
                    header = in.readUTF();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public boolean hasNext() {
                try {
                    if (in.available() > 0) {
                        nextValue = in.readLong();
                        return false;
                    }
                } catch (EOFException ex) {
                    return false;
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                return true;
            }

            @Override
            public long nextLong() {
                return nextValue;
            }
        };
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(
                iterator,
                Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL), false);
    }

    @Override
    public boolean isMem() {
        return false;
    }

    @Override
    public void flush() {

    }
}
