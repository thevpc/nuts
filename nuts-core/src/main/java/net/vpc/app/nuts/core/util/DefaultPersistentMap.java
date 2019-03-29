/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.vpc.common.util.FileDepthFirstIterator;
import net.vpc.common.util.IteratorBuilder;
import net.vpc.common.util.LRUMap;

/**
 *
 * @author vpc
 */
public class DefaultPersistentMap<K, V> implements PersistentMap<K, V>, AutoCloseable {

    private static final String EXT_DBE = ".dbe";
    private File root;
    private int maxCache = 512;
    private int maxBuffer = 0;
    private Map<K, V> dirty = new HashMap<>();
    private Map<K, V> cache;
    private HashPath hash = new DefaultHashPath();
    private Class<K> keyType;
    private Class<V> valueType;
    private Serializer<K> keySer;
    private Serializer<V> valueSer;

//    public static void main(String[] args) {
//        DefaultPersistentMap<String, String> t = new DefaultPersistentMap<>(String.class, String.class, new File(System.getProperty("user.home"), "toz"));
//        long a = System.currentTimeMillis();
//        for (int i = 0; i < 10000; i++) {
//            t.put(String.valueOf(i), Integer.toHexString(i));
//        }
//        t.flush();
//        System.out.println(System.currentTimeMillis() - a);
//        for (Map.Entry<String, String> entry : t.entrySet()) {
//            System.out.println(entry.getKey() + ":" + entry.getValue());
//        }
//        System.out.println(t.size());
//    }
    public DefaultPersistentMap(Class<K> keyType, Class<V> valueType, File root) {
        this(keyType, valueType, root, 512);
    }

    public DefaultPersistentMap(Class<K> keyType, Class<V> valueType, File root, int maxCache) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.root = root;
        this.root.mkdirs();
        this.cache = new LRUMap<K, V>(this.maxCache = maxCache);
        this.keySer = resolveSerializer(keyType);
        this.valueSer = resolveSerializer(valueType);
    }

    private String hashKey(K k) {
        return hash.path(k.hashCode());
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            int size = -1;

            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return IteratorBuilder.ofFileDfs(root)
                        .filter(x -> x.getName().endsWith(EXT_DBE))
                        .map(
                                (File x) -> (Map.Entry<K, V>) resolve(x)
                        )
                        .nonNull()
                        .iterator();
            }

            @Override
            public int size() {
                if (size == -1) {
                    this.size = DefaultPersistentMap.this.size();
                }
                return size;
            }
        };
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<K>() {
            int size = -1;

            @Override
            public Iterator<K> iterator() {
                return IteratorBuilder.ofFileDfs(root)
                        .filter(x -> x.getName().endsWith(EXT_DBE))
                        .map(
                                (File x) -> (Map.Entry<K, V>) resolve(x)
                        )
                        .nonNull()
                        .map(x -> x.getKey())
                        .iterator();
            }

            @Override
            public int size() {
                if (size == -1) {
                    this.size = DefaultPersistentMap.this.size();
                }
                return size;
            }
        };
    }

    @Override
    public int size() {
        return computeSize();
    }

    public int computeSize() {
        int s = 0;
        Iterator<Map.Entry<K, V>> p = entrySet().iterator();
        while (p.hasNext()) {
            p.next();
            s++;
        }
        return s;
    }

    private KeyValFile resolve(File f) {
        KeyValFile ff = new KeyValFile(f);
        ff.load();
        return ff;
    }

    protected class KeyValFile implements Map.Entry<K, V> {

        K key;
        V value;
        File file;
        boolean exists;

        public KeyValFile(File file) {
            this.file = file;
        }

        public KeyValFile(K key, V value, File file, boolean exists) {
            this.key = key;
            this.value = value;
            this.file = file;
            this.exists = exists;
        }

        public void load() {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                try (MyExtendedInputStream is = new MyExtendedInputStream(new ByteArrayInputStream(bytes))) {
                    key = keySer.read(is);
                    value = valueSer.read(is);
                    this.exists = true;
                }
            } catch (IOException ex) {
                this.exists = false;
                file.delete();
            }
        }

        public void store() {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            try (MyExtendedOutputStream s = new MyExtendedOutputStream(b)) {
                keySer.store(key, s);
                valueSer.store(value, s);
                s.flush();
                file.getParentFile().mkdirs();
                Files.write(file.toPath(), b.toByteArray(), StandardOpenOption.CREATE);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            dirty.put(key, value);
            return old;
        }

    }

    protected KeyValFile resolveFileHandle(K k) {
        String j = hashKey(k);
        File x = new File(root, j);
        String[] children = x.list();
        int max = 0;
        if (children != null) {
            for (String c : children) {
                if (c.endsWith(EXT_DBE)) {
                    int v = -1;
                    try {
                        v = Integer.parseInt(c.substring(0, c.length() - EXT_DBE.length()), 16);
                    } catch (Exception any) {
                        continue;
                    }
                    if (v > max) {
                        v = max;
                    }
                    File t = new File(x, c);
                    KeyValFile ff = new KeyValFile(t);
                    ff.load();
                    if (ff.key.equals(k)) {
                        return ff;
                    }
                }
            }
        }
        return new KeyValFile(k, null, new File(x, Integer.toString(max + 1, 16) + EXT_DBE), false);
    }

    public V load(K k) {
        KeyValFile f = resolveFileHandle(k);
        if (f.exists) {
            return f.value;
        }
        return null;
    }

    @Override
    public V get(K k) {
        V g = dirty.get(k);
        if (g == null) {
            if (dirty.containsKey(k)) {
                return null;
            }
            g = cache.get(k);
            if (g == null) {
                if (cache.containsKey(k)) {
                    return null;
                }
                g = load(k);
                if (g != null) {
                    cache.put(k, g);
                }
            }
        }
        return g;
    }

    @Override
    public void put(K k, V v) {
        dirty.put(k, v);
        if (dirty.size() >= maxBuffer) {
            flush();
        }
    }

    @Override
    public void flush() {
        for (Iterator<Map.Entry<K, V>> it = dirty.entrySet().iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = it.next();
            KeyValFile f = resolveFileHandle(entry.getKey());
            f.value = entry.getValue();
            f.store();
            it.remove();
        }
    }

    private static class StringSerializer implements Serializer<String> {

        public StringSerializer() {
        }

        @Override
        public void store(String obj, ExtendedOutputStream out) throws IOException {
            out.writeUTF(obj);
        }

        @Override
        public String read(ExtendedInputStream in) throws IOException {
            return in.readUTF();
        }
    }

    private static class IntegerSerializer implements Serializer<Integer> {

        public IntegerSerializer() {
        }

        @Override
        public void store(Integer obj, ExtendedOutputStream out) throws IOException {
            out.writeInt(obj);
        }

        @Override
        public Integer read(ExtendedInputStream in) throws IOException {
            return in.readInt();
        }
    }

    private static class ObjectSerializer implements Serializer<Object> {

        public ObjectSerializer() {
        }

        @Override
        public void store(Object obj, ExtendedOutputStream out) throws IOException {
            out.writeObject(obj);
        }

        @Override
        public Object read(ExtendedInputStream in) throws IOException {
            try {
                return in.readObject();
            } catch (ClassNotFoundException ex) {
                throw new IOException(ex);
            }
        }
    }

    public Serializer resolveSerializer(Class cls) {
        switch (cls.getName()) {
            case "java.lang.String":
                return new StringSerializer();
            case "java.lang.Integer":
                return new IntegerSerializer();
        }
        throw new IllegalArgumentException("Not supported yet");
    }

    private static class DefaultHashPath implements HashPath {

        public DefaultHashPath() {
        }

        @Override
        public String path(int hash) {
            StringBuilder sb = new StringBuilder(16);
            sb.append(Integer.toUnsignedString(hash, 36).toUpperCase());
            while (sb.length() < 6) {
                sb.insert(0, '0');
            }
            sb.insert(2, '/');
            sb.insert(5, '/');
//        sb.insert(3, '/');
            return sb.toString();
        }
    }

    @Override
    public void close() throws Exception {
        flush();
    }

    private class MyExtendedOutputStream extends ExtendedOutputStream {

        public MyExtendedOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        public MyExtendedOutputStream() throws IOException, SecurityException {
        }

        @Override
        void writeLob(String name, InputStream out) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }

    private class MyExtendedInputStream extends ExtendedInputStream {

        public MyExtendedInputStream(InputStream out) throws IOException {
            super(out);
        }

        public MyExtendedInputStream() throws IOException, SecurityException {
        }

        @Override
        InputStream readLob(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
