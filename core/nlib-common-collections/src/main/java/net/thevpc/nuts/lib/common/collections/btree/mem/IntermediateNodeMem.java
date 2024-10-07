package net.thevpc.nuts.lib.common.collections.btree.mem;

import net.thevpc.nuts.lib.common.collections.BPlusTree;

import java.util.Arrays;
import java.util.stream.Collectors;

class IntermediateNodeMem<K extends Comparable<K>, V> extends NodeMem<K, V> implements BPlusTree.IntermediateNode<K, V> {

//    Comparable[] keys;
    K firstKey;
    BPlusTree.IntermediateNode<K, V> leftSibling;
    BPlusTree.IntermediateNode<K, V> rightSibling;
    NodeMem<K, V>[] children;


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
    public BPlusTree.Node<K, V> child(int i) {
        return children[i];
    }

    @Override
    public BPlusTree.IntermediateNode<K, V> leftSibling() {
        return leftSibling;
    }

    @Override
    public BPlusTree.IntermediateNode<K, V> rightSibling() {
        return rightSibling;
    }

    /**
     * Constructor
     *
     * @param m:    the max degree of the InternalNode
     */
    IntermediateNodeMem(int m) {
        this.maxSize = m;
        this.minSize = (int) Math.ceil(m / 2.0);
        this.size = 0;
        // plus one to handle overful!!
        this.children = new NodeMem[this.maxSize + 1];
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
