package net.thevpc.nuts.util;

public abstract class NBPlusTreeStoreMemNode<K extends Comparable<K>, V> implements NBPlusTree.Node<K, V> {

    int maxSize;
    int minSize;
    int size;
    NBPlusTree.IntermediateNode parent;

    @Override
    public NBPlusTree.IntermediateNode<K, V> parent() {
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
