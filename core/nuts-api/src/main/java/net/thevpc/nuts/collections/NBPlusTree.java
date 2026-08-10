/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NRef;

import java.util.*;

/**
 * A self-balancing B+ Tree implementation that extends {@link Map}.
 * <p>
 * This collection is optimized for high-performance retrieval, insert, and delete operations.
 * It supports both in-memory usage (using standard Java object references) and page-based/file-based
 * persistence layouts. It can also configured to either act as a unique-key map or support duplicate keys.
 *
 * @param <K> the type of keys maintained by this map (must implement {@link Comparable})
 * @param <V> the type of mapped values
 * @author vpc
 * @since 0.8.4
 */
public interface NBPlusTree<K, V> extends Map<K, V>, AutoCloseable {

    /**
     * Creates an in-memory B+ Tree instance with a custom order and duplicate configuration.
     *
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new in-memory {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> of(int order, boolean allowDuplicates) {
        return of(order, allowDuplicates, Comparable::compareTo);
    }

    /**
     * Creates an in-memory B+ Tree instance with a custom order and duplicate configuration.
     *
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new in-memory {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> of(int order, boolean allowDuplicates, Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(order, allowDuplicates, comparator);
    }

    /**
     * Creates an in-memory B+ Tree instance with a custom order and duplicates disabled.
     *
     * @param order the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param <K>   the key type
     * @param <V>   the value type
     * @return a new in-memory {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> of(int order) {
        return NUtilsRPI.of().createBtreePlus(order, false, Comparable::compareTo);
    }

    /**
     * Creates an in-memory B+ Tree instance with a custom order and duplicates disabled.
     *
     * @param order the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param <K>   the key type
     * @param <V>   the value type
     * @return a new in-memory {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> of(int order, Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(order, false, comparator);
    }

    /**
     * Creates a page-based in-memory B+ Tree instance.
     *
     * @param pageSize        the block page size in bytes (e.g., 4096)
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based in-memory {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> ofInMemory(int pageSize, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        return ofInMemory(pageSize, order, allowDuplicates, keySerializer, valSerializer, Comparable::compareTo);
    }

    /**
     * Creates a page-based in-memory B+ Tree instance.
     *
     * @param pageSize        the block page size in bytes (e.g., 4096)
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based in-memory {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> ofInMemory(int pageSize, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer, Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(NPageStore.ofInMemory(pageSize), order, allowDuplicates, keySerializer, valSerializer, comparator);
    }

    /**
     * Creates a file-persistent B+ Tree instance.
     *
     * @param path            the target storage file path
     * @param pageSize        the block page size in bytes (e.g., 4096)
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new file-persistent {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> ofPath(NPath path, int pageSize, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        return NUtilsRPI.of().createBtreePlus(NPageStore.ofFile(path, pageSize), order, allowDuplicates, keySerializer, valSerializer, Comparable::compareTo);
    }

    /**
     * Creates a page-based B+ Tree instance using default configuration (order = 128 for file store, allowDuplicates = false).
     *
     * @param store         the underlying page store
     * @param keySerializer the serializer to encode/decode keys
     * @param valSerializer the serializer to encode/decode values
     * @param <K>           the key type
     * @param <V>           the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> of(NPageStore store, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        return NUtilsRPI.of().createBtreePlus(store, 0, false, keySerializer, valSerializer, Comparable::compareTo);
    }

    /**
     * Creates a page-based B+ Tree instance with a custom order and duplicates configuration.
     *
     * @param store           the underlying page store
     * @param order           the maximum number of children for intermediate nodes, or {@code <= 0} to use standard defaults
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> of(NPageStore store, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        return NUtilsRPI.of().createBtreePlus(store, order, allowDuplicates, keySerializer, valSerializer, Comparable::compareTo);
    }

    /**
     * Creates a page-based B+ Tree instance with default order and custom duplicates configuration.
     *
     * @param store           the underlying page store
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K extends Comparable<K>, V> NBPlusTree<K, V> of(NPageStore store, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) {
        return NUtilsRPI.of().createBtreePlus(store, 0, allowDuplicates, keySerializer, valSerializer, Comparable::compareTo);
    }

    /**
     * Creates a file-persistent B+ Tree instance.
     *
     * @param path            the target storage file path
     * @param pageSize        the block page size in bytes (e.g., 4096)
     * @param order           the maximum number of children for intermediate nodes (must be {@code >= 3})
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new file-persistent {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> ofPath(NPath path, int pageSize, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer,Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(NPageStore.ofFile(path, pageSize), order, allowDuplicates, keySerializer, valSerializer, comparator);
    }

    /**
     * Creates a page-based B+ Tree instance using default configuration (order = 128 for file store, allowDuplicates = false).
     *
     * @param store         the underlying page store
     * @param keySerializer the serializer to encode/decode keys
     * @param valSerializer the serializer to encode/decode values
     * @param <K>           the key type
     * @param <V>           the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> of(NPageStore store, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer,Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(store, 0, false, keySerializer, valSerializer, comparator);
    }

    /**
     * Creates a page-based B+ Tree instance with a custom order and duplicates configuration.
     *
     * @param store           the underlying page store
     * @param order           the maximum number of children for intermediate nodes, or {@code <= 0} to use standard defaults
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> of(NPageStore store, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer,Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(store, order, allowDuplicates, keySerializer, valSerializer, comparator);
    }

    /**
     * Creates a page-based B+ Tree instance with default order and custom duplicates configuration.
     *
     * @param store           the underlying page store
     * @param allowDuplicates whether duplicate keys are permitted in the tree
     * @param keySerializer   the serializer to encode/decode keys
     * @param valSerializer   the serializer to encode/decode values
     * @param <K>             the key type
     * @param <V>             the value type
     * @return a new page-based {@code NBPlusTree}
     */
    static <K, V> NBPlusTree<K, V> of(NPageStore store, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer,Comparator<K> comparator) {
        return NUtilsRPI.of().createBtreePlus(store, 0, allowDuplicates, keySerializer, valSerializer, comparator);
    }

    boolean remove(Object obj, NRef<V> oldValueOutHolder);

    /**
     * Returns the size of the B+ Tree as a long value.
     *
     * @return the number of key-value pairs stored in the tree
     */
    long sizeLong();

    /**
     * Inserts or updates a key-value pair.
     *
     * @param key            the key to insert/update
     * @param value          the associated value
     * @param allowDuplicate whether duplicates are allowed. If {@code false}, existing values are updated.
     *                       If {@code true}, a new duplicate entry is appended.
     * @return the old value associated with the key if duplicates are disabled and it existed, {@code null} otherwise
     */
    V add(K key, V value, boolean allowDuplicate);

    /**
     * Returns an iterator over all entries in the tree in sorted key order.
     *
     * @return an entry iterator
     */
    Iterator<Map.Entry<K, V>> entryIterator();

    /**
     * Returns an iterator over all keys in the tree in sorted order.
     *
     * @return a key iterator
     */
    Iterator<K> keyIterator();

    /**
     * Returns a list of all values associated with the exact matching key.
     * <p>
     * Useful when the tree permits duplicate keys.
     *
     * @param key the search key
     * @return a list of matching values, or an empty list if not found
     */
    List<V> search(K key);


    /**
     * Retrieves the value mapped to the given key wrapped in an {@link NOptional}.
     *
     * @param key the search key
     * @return an optional containing the value if found, or an empty optional otherwise
     */
    NOptional<V> getOptional(K key);

    /**
     * Performs a range query, returning all values whose keys fall between the specified bounds.
     *
     * @param lowerBound the lower key boundary (inclusive)
     * @param upperBound the upper key boundary (inclusive)
     * @return a list of values matching the range query
     */
    List<V> search(K lowerBound, K upperBound);

    /**
     * Visitor interface for traversing the tree structure during debugging or validation.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface Visitor<K, V> {
        /**
         * Visits a leaf node in the B+ Tree structure.
         *
         * @param node  the visited leaf node, or {@code null} if empty
         * @param level the current depth level in the tree (0-indexed)
         */
        void visitLeaf(LeafNode<K, V> node, int level);

        /**
         * Visits an intermediate node in the B+ Tree structure.
         *
         * @param node  the visited intermediate node
         * @param level the current depth level in the tree (0-indexed)
         */
        void visitIntermediate(IntermediateNode<K, V> node, int level);
    }

    /**
     * Traverses the tree structure, invoking the visitor on each encountered node.
     *
     * @param visitor the callback listener
     */
    void visit(Visitor<K, V> visitor);

    /**
     * Closes the tree, flushing metadata and committing any outstanding transactions to the underlying store.
     */
    @Override
    void close();

    /**
     * Base interface representing a node within the B+ Tree structure.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface Node<K, V> {

        /**
         * Returns the parent node of this node.
         *
         * @return the parent intermediate node, or {@code null} if this is the root node
         */
        IntermediateNode<K, V> parent();

        /**
         * Returns whether this is a leaf node.
         *
         * @return {@code true} if leaf, {@code false} if intermediate node
         */
        boolean isLeaf();

        /**
         * Returns the current number of child pointers (for intermediate nodes) or keys (for leaf nodes).
         *
         * @return current node size
         */
        int size();

        /**
         * Returns the minimum size constraint allowed for this node.
         *
         * @return minimum node size
         */
        int minSize();

        /**
         * Returns the maximum capacity limit allowed for this node.
         *
         * @return maximum node size
         */
        int maxSize();

        /**
         * Returns the leftmost/first key indexed in this subtree node.
         *
         * @return the first key
         */
        K firstKey();
    }

    /**
     * Represents an intermediate node within the B+ Tree structure that routes searches but stores no values.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface IntermediateNode<K, V> extends Node<K, V> {

        /**
         * Retrieves the child node pointer at the specified index.
         *
         * @param i index of child pointer
         * @return the child node
         */
        Node<K, V> child(int i);

        /**
         * Returns the key used as a routing guide separator.
         *
         * @param i index of the guide key
         * @return the guide key at index {@code i}
         */
        K key(int i);

        /**
         * Returns the left sibling of this node sharing the same parent.
         *
         * @return the left sibling, or {@code null} if none
         */
        IntermediateNode<K, V> leftSibling();

        /**
         * Returns the right sibling of this node sharing the same parent.
         *
         * @return the right sibling, or {@code null} if none
         */
        IntermediateNode<K, V> rightSibling();
    }

    /**
     * Represents a leaf node in the B+ Tree that contains the actual mapped entries.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    interface LeafNode<K, V> extends Node<K, V> {

        /**
         * Returns all keys stored in this leaf node.
         *
         * @return list of keys
         */
        List<K> keys();

        /**
         * Returns the value stored at the specified index inside this leaf.
         *
         * @param index internal leaf array index
         * @return the value at index
         */
        V valueAt(int index);

        /**
         * Returns the key stored at the specified index inside this leaf.
         *
         * @param index internal leaf array index
         * @return the key at index
         */
        K keyAt(int index);

        /**
         * Returns the key-value entry stored at the specified index inside this leaf.
         *
         * @param index internal leaf array index
         * @return the entry at index
         */
        Entry<K, V> entryAt(int index);

        /**
         * Returns the left sibling leaf node in the doubly linked leaf chain.
         *
         * @return the left sibling leaf, or {@code null} if this is the start leaf
         */
        LeafNode<K, V> leftSibling();

        /**
         * Returns the right sibling leaf node in the doubly linked leaf chain.
         *
         * @return the right sibling leaf, or {@code null} if this is the end leaf
         */
        LeafNode<K, V> rightSibling();
    }

}
