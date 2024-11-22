/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
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

/**
 * @author vpc
 */
public interface NBPlusTreeStore<K extends Comparable<K>, V> {

    int m();

    void save();

    NBPlusTree.LeafNode<K, V> createLeafNode(NBPlusTree.IntermediateNode<K, V> parent);

    /**
     * This method attempts to insert a dictionary pair within the dictionary of
     * the LeafNode object. If it succeeds, numPairs increments, the dictionary
     * is sorted, and the boolean true is returned. If the method fails, the
     * boolean false is returned.
     *
     * @return a boolean indicating whether or not the insert was successful
     */
    void addEntry(NBPlusTree.LeafNode<K, V> node, K k, V v);

    void addEntries(NBPlusTree.LeafNode<K, V> node, NBPlusTree.Entry<K, V>[] dp);

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


    /**
     * Given a Node pointer, this method will return the index of where the
     * pointer lies within the childPointers instance variable. If the pointer
     * can't be found, the method returns -1.
     *
     * @param pointer: a Node pointer that may lie within the childPointers
     *                 instance variable
     * @return the index of 'pointer' within childPointers, or -1 if 'pointer'
     * can't be found
     */
    int findIndexOfChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer);

    /**
     * Given a pointer to a Node object and an integer index, this method
     * inserts the pointer at the specified index within the childPointers
     * instance variable. As a result of the insert, some pointers may be
     * shifted to the right of the index.
     *
     * @param pointer: the Node pointer to be inserted
     * @param index:   the index at which the insert is to take place
     */

    void addChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer, int index);


    void updateChildAt(NBPlusTree.IntermediateNode<K, V> node, int index, K key, NBPlusTree.Node<K, V> child);

    V updateValueAt(NBPlusTree.LeafNode<K, V> node, int index, V value);


    /**
     * This method sets childPointers[index] to null and additionally decrements
     * the current degree of the InternalNode.
     *
     * @param index: the location within childPointers to be set to null
     */
    void removeChildAt(NBPlusTree.IntermediateNode<K, V> node, int index);

    void removeChildAt(NBPlusTree.LeafNode<K, V> node, int index);

    int indexOfKey(NBPlusTree.LeafNode<K, V> leafNode, K key);


}
