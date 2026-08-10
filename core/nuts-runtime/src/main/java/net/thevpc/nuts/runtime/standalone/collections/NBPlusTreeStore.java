package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NBPlusTree;

import java.util.Comparator;

public interface NBPlusTreeStore<K, V> extends AutoCloseable {

    int order();

    void save();

    NBPlusTree.LeafNode<K, V> createLeafNode(NBPlusTree.IntermediateNode<K, V> parent);

    void addEntry(NBPlusTree.LeafNode<K, V> node, K k, V v, Comparator<K> comparator);

    void addEntries(NBPlusTree.LeafNode<K, V> node, NBPlusTree.Entry<K, V>[] dp, Comparator<K> comparator);

    NBPlusTree.IntermediateNode<K, V> createInternalNode();

    long size();

    void incSize(long size);

    void updateRoot(NBPlusTree.IntermediateNode<K, V> root);

    void updateParent(NBPlusTree.Node<K, V> root, NBPlusTree.IntermediateNode<K, V> parent);

    boolean isAllowDuplicates();

    void updateFirstLeaf(NBPlusTree.LeafNode<K, V> node);

    void updateLeftSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value);

    void free(NBPlusTree.Node<K, V> node);

    void updateRightSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value);

    void updateLeftSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value);

    void updateRightSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value);

    NBPlusTree.IntermediateNode<K, V> root();

    NBPlusTree.LeafNode<K, V> firstLeaf();

    int findIndexOfChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer);

    void addChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer, int index);

    void updateChildAt(NBPlusTree.IntermediateNode<K, V> node, int index, K key, NBPlusTree.Node<K, V> child);

    V updateValueAt(NBPlusTree.LeafNode<K, V> node, int index, V value);

    void removeChildAt(NBPlusTree.IntermediateNode<K, V> node, int index);

    void removeChildAt(NBPlusTree.LeafNode<K, V> node, int index);

    int indexOfKey(NBPlusTree.LeafNode<K, V> leafNode, K key, Comparator<K> comparator);

    void updateFirstKey(NBPlusTree.IntermediateNode<K, V> node, K firstKey);

    @Override
    void close();
}
