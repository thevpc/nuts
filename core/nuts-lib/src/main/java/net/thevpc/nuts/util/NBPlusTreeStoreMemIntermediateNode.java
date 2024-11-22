package net.thevpc.nuts.util;

import java.util.Arrays;
import java.util.stream.Collectors;

class NBPlusTreeStoreMemIntermediateNode<K extends Comparable<K>, V> extends NBPlusTreeStoreMemNode<K, V> implements NBPlusTree.IntermediateNode<K, V> {

//    Comparable[] keys;
    K firstKey;
    NBPlusTree.IntermediateNode<K, V> leftSibling;
    NBPlusTree.IntermediateNode<K, V> rightSibling;
    NBPlusTreeStoreMemNode<K, V>[] children;


    public boolean isLeaf() {
        return false;
    }

    @Override
    public K firstKey() {
        return firstKey;
    }

    @Override
    public K key(int i) {
        return children[i].firstKey();
    }

    @Override
    public NBPlusTree.Node<K, V> child(int i) {
        return children[i];
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> leftSibling() {
        return leftSibling;
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> rightSibling() {
        return rightSibling;
    }

    /**
     * Constructor
     *
     * @param m:    the max degree of the InternalNode
     */
    NBPlusTreeStoreMemIntermediateNode(int m) {
        this.maxSize = m;
        this.minSize = (int) Math.ceil(m / 2.0);
        this.size = 0;
        // plus one to handle overful!!
        this.children = new NBPlusTreeStoreMemNode[this.maxSize + 1];
    }

    @Override
    public String toString() {
        return "IntermediateNode{" +
                "size=" + size +
                ", firstKey=" + firstKey +
                ", children=[" + Arrays.stream(children).map(x -> x == null ? "null" : String.valueOf(x.firstKey())).collect(Collectors.joining(",")) + "]" +
                '}';
    }

}
