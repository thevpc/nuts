package net.thevpc.nuts.runtime.standalone.util.collections.btree.mem;

import net.thevpc.nuts.runtime.standalone.util.collections.BPlusTree;

import java.util.*;

/**
 * This class represents the leaf nodes within the B+ tree that hold
 * dictionary pairs. The leaf node has no children. The leaf node has a
 * minimum and maximum number of dictionary pairs it can hold, as specified
 * by m, the max degree of the B+ tree. The leaf nodes form a doubly linked
 * list that, i.e. each leaf node has a left and right sibling
 */
public class LeafNodeMem<K extends Comparable<K>, V> extends NodeMem<K, V> implements BPlusTree.LeafNode<K, V> {

    BPlusTree.LeafNode<K, V> leftSibling;
    BPlusTree.LeafNode<K, V> rightSibling;
    Map.Entry<K, V>[] dictionary;

    /**
     * Given an index, this method sets the dictionary pair at that index
     * within the dictionary to null.
     *
     * @param index: the location within the dictionary to be set to null
     */
    public void delete(int index) {

        // Delete dictionary pair from leaf
        this.dictionary[index] = null;

        // Decrement numPairs
        size--;
    }

    @Override
    public K firstKey() {
        return /*this.dictionary.length == 0 ? null :
                this.dictionary[0]==null?null:*/
                this.dictionary[0].getKey();
    }

    @Override
    public boolean isLeaf() {
        return true;
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

    public V setValueAt(int index, V value) {
        BPlusTree.Entry<K, V> e = this.dictionary[index];
        V old=e.getValue();
        this.dictionary[index]=new AbstractMap.SimpleEntry<>(e.getKey(), value);
        return old;
    }

    @Override
    public BPlusTree.Entry<K, V> entryAt(int index) {
        return dictionary[index];
    }


    @Override
    public BPlusTree.LeafNode<K, V> leftSibling() {
        return leftSibling;
    }

    @Override
    public BPlusTree.LeafNode<K, V> rightSibling() {
        return rightSibling;
    }

    /**
     * Constructor
     *
     * @param m:      order of B+ tree that is used to calculate maxNumPairs and
     *                minNumPairs
     * @param parent: parent of newly created child LeafNode
     */
    public LeafNodeMem(int m, BPlusTree.IntermediateNode<K, V> parent) {
        this.maxSize = m - 1;
        this.minSize = (int) (Math.ceil(m / 2) - 1);
        this.dictionary = new BPlusTree.Entry[m];
        this.size = 0;
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "LeafNodeMem{" +
                "size=" + size +
                ", firstKey=" + firstKey() +
                ", values=" + Arrays.toString(dictionary) +
                '}';
    }
}
