package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NBPlusTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.List;

public class NBPlusTreeStoreFixedDiskLeafNode<K extends Comparable<K>, V> extends NBPlusTreeStoreFixedDiskNode<K, V> implements NBPlusTree.LeafNode<K, V> {

    protected long leftSiblingId = -1;
    protected long rightSiblingId = -1;
    protected NBPlusTree.Entry<K, V>[] dictionary;

    @SuppressWarnings("unchecked")
    public NBPlusTreeStoreFixedDiskLeafNode(NBPlusTreeStoreFixedDisk<K, V> store, int m) {
        super(store, m);
        this.dictionary = new NBPlusTree.Entry[m];
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public K firstKey() {
        return size == 0 ? null : dictionary[0].getKey();
    }

    @Override
    public List<K> keys() {
        return new AbstractList<K>() {
            @Override
            public K get(int index) {
                return dictionary[index].getKey();
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public V valueAt(int index) {
        return dictionary[index].getValue();
    }

    @Override
    public K keyAt(int index) {
        return dictionary[index].getKey();
    }

    @Override
    public NBPlusTree.Entry<K, V> entryAt(int index) {
        return dictionary[index];
    }

    @Override
    public NBPlusTree.LeafNode<K, V> leftSibling() {
        return leftSiblingId == -1 ? null : (NBPlusTree.LeafNode<K, V>) store.loadNode(leftSiblingId);
    }

    @Override
    public NBPlusTree.LeafNode<K, V> rightSibling() {
        return rightSiblingId == -1 ? null : (NBPlusTree.LeafNode<K, V>) store.loadNode(rightSiblingId);
    }

    public V setValueAt(int index, V value) {
        NBPlusTree.Entry<K, V> e = this.dictionary[index];
        V old = e.getValue();
        this.dictionary[index] = new AbstractMap.SimpleEntry<>(e.getKey(), value);
        dirty = true;
        return old;
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeBoolean(true); // isLeaf
        dos.writeLong(parentId);
        dos.writeLong(leftSiblingId);
        dos.writeLong(rightSiblingId);
        dos.writeInt(size);
        for (int i = 0; i < size; i++) {
            NBPlusTree.Entry<K, V> entry = dictionary[i];
            store.serializeKey(entry.getKey(), dos);
            store.serializeValue(entry.getValue(), dos);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        // boolean isLeaf is already read
        parentId = dis.readLong();
        leftSiblingId = dis.readLong();
        rightSiblingId = dis.readLong();
        size = dis.readInt();
        for (int i = 0; i < size; i++) {
            K k = store.deserializeKey(dis);
            V v = store.deserializeValue(dis);
            dictionary[i] = new AbstractMap.SimpleEntry<>(k, v);
        }
    }
}
