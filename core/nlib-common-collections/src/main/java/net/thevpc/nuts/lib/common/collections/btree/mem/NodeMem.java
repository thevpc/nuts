package net.thevpc.nuts.lib.common.collections.btree.mem;

import net.thevpc.nuts.lib.common.collections.BPlusTree;

public abstract class NodeMem<K extends Comparable<K>, V> implements BPlusTree.Node<K, V> {

    int maxSize;
    int minSize;
    int size;
    BPlusTree.IntermediateNode parent;

    @Override
    public BPlusTree.IntermediateNode<K, V> parent() {
        return parent;
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

}
