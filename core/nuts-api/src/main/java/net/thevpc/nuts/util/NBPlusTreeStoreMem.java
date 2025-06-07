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

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author vpc
 */
public class NBPlusTreeStoreMem<K extends Comparable<K>, V> implements NBPlusTreeStore<K, V> {

    protected boolean allowDuplicates;
    protected int m;
    protected long size;
    protected NBPlusTree.IntermediateNode<K, V> root;
    protected NBPlusTree.LeafNode<K, V> firstLeaf;

    public NBPlusTreeStoreMem() {
    }

    public NBPlusTreeStoreMem(int m, boolean allowDuplicates) {
        this.m = m;
        this.allowDuplicates = allowDuplicates;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    @Override
    public void updateParent(NBPlusTree.Node<K, V> root, NBPlusTree.IntermediateNode<K, V> parent) {
        ((NBPlusTreeStoreMemNode<K, V>) root).parent = parent;
    }


    @Override
    public void save() {

    }

    @Override
    public NBPlusTree.LeafNode<K, V> createLeafNode(NBPlusTree.IntermediateNode<K, V> parent) {
        return new NBPlusTreeStoreMemLeafNode<>(this.m, parent);
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> createInternalNode() {
        return new NBPlusTreeStoreMemIntermediateNode<K, V>(m);
    }

    public long size() {
        return size;
    }

    @Override
    public void incSize(long sizeDiff) {
        this.size += sizeDiff;
    }

    public NBPlusTree.IntermediateNode<K, V> root() {
        return root;
    }

    public NBPlusTree.LeafNode<K, V> firstLeaf() {
        return firstLeaf;
    }

    public int m() {
        return m;
    }

    @Override
    public void updateRoot(NBPlusTree.IntermediateNode<K, V> node) {
        this.root = node;
    }

    public void updateFirstLeaf(NBPlusTree.LeafNode<K, V> node) {
        this.firstLeaf = node;
    }

    @Override
    public void free(NBPlusTree.Node<K, V> node) {

    }

    public void updateLeftSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value) {
        NBPlusTreeStoreMemIntermediateNode<K, V> m = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
        m.leftSibling = (NBPlusTreeStoreMemIntermediateNode<K, V>) value;
    }

    public void updateRightSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value) {
        NBPlusTreeStoreMemIntermediateNode<K, V> m = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
        m.rightSibling = (NBPlusTreeStoreMemIntermediateNode<K, V>) value;
    }

    public void updateLeftSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value) {
        NBPlusTreeStoreMemLeafNode<K, V> m = (NBPlusTreeStoreMemLeafNode<K, V>) node;
        m.leftSibling = (NBPlusTreeStoreMemLeafNode<K, V>) value;
    }

    public void updateRightSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value) {
        NBPlusTreeStoreMemLeafNode<K, V> m = (NBPlusTreeStoreMemLeafNode<K, V>) node;
        m.rightSibling = (NBPlusTreeStoreMemLeafNode<K, V>) value;
    }


    @Override
    public void addEntry(NBPlusTree.LeafNode<K, V> node, K k, V v) {
        NBPlusTreeStoreMemLeafNode<K, V> nn = (NBPlusTreeStoreMemLeafNode<K, V>) node;
        AbstractMap.SimpleEntry<K, V> nv = new AbstractMap.SimpleEntry<>(k, v);

//        nn.dictionary[nn.size] = nv;
//        nn.size++;
//        Arrays.sort(nn.dictionary, BPlusTreeHelper::compareEntries);

        int index = Arrays.binarySearch(nn.dictionary, 0, nn.size, nv, NBPlusTreeHelper::compareEntries);
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
    public void addEntries(NBPlusTree.LeafNode<K, V> node, NBPlusTree.Entry<K, V>[] orderedElements) {
        NBPlusTreeStoreMemLeafNode<K, V> nn = (NBPlusTreeStoreMemLeafNode<K, V>) node;
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
                if (NBPlusTreeHelper.compareEntries(arr1[i], arr2[j]) < 0) {
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


    public int findIndexOfChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> child) {
        NBPlusTreeStoreMemIntermediateNode<K, V> nn = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
        for (int i = 0; i < nn.children.length; i++) {
            if (NBPlusTreeHelper.eq(nn.children[i], child)) {
                return i;
            }
        }
        return -1;
    }

    public void addChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer, int index) {
        NBPlusTreeStoreMemIntermediateNode<K, V> nn = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
        if (index < 0) {
            index = nn.size;
        }
        System.arraycopy(nn.children, index, nn.children, index + 1, nn.size - index);
//        for (int i = nn.size - 1; i >= index; i--) {
//            nn.children[i + 1] = nn.children[i];
//        }
        nn.children[index] = (NBPlusTreeStoreMemNode<K, V>) pointer;
        nn.size++;
        if (index == 0) {
            nn.firstKey = nn.children[0].firstKey();
        }
//        check(nn.children, nn.size);
    }

    @Override
    public void updateChildAt(NBPlusTree.IntermediateNode<K, V> node, int index, K key, NBPlusTree.Node<K, V> child) {
        NBPlusTreeStoreMemIntermediateNode<K, V> nn = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
        nn.children[index] = (NBPlusTreeStoreMemNode<K, V>) child;
        if (index == 0) {
            nn.firstKey = nn.children[0].firstKey();
        }
//        check(nn.children, nn.size);
    }

    @Override
    public V updateValueAt(NBPlusTree.LeafNode<K, V> node, int index, V value) {
        NBPlusTreeStoreMemLeafNode<K, V> nn = (NBPlusTreeStoreMemLeafNode<K, V>) node;
        return nn.setValueAt(index, value);
    }

    /**
     * Given an index, this method sets the dictionary pair at that index within
     * the dictionary to null.
     *
     * @param index: the location within the dictionary to be set to null
     */
    public void removeChildAt(NBPlusTree.LeafNode<K, V> node, int index) {
        NBPlusTreeStoreMemLeafNode<K, V> nn = (NBPlusTreeStoreMemLeafNode<K, V>) node;
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
    public int indexOfKey(NBPlusTree.LeafNode<K, V> node, K key) {
        NBPlusTreeStoreMemLeafNode<K, V> nn = (NBPlusTreeStoreMemLeafNode<K, V>) node;
        AbstractMap.SimpleEntry e = new AbstractMap.SimpleEntry(key, null);
        return Arrays.binarySearch(nn.dictionary, 0, nn.size, e, NBPlusTreeHelper::compareEntries);
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


    public void removeChildAt(NBPlusTree.IntermediateNode<K, V> node, int index) {
        NBPlusTreeStoreMemIntermediateNode<K, V> nn = (NBPlusTreeStoreMemIntermediateNode<K, V>) node;
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
