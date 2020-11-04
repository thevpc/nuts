/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.util.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author vpc
 */
public interface PersistentMap<K, V> {

    V get(K k);

    void put(K k, V v);

    void flush();

    int size();

    Set<K> keySet();

    Set<Map.Entry<K, V>> entrySet();

    interface HashPath<T> {

        String path(int hash);
    }

    interface Serializer<T> {

        void store(T obj, ExtendedOutputStream out) throws IOException;

        T read(ExtendedInputStream in) throws IOException;
    }

    abstract class ExtendedOutputStream extends ObjectOutputStream {

        public ExtendedOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        public ExtendedOutputStream() throws IOException, SecurityException {
        }

        abstract void writeLob(String name, InputStream out) throws IOException;
    }

    abstract class ExtendedInputStream extends ObjectInputStream {

        public ExtendedInputStream(InputStream in) throws IOException {
            super(in);
        }

        public ExtendedInputStream() throws IOException, SecurityException {
        }

        abstract InputStream readLob(String name) throws IOException;
    }
}
