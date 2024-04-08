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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.util.collections.btree;

import net.thevpc.nuts.runtime.standalone.util.collections.BPlusTree;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author vpc
 */
public class BPlusTreeHelper {

    public static <K extends Comparable<K>, V> int compareEntries(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
        //entry null last
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return 1;
        }
        if (o2 == null) {
            return -1;
        }


        //key null first
        K k1 = o1.getKey();
        K k2 = o2.getKey();

        if (k1 == null && k2 == null) {
            return 0;
        }
        if (k1 == null) {
            return -1;
        }
        if (k2 == null) {
            return 1;
        }
        return k1.compareTo(k2);
    }

    public static <K extends Comparable<K>> int compareKey(K k1, K k2) {
        if (k1 == k2) {
            return 0;
        } else if (k1 == null) {
            return -1;
        } else if (k2 == null) {
            return 1;
        } else {
            return k1.compareTo(k2);
        }
    }

    /**
     * This simple method determines if the LeafNode is deficient, i.e. the
     * numPairs within the LeafNode object is below minNumPairs.
     *
     * @return a boolean indicating whether or not the LeafNode is deficient
     */
    public static <K extends Comparable<K>, V> boolean isDeficient(BPlusTree.Node<K, V> node) {
        return node.size() < node.minSize();
    }


    public static <K extends Comparable<K>, V> boolean isEmpty(BPlusTree.Node<K, V> node) {
        return node.size() == 0;
    }

    /**
     * This simple method determines if the LeafNode is full, i.e. the numPairs
     * within the LeafNode is equal to the maximum number of pairs.
     *
     * @return a boolean indicating whether or not the LeafNode is full
     */
    public static <K extends Comparable<K>, V> boolean isFull(BPlusTree.LeafNode<K, V> node) {
        return node.size() == node.maxSize();
    }

    /**
     * This simple method determines if the LeafNode object is capable of
     * lending a dictionary pair to a deficient leaf node. The LeafNode object
     * can lend a dictionary pair if its numPairs is greater than the minimum
     * number of pairs it can hold.
     *
     * @return a boolean indicating whether or not the LeafNode object can give
     * a dictionary pair to a deficient leaf node
     */
    public static <K extends Comparable<K>, V> boolean isLendable(BPlusTree.Node<K, V> node) {
        return node.size() > node.minSize();
    }


    /**
     * This simple method determines if the LeafNode object is capable of being
     * merged with, which occurs when the number of pairs within the LeafNode
     * object is equal to the minimum number of pairs it can hold.
     *
     * @return a boolean indicating whether or not the LeafNode object can be
     * merged with
     */
    public static <K extends Comparable<K>, V> boolean isMergeable(BPlusTree.LeafNode<K, V> node) {
        return node.size() == node.minSize();
    }


    /**
     * This simple method determines if the InternalNode is capable of being
     * merged with. An InternalNode can be merged with if it has the minimum
     * degree of children.
     *
     * @return a boolean indicating whether or not the InternalNode can be
     * merged with
     */
    public static <K extends Comparable<K>, V> boolean isMergeable(BPlusTree.IntermediateNode<K, V> node) {
        return node.size() == node.minSize();
    }

    /**
     * This simple method determines if the InternalNode is considered overfull,
     * i.e. the InternalNode object's current degree is one more than the
     * specified maximum.
     *
     * @return a boolean indicating if the InternalNode is overfull
     */
    public static <K extends Comparable<K>, V> boolean isOverfull(BPlusTree.IntermediateNode<K, V> node) {
        return node.size() == node.maxSize() + 1;
    }

    public static boolean eq(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    public static <K extends Comparable<K>> int binarySearchKeys(List<K> a, int numPairs, K key) {
        Comparator<K> c = new Comparator<K>() {
            @Override
            public int compare(K o1, K o2) {
                return compareKey(o1, o2);
            }
        };
        int fromIndex = 0;
        int toIndex = numPairs;
        int low = fromIndex;
        int high = toIndex - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            K midVal = a.get(mid);
            int cmp = c.compare(midVal, key);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }

    /**
     * This method performs a standard linear search on a sorted
     * DictionaryPair[] and returns the index of the first null entry found.
     * Otherwise, this method returns a -1. This method is primarily used in
     * place of binarySearch() when the target t = null.
     *
     * @param dps: list of dictionary pairs sorted by key within leaf node
     * @return index of the target value if found, else -1
     */
    private static int linearNullSearch(BPlusTree.Entry[] dps) {
        for (int i = 0; i < dps.length; i++) {
            if (dps[i] == null) {
                return i;
            }
        }
        return -1;
    }
}
