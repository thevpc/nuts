package net.thevpc.nuts.runtime.bundles.nanodb;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

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

    public NanoDBDefaultIndex(Class<T> keyType,NanoDBSerializer<T> ser, DBIndexValueStoreFactory storeFactory, Map<T, DBIndexValueStore> index, File file) {
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
    public void load(NutsSession session) {
        if (file.exists()) {
            load(file,session);
        }
    }

    @Override
    public void flush(NutsSession session) {
        file.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(file)) {
            store(new NanoDBDefaultOutputStream(out,session),session);
        } catch (IOException e) {
            throw new NutsIOException(session,e);
        }
    }

    @Override
    public void put(T s, long position,NutsSession session) {
        DBIndexValueStore store = index.get(s);
        if (store == null) {
            store = storeFactory.create(this, s);
            index.put(s, store);
        }
        store.add(position,session);

    }

    @Override
    public LongStream get(T s,NutsSession session) {
        DBIndexValueStore store = index.get(s);
        return store == null ? Arrays.stream(new long[0]) : store.stream(session);
    }

    @Override
    public void clear(NutsSession session) {
        index.clear();
    }

    @Override
    public Stream<T> findAll(NutsSession session) {
        return index.keySet().stream();
    }

    public void storeKey(T k, NanoDBOutputStream dos, NutsSession session) throws IOException {
        ser.write(k, dos, session);
    }


    public void store(NanoDBOutputStream dos,NutsSession session) {
        try {
            dos.writeUTF(NANODB_INDEX_0_8_1);
            dos.writeLong(index.size());
            for (Map.Entry<T, DBIndexValueStore> e : index.entrySet()) {
                storeKey(e.getKey(), dos, session);
                DBIndexValueStore store = e.getValue();
                boolean mem = store.isMem();
                if (mem) {
                    long[] pos = store.stream(session).toArray();
                    dos.writeByte(0);
                    dos.writeInt(pos.length);
                    for (long po : pos) {
                        dos.writeLong(po);
                    }
                } else {
                    dos.writeByte(1);
                    store.flush(session);
                }
            }
            dos.flush();
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

    public void load(NanoDBInputStream in,NutsSession session) {
        try {
            String header = in.readUTF();
            if (!NANODB_INDEX_0_8_1.equals(header)) {
                throw new NutsIOException(session, NutsMessage.cstyle("unsupported index file %s",header));
            }
            long r = in.readLong();
            index = new HashMap<T, DBIndexValueStore>(r <= 10 ? 10 : (int) r);

            for (long i = 0; i < r; i++) {
                T o = readKey(in,session);
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
            throw new NutsIOException(session,ex);
        }
    }

    protected T readKey(NanoDBInputStream in,NutsSession session) throws IOException {
        return ser.read(in, keyType, session);
    }

    public void store(File stream,NutsSession session) throws IOException {
        try (OutputStream out = new FileOutputStream(stream)) {
            store(new NanoDBDefaultOutputStream(out,session),session);
        }
    }



    public void load(File stream,NutsSession session) {
        try (InputStream out = new FileInputStream(stream)) {
            load(new NanoDBDefaultInputStream(out,session),session);
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
    }

}
