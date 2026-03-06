package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.util.NBPlusTree;

public abstract class NBPlusTreeStoreFixedDiskNode<K extends Comparable<K>, V> implements NBPlusTree.Node<K, V> {

    protected long blockId = -1;
    protected long parentId = -1;
    protected boolean dirty = true;
    protected int size = 0;
    protected int maxSize;
    protected int minSize;

    protected NBPlusTreeStoreFixedDisk<K, V> store;

    public NBPlusTreeStoreFixedDiskNode(NBPlusTreeStoreFixedDisk<K, V> store, int m) {
        this.store = store;
        this.maxSize = m - 1;
        this.minSize = (int) (Math.ceil(m / 2.0) - 1);
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> parent() {
        return parentId == -1 ? null : (NBPlusTree.IntermediateNode<K, V>) store.loadNode(parentId);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int minSize() {
        return minSize;
    }

    @Override
    public int maxSize() {
        return maxSize;
    }
    
    public abstract void serialize(java.io.DataOutputStream dos) throws java.io.IOException;
    public abstract void deserialize(java.io.DataInputStream dis) throws java.io.IOException;
}
