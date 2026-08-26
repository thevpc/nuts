package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NBPlusTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.List;

public class NBPlusTreeStoreFixedDiskLeafNode<K, V> extends NBPlusTreeStoreFixedDiskNode<K, V> implements NBPlusTree.LeafNode<K, V> {

    protected long leftSiblingId = -1;
    protected long rightSiblingId = -1;
    protected K[] keys;
    protected V[] values;

    @SuppressWarnings("unchecked")
    public NBPlusTreeStoreFixedDiskLeafNode(NBPlusTreeStoreFixedDisk<K, V> store, int m) {
        super(store, m);
        this.keys = (K[]) new Comparable[m];
        this.values = (V[]) new Object[m];
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public K firstKey() {
        return size == 0 ? null : keys[0];
    }

    @Override
    public List<K> keys() {
        return new AbstractList<K>() {
            @Override
            public K get(int index) {
                return keys[index];
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public V valueAt(int index) {
        return values[index];
    }

    @Override
    public K keyAt(int index) {
        return keys[index];
    }

    @Override
    public NBPlusTree.Entry<K, V> entryAt(int index) {
        return new AbstractMap.SimpleEntry<>(keys[index], values[index]);
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
        V old = this.values[index];
        this.values[index] = value;
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
            store.serializeKey(keys[i], dos);
            store.serializeValue(values[i], dos);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        parentId = dis.readLong();
        leftSiblingId = dis.readLong();
        rightSiblingId = dis.readLong();
        size = dis.readInt();
        for (int i = 0; i < size; i++) {
            keys[i] = store.deserializeKey(dis);
            values[i] = store.deserializeValue(dis);
        }
    }
}
