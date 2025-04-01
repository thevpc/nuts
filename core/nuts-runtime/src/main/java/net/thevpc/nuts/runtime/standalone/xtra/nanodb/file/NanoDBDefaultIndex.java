package net.thevpc.nuts.runtime.standalone.xtra.nanodb.file;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.*;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NanoDBDefaultIndex<T> extends NanoDBAbstractIndex<T> {
    public static final String NANODB_INDEX_0_8_1 = "nanodb-index-0.8.1";
    private Map<T, DBIndexValueStore> index = new HashMap<>();
    private DBIndexValueStoreFactory storeFactory;
    private File file;
    private Class<T> keyType;

    public NanoDBDefaultIndex(Class<T> keyType, NanoDBSerializer<T> ser, DBIndexValueStoreFactory storeFactory, Map<T, DBIndexValueStore> index, File file) {
        super(ser);
        this.keyType = keyType;
        this.index = index;
        this.storeFactory = storeFactory;
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public void load() {
        if (file.exists()) {
            load(file);
        }
    }

    @Override
    public void flush() {
        file.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(file)) {
            store(new NanoDBDefaultOutputStream(out));
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public void put(T s, long position) {
        DBIndexValueStore store = index.get(s);
        if (store == null) {
            store = storeFactory.create(this, s);
            index.put(s, store);
        }
        store.add(position);

    }

    @Override
    public LongStream get(T s) {
        DBIndexValueStore store = index.get(s);
        return store == null ? Arrays.stream(new long[0]) : store.stream();
    }

    @Override
    public void clear() {
        index.clear();
    }

    @Override
    public Stream<T> findAll() {
        return index.keySet().stream();
    }

    public void storeKey(T k, NanoDBOutputStream dos) throws IOException {
        ser.write(k, dos);
    }


    public void store(NanoDBOutputStream dos) {
        try {
            dos.writeUTF(NANODB_INDEX_0_8_1);
            dos.writeLong(index.size());
            for (Map.Entry<T, DBIndexValueStore> e : index.entrySet()) {
                storeKey(e.getKey(), dos);
                DBIndexValueStore store = e.getValue();
                boolean mem = store.isMem();
                if (mem) {
                    long[] pos = store.stream().toArray();
                    dos.writeByte(0);
                    dos.writeInt(pos.length);
                    for (long po : pos) {
                        dos.writeLong(po);
                    }
                } else {
                    dos.writeByte(1);
                    store.flush();
                }
            }
            dos.flush();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public void load(NanoDBInputStream in) {
        try {
            String header = in.readUTF();
            if (!NANODB_INDEX_0_8_1.equals(header)) {
                throw new NIOException(NMsg.ofC("unsupported index file %s",header));
            }
            long r = in.readLong();
            index = new HashMap<T, DBIndexValueStore>(r <= 10 ? 10 : (int) r);

            for (long i = 0; i < r; i++) {
                T o = readKey(in);
                byte type = in.readByte();
                if (type == 0 /**in  memory **/) {
                    int len = in.readInt();
                    long[] pos = new long[len];
                    for (int j = 0; j < len; j++) {
                        pos[j] = in.readLong();
                    }
                    index.put(o, storeFactory.load(this, o, pos));
                } else if (type == 1 /**in  memory **/) {
                    index.put(o, storeFactory.loadExternal(this, o));
                } else {
                    index.put(o, storeFactory.loadExternal(this, o));
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    protected T readKey(NanoDBInputStream in) throws IOException {
        return ser.read(in, keyType);
    }

    public void store(File stream) throws IOException {
        try (OutputStream out = new FileOutputStream(stream)) {
            store(new NanoDBDefaultOutputStream(out));
        }
    }



    public void load(File stream) {
        try (InputStream out = new FileInputStream(stream)) {
            load(new NanoDBDefaultInputStream(out));
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

}
