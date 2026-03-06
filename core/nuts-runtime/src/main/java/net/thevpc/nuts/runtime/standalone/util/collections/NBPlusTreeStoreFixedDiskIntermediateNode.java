package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NBPlusTree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NBPlusTreeStoreFixedDiskIntermediateNode<K extends Comparable<K>, V> extends NBPlusTreeStoreFixedDiskNode<K, V> implements NBPlusTree.IntermediateNode<K, V> {

    protected K firstKey;
    protected long leftSiblingId = -1;
    protected long rightSiblingId = -1;
    protected long[] childrenIds;

    public NBPlusTreeStoreFixedDiskIntermediateNode(NBPlusTreeStoreFixedDisk<K, V> store, int m) {
        super(store, m);
        // plus one to handle overfull!!
        this.childrenIds = new long[this.maxSize + 1];
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public K firstKey() {
        return firstKey;
    }

    @Override
    public K key(int i) {
        return store.loadNode(childrenIds[i]).firstKey();
    }

    @Override
    public NBPlusTree.Node<K, V> child(int i) {
        return store.loadNode(childrenIds[i]);
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> leftSibling() {
        return leftSiblingId == -1 ? null : (NBPlusTree.IntermediateNode<K, V>) store.loadNode(leftSiblingId);
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> rightSibling() {
        return rightSiblingId == -1 ? null : (NBPlusTree.IntermediateNode<K, V>) store.loadNode(rightSiblingId);
    }

    @Override
    public void serialize(DataOutputStream dos) throws IOException {
        dos.writeBoolean(false); // isLeaf
        dos.writeLong(parentId);
        dos.writeLong(leftSiblingId);
        dos.writeLong(rightSiblingId);
        dos.writeInt(size);
        store.serializeKey(firstKey, dos);
        for (int i = 0; i < size; i++) {
            dos.writeLong(childrenIds[i]);
        }
    }

    @Override
    public void deserialize(DataInputStream dis) throws IOException {
        parentId = dis.readLong();
        leftSiblingId = dis.readLong();
        rightSiblingId = dis.readLong();
        size = dis.readInt();
        firstKey = store.deserializeKey(dis);
        for (int i = 0; i < size; i++) {
            childrenIds[i] = dis.readLong();
        }
    }
}
