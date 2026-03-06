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
package net.thevpc.nuts.util;

import net.thevpc.nuts.internal.rpi.NCollectionsRPI;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;

/**
 * @author vpc from
 * inspired from
 * https://github.com/shandysulen/B-Plus-Tree/blob/master/bplustree.java (MIT)
 * changed to support multiple stores and generic parameters
 */
public interface NBPlusTree<K extends Comparable<K>, V> extends Map<K, V> , AutoCloseable{

    public static <K extends Comparable<K>, V> NBPlusTree<K, V> of(int m, boolean allowDuplicates) {
        return NCollectionsRPI.of().btreePlus(m,allowDuplicates);
    }

    public static <K extends Comparable<K>, V> NBPlusTree<K, V> of(int m) {
        return NCollectionsRPI.of().btreePlus(m);
    }


    public long sizeLong();

    /**
     * Given a key, this method will remove the dictionary pair with the
     * corresponding key from the B+ tree.
     *
     * @param key: an integer key that corresponds with an existing dictionary
     *             pair
     */
    boolean remove(K key) ;


    public V add(K key, V value, boolean allowDuplicate) ;

    public Iterator<Map.Entry<K, V>> entryIterator() ;
    public Iterator<K> keyIterator() ;

    public List<V> search(K key) ;

    public V get(K key) ;

    public NOptional<V> getOptional(K key) ;
    /**
     * This method traverses the doubly linked list of the B+ tree and records
     * all values whose associated keys are within the range specified by
     * lowerBound and upperBound.
     *
     * @param lowerBound: (int) the lower bound of the range
     * @param upperBound: (int) the upper bound of the range
     * @return a List<V> that holds all values of dictionary pairs whose keys
     * are within the specified range
     */
    public List<V> search(K lowerBound, K upperBound) ;


    public interface Visitor<K extends Comparable<K>, V> {
        void visitLeaf(LeafNode<K, V> node, int level);

        void visitIntermediate(IntermediateNode<K, V> node, int level);
    }

    public void visit(Visitor<K, V> visitor) ;

    @Override
    void close();

    /**
     * This class represents a general node within the B+ tree and serves as a
     * superclass of InternalNode and LeafNode.
     */
    public interface Node<K extends Comparable<K>, V> {

        IntermediateNode<K, V> parent();

        boolean isLeaf();

        int size();

        int minSize();

        int maxSize();

        K firstKey();
    }

    /**
     * This class represents the internal nodes within the B+ tree that traffic
     * all search/insert/delete operations. An internal node only holds keys; it
     * does not hold dictionary pairs.
     */
    public interface IntermediateNode<K extends Comparable<K>, V> extends Node<K, V> {

        Node<K, V> child(int i);


        K key(int i);

        IntermediateNode<K, V> leftSibling();

        IntermediateNode<K, V> rightSibling();

    }

    /**
     * This class represents the leaf nodes within the B+ tree that hold
     * dictionary pairs. The leaf node has no children. The leaf node has a
     * minimum and maximum number of dictionary pairs it can hold, as specified
     * by m, the max degree of the B+ tree. The leaf nodes form a doubly linked
     * list that, i.e. each leaf node has a left and right sibling
     */
    public interface LeafNode<K extends Comparable<K>, V> extends Node<K, V> {

        List<K> keys();

        V valueAt(int index);

        K keyAt(int index);

        Entry<K, V> entryAt(int index);

        LeafNode<K, V> leftSibling();

        LeafNode<K, V> rightSibling();
    }

}
