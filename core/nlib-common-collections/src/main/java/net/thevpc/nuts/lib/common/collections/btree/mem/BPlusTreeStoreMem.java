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
package net.thevpc.nuts.lib.common.collections.btree.mem;

import net.thevpc.nuts.lib.common.collections.BPlusTree;
import net.thevpc.nuts.lib.common.collections.btree.BPlusTreeHelper;
import net.thevpc.nuts.lib.common.collections.btree.BPlusTreeStore;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author vpc
 */
public class BPlusTreeStoreMem<K extends Comparable<K>, V> implements BPlusTreeStore<K, V> {

    protected boolean allowDuplicates;
    protected int m;
    protected long size;
    protected BPlusTree.IntermediateNode<K, V> root;
    protected BPlusTree.LeafNode<K, V> firstLeaf;

    public BPlusTreeStoreMem() {
    }

    public BPlusTreeStoreMem(int m, boolean allowDuplicates) {
        this.m = m;
        this.allowDuplicates = allowDuplicates;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    @Override
    public void updateParent(BPlusTree.Node<K, V> root, BPlusTree.IntermediateNode<K, V> parent) {
        ((NodeMem<K, V>) root).parent = parent;
    }


    @Override
    public void save() {

    }

    @Override
    public BPlusTree.LeafNode<K, V> createLeafNode(BPlusTree.IntermediateNode<K, V> parent) {
        return new LeafNodeMem<>(this.m, parent);
    }

    @Override
    public BPlusTree.IntermediateNode<K, V> createInternalNode() {
        return new IntermediateNodeMem<K, V>(m);
    }

    public long size() {
        return size;
    }

    @Override
    public void incSize(long sizeDiff) {
        this.size += sizeDiff;
    }

    public BPlusTree.IntermediateNode<K, V> root() {
        return root;
    }

    public BPlusTree.LeafNode<K, V> firstLeaf() {
        return firstLeaf;
    }

    public int m() {
        return m;
    }

    @Override
    public void updateRoot(BPlusTree.IntermediateNode<K, V> node) {
        this.root = node;
    }

    public void updateFirstLeaf(BPlusTree.LeafNode<K, V> node) {
        this.firstLeaf = node;
    }

    @Override
    public void free(BPlusTree.Node<K, V> node) {

    }

    public void updateLeftSibling(BPlusTree.IntermediateNode<K, V> node, BPlusTree.IntermediateNode<K, V> value) {
        IntermediateNodeMem<K, V> m = (IntermediateNodeMem<K, V>) node;
        m.leftSibling = (IntermediateNodeMem<K, V>) value;
    }

    public void updateRightSibling(BPlusTree.IntermediateNode<K, V> node, BPlusTree.IntermediateNode<K, V> value) {
        IntermediateNodeMem<K, V> m = (IntermediateNodeMem<K, V>) node;
        m.rightSibling = (IntermediateNodeMem<K, V>) value;
    }

    public void updateLeftSibling(BPlusTree.LeafNode<K, V> node, BPlusTree.LeafNode<K, V> value) {
        LeafNodeMem<K, V> m = (LeafNodeMem<K, V>) node;
        m.leftSibling = (LeafNodeMem<K, V>) value;
    }

    public void updateRightSibling(BPlusTree.LeafNode<K, V> node, BPlusTree.LeafNode<K, V> value) {
        LeafNodeMem<K, V> m = (LeafNodeMem<K, V>) node;
        m.rightSibling = (LeafNodeMem<K, V>) value;
    }


    @Override
    public void addEntry(BPlusTree.LeafNode<K, V> node, K k, V v) {
        LeafNodeMem<K, V> nn = (LeafNodeMem<K, V>) node;
        AbstractMap.SimpleEntry<K, V> nv = new AbstractMap.SimpleEntry<>(k, v);

//        nn.dictionary[nn.size] = nv;
//        nn.size++;
//        Arrays.sort(nn.dictionary, BPlusTreeHelper::compareEntries);

        int index = Arrays.binarySearch(nn.dictionary, 0, nn.size, nv, BPlusTreeHelper::compareEntries);
        if (index >= 0) {
            while (index < nn.size && Objects.equals(nn.dictionary[index].getKey(), k)) {
                index++;
            }
            System.arraycopy(nn.dictionary, index, nn.dictionary, index + 1, nn.size - index);
            nn.dictionary[index] = nv;
        } else {
            index = -index - 1;
        }
        System.arraycopy(nn.dictionary, index, nn.dictionary, index + 1, nn.size - index);
        nn.dictionary[index] = nv;
        nn.size++;
//        check(nn.dictionary, nn.size);
    }

    @Override
    public void addEntries(BPlusTree.LeafNode<K, V> node, BPlusTree.Entry<K, V>[] orderedElements) {
        LeafNodeMem<K, V> nn = (LeafNodeMem<K, V>) node;
        if (orderedElements != null && orderedElements.length > 0) {
            Map.Entry<K, V> firstNext = orderedElements[0];
//            int p=Arrays.binarySearch(nn.dictionary,0,nn.size,firstNext,BPlusTreeHelper::compareEntries);
//            if(p<0){
//                p=-(p+1);
//            }
            Map.Entry<K, V>[] arr1 = new Map.Entry[nn.size];
            System.arraycopy(nn.dictionary, 0, arr1, 0, arr1.length);
            Map.Entry<K, V>[] arr2 = orderedElements;
            int i = 0, j = 0, k = 0;
            int n1 = nn.size;
            int n2 = arr2.length;
            // Traverse both array
            while (i < n1 && j < n2) {
                if (BPlusTreeHelper.compareEntries(arr1[i], arr2[j]) < 0) {
                    nn.dictionary[k++] = arr1[i++];
                } else {
                    nn.dictionary[k++] = arr2[j++];
                }
            }
            System.arraycopy(arr1, i, nn.dictionary, k, n1 - i);
            System.arraycopy(arr2, j, nn.dictionary, k + n1 - i, n2 - j);
            nn.size += orderedElements.length;
//            check(nn.dictionary, nn.size);
//            while (i < n1) {
//                nn.dictionary[k++] = arr1[i++];
//            }
//            while (j < n2) {
//                nn.dictionary[k++] = arr2[j++];
//            }


//            for (BPlusTree.Entry dp : orderedElements) {
//                if (dp != null) {
//                    nn.dictionary[nn.size] = dp;
//                    nn.size++;
//                    changed = true;
//                } else {
//                    break;
//                }
//            }
//            if (changed) {
//                Arrays.sort(nn.dictionary, BPlusTreeHelper::compareEntries);
//            }
        }
    }


    public int findIndexOfChild(BPlusTree.IntermediateNode<K, V> node, BPlusTree.Node<K, V> child) {
        IntermediateNodeMem<K, V> nn = (IntermediateNodeMem<K, V>) node;
        for (int i = 0; i < nn.children.length; i++) {
            if (BPlusTreeHelper.eq(nn.children[i], child)) {
                return i;
            }
        }
        return -1;
    }

    public void addChild(BPlusTree.IntermediateNode<K, V> node, BPlusTree.Node<K, V> pointer, int index) {
        IntermediateNodeMem<K, V> nn = (IntermediateNodeMem<K, V>) node;
        if (index < 0) {
            index = nn.size;
        }
        System.arraycopy(nn.children, index, nn.children, index + 1, nn.size - index);
//        for (int i = nn.size - 1; i >= index; i--) {
//            nn.children[i + 1] = nn.children[i];
//        }
        nn.children[index] = (NodeMem<K, V>) pointer;
        nn.size++;
        if (index == 0) {
            nn.firstKey = nn.children[0].firstKey();
        }
//        check(nn.children, nn.size);
    }

    @Override
    public void updateChildAt(BPlusTree.IntermediateNode<K, V> node, int index, K key, BPlusTree.Node<K, V> child) {
        IntermediateNodeMem<K, V> nn = (IntermediateNodeMem<K, V>) node;
        nn.children[index] = (NodeMem<K, V>) child;
        if (index == 0) {
            nn.firstKey = nn.children[0].firstKey();
        }
//        check(nn.children, nn.size);
    }

    @Override
    public V updateValueAt(BPlusTree.LeafNode<K, V> node, int index, V value) {
        LeafNodeMem<K, V> nn = (LeafNodeMem<K, V>) node;
        return nn.setValueAt(index, value);
    }

    /**
     * Given an index, this method sets the dictionary pair at that index within
     * the dictionary to null.
     *
     * @param index: the location within the dictionary to be set to null
     */
    public void removeChildAt(BPlusTree.LeafNode<K, V> node, int index) {
        LeafNodeMem<K, V> nn = (LeafNodeMem<K, V>) node;
        System.arraycopy(nn.dictionary, index + 1, nn.dictionary, index, nn.size - index - 1);
        nn.size--;
        nn.dictionary[nn.size] = null;
//        check(nn.dictionary, nn.size);
//        // Delete dictionary pair from leaf
//        nn.dictionary[index] = null;
//        for (int i = index + 1; i < nn.dictionary.length; i++) {
//            nn.dictionary[i - 1] = nn.dictionary[i];
//            if (nn.dictionary[i] == null) {
//                break;
//            }
//        }
//        // Decrement numPairs
//        nn.size--;
    }

    @Override
    public int indexOfKey(BPlusTree.LeafNode<K, V> node, K key) {
        LeafNodeMem<K, V> nn = (LeafNodeMem<K, V>) node;
        AbstractMap.SimpleEntry e = new AbstractMap.SimpleEntry(key, null);
        return Arrays.binarySearch(nn.dictionary, 0, nn.size, e, BPlusTreeHelper::compareEntries);
    }

    //    private void check(Object[] a, int size) {
//        for (int i = 0; i < size; i++) {
//            if (a[i] == null) {
//                throw new IllegalArgumentException("error");
//            }
//        }
//        for (int i = size; i < a.length; i++) {
//            if (a[i] != null) {
//                throw new IllegalArgumentException("error");
//            }
//        }
//    }


    public void removeChildAt(BPlusTree.IntermediateNode<K, V> node, int index) {
        IntermediateNodeMem<K, V> nn = (IntermediateNodeMem<K, V>) node;
        System.arraycopy(nn.children, index + 1, nn.children, index, nn.size - index - 1);
        nn.size--;
        nn.children[nn.size] = null;
        if (index == 0) {
            nn.firstKey = nn.children[0] == null ? null : nn.children[0].firstKey();
        }
//        check(nn.children, nn.size);
//
//
//        BPlusTree.Node old = nn.children[index];
//        nn.children[index] = null;
//        for (int i = index + 1; i < nn.children.length; i++) {
//            nn.children[i - 1] = nn.children[i];
//            if (nn.children[i] == null) {
//                break;
//            }
//        }
//        if (index == 0) {
//            nn.firstKey = nn.children[0] == null ? null : nn.children[0].firstKey();
//        }
//        nn.size--;
    }

}
