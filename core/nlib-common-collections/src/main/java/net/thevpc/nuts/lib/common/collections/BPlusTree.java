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
package net.thevpc.nuts.lib.common.collections;

import net.thevpc.nuts.lib.common.collections.btree.BPlusTreeHelper;
import net.thevpc.nuts.lib.common.collections.btree.BPlusTreeStore;
import net.thevpc.nuts.lib.common.collections.btree.mem.BPlusTreeStoreMem;
import net.thevpc.nuts.util.NOptional;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Function;

/**
 * @author vpc from
 * inspired from
 * https://github.com/shandysulen/B-Plus-Tree/blob/master/bplustree.java (MIT)
 * changed to support multiple stores and generic parameters
 */
public class BPlusTree<K extends Comparable<K>, V> extends AbstractMap<K, V> {

    private BPlusTreeStore<K, V> store;
    private int m;

    /**
     * Constructor
     */
    public static <K extends Comparable<K>, V> BPlusTree<K, V> of(int m, boolean allowDuplicates) {
        return new BPlusTree<K, V>(new BPlusTreeStoreMem<>(m, allowDuplicates));
    }

    public static <K extends Comparable<K>, V> BPlusTree<K, V> of(int m) {
        return new BPlusTree<K, V>(new BPlusTreeStoreMem<>(m, false));
    }

    public BPlusTree(BPlusTreeStore<K, V> store) {
        this.store = store;
        m = store.m();
    }

    /**
     * This method starts at the root of the B+ tree and traverses down the tree
     * via key comparisons to the corresponding leaf node that holds 'key'
     * within its dictionary.
     *
     * @param key: the unique key that lies within the dictionary of a LeafNode
     *             object
     * @return the LeafNode object that contains the key within its dictionary
     */
    private LeafNode<K, V> findLeafNode(K key) {
        IntermediateNode<K, V> node = store.root();
        if (node == null) {
            return store.firstLeaf();
        }
        return findLeafNode(node, key);
    }

    private LeafNode<K, V> findLeafNode(IntermediateNode<K, V> node, K key) {

        // Initialize keys and index variable
        int i;

        // Find next node on path to appropriate leaf node
        int size = node.size();
//        List<K> ll=new AbstractList<K>(){
//            @Override
//            public K get(int index) {
//                return node.key(index);
//            }
//
//            @Override
//            public int size() {
//                return size;
//            }
//        };
//        int i2 = Collections.binarySearch(ll, key);
//        if(i2<0){
//            i2=-i2-1;
//        }
//        i=i2;
        for (i = 0; i < size - 1; i++) {
            K key1 = node.key(i + 1);
            if (BPlusTreeHelper.compareKey(key, key1) < 0) {
                break;
            }
        }

        /* Return node if it is a LeafNode object,
		   otherwise repeat the search function a level down */
        Node<K, V> child = node.child(i);
        if (child.isLeaf()) {
            return (LeafNode<K, V>) child;
        } else {
            return findLeafNode((IntermediateNode<K, V>) child, key);
        }
    }

    /**
     * This is a simple method that returns the midpoint (or lower bound
     * depending on the context of the method invocation) of the max degree m of
     * the B+ tree.
     *
     * @return (int) midpoint/lower bound
     */
    private int getMidpoint() {
        return (int) Math.ceil((m + 1) / 2.0) - 1;
    }

    /**
     * Given a deficient InternalNode in, this method remedies the deficiency
     * through borrowing and merging.
     *
     * @param in: a deficient InternalNode
     */
    private void handleDeficiency(IntermediateNode<K, V> in) {
        if (in == null) {
            return;
        }
        if (in.size() == 0) {
            //okk
        } else if (!BPlusTreeHelper.isDeficient(in)) {
            return;
        }

        IntermediateNode<K, V> sibling;
        IntermediateNode<K, V> parent = in.parent();

        // Remedy deficient root node
        if (BPlusTreeHelper.eq(store.root(), in)) {
            for (int i = 0; i < in.size(); i++) {
                if (in.child(i) != null) {
                    if (in.child(i).isLeaf()) {
                        store.updateRoot(null);
                    } else {
                        store.updateRoot((IntermediateNode<K, V>) in.child(i));
                        store.updateParent(store.root(), null);
                    }
                }
            }
        } // Borrow:
        else if (in.leftSibling() != null && BPlusTreeHelper.isLendable(in.leftSibling())) {
            sibling = in.leftSibling();
        } else if (in.rightSibling() != null && BPlusTreeHelper.isLendable(in.rightSibling())) {
            sibling = in.rightSibling();

            // Copy 1 key and pointer from sibling (atm just 1 key)
            Node<K, V> node = sibling.child(0);
            store.updateChildAt(in, in.size(), parent.key(0), node);
            store.removeChildAt(sibling, 0);
        } // Merge:
        else if (in.leftSibling() != null && BPlusTreeHelper.isMergeable(in.leftSibling())) {

        } else if (in.rightSibling() != null && BPlusTreeHelper.isMergeable(in.rightSibling())) {
            sibling = in.rightSibling();

            // Copy rightmost key in parent to beginning of sibling's keys &
            // delete key from parent
            int childrenCount = in.size();

            // Copy in's child pointer over to sibling's list of child pointers
            for (int i = childrenCount - 1; i >= 0; i++) {
                store.addChild(sibling, in.child(i), 0);
                store.updateParent(in.child(i), sibling);
            }

            // Delete child pointer from grandparent to deficient node
            store.removeChildAt(parent, store.findIndexOfChild(parent, in));

            // Remove left sibling
            store.updateLeftSibling(sibling, in.leftSibling());
            store.free(in);
        }

        // Handle deficiency a level up if it exists
        handleDeficiency(parent);
        store.save();
    }

    /**
     * This is a simple method that determines if the B+ tree is empty or not.
     *
     * @return a boolean indicating if the B+ tree is empty or not
     */
    public boolean isEmpty() {
        return store.firstLeaf() == null;
    }

    /**
     * When an insertion into the B+ tree causes an overfull node, this method
     * is called to remedy the issue, i.e. to split the overfull node. This
     * method calls the sub-methods of splitKeys() and splitChildPointers() in
     * order to split the overfull node.
     *
     * @param in: an overfull InternalNode that is to be split
     */
    private void splitInternalNode(IntermediateNode<K, V> in) {
        // Acquire parent
        IntermediateNode<K, V> parent = in.parent();

        // Split keys and pointers in half
        int midpoint = getMidpoint();
//        K newParentKey = in.key(midpoint);

        ////
//        Comparable<K>[] halfKeys = new Comparable[store.m()];
//
//        // Remove split-indexed value from keys
////        store.updateKeyAt(in, midpoint, null);
//        int keysCount = in.keysCount();
//
//        // Copy half of the values into halfKeys while updating original keys
//        for (int i = midpoint + 1; i < keysCount; i++) {
//            halfKeys[i - midpoint - 1] = in.key(i);
//        }

        ////
        int childrenCount = in.size();
        Node<K, V>[] halfPointers = new Node[m + 1];

        // Copy half of the values into halfPointers while updating original keys
        for (int i = childrenCount - 1; i >= midpoint + 1; i--) {
            halfPointers[i - midpoint - 1] = in.child(i);
            store.removeChildAt(in, i);
        }

        // Create new sibling internal node and add half of keys and pointers
        IntermediateNode<K, V> sibling = store.createInternalNode();
        for (int i = 0; i < halfPointers.length; i++) {
            Node<K, V> pointer = halfPointers[i];
            if (pointer != null) {
//                store.updateKeyAt(sibling, sibling.size(), (K) halfKeys[i]);
                store.addChild(sibling, pointer, -1);
                store.updateParent(pointer, sibling);
            }
        }

        // Make internal nodes siblings of one another
        store.updateRightSibling(sibling, in.rightSibling());
        if (sibling.rightSibling() != null) {
            store.updateLeftSibling(sibling.rightSibling(), sibling);
        }
        store.updateRightSibling(in, sibling);
        store.updateLeftSibling(sibling, in);

        if (parent == null) {
            // Create new root node and add midpoint key and pointers
            IntermediateNode<K, V> newRoot = store.createInternalNode();
            store.addChild(newRoot, in, -1);
            store.addChild(newRoot, sibling, -1);
            store.updateRoot(newRoot);
            // Add pointers from children to parent
            store.updateParent(in, newRoot);
            store.updateParent(sibling, newRoot);

        } else {

            // Add key to parent
//            store.updateKeyAt(parent, parent.degree() - 1, newParentKey);

            // Set up pointer to new sibling
            int pointerIndex = store.findIndexOfChild(parent, in) + 1;
            store.addChild(parent, sibling, pointerIndex);
            store.updateParent(sibling, parent);
        }
        store.save();
    }

    /*~~~~~~~~~~~~~~~~ API: DELETE, INSERT, SEARCH ~~~~~~~~~~~~~~~~*/
    public int size() {
        return (int) store.size();
    }

    public long sizeLong() {
        return store.size();
    }

    /**
     * Given a key, this method will remove the dictionary pair with the
     * corresponding key from the B+ tree.
     *
     * @param key: an integer key that corresponds with an existing dictionary
     *             pair
     */
    public boolean remove(K key) {
        if (isEmpty()) {

            /* Flow of execution goes here when B+ tree has no dictionary pairs */
            //System.err.println("Invalid Delete: The B+ tree is currently empty.");
            return false;
        } else {

            // Get leaf node and attempt to find index of key to delete
            LeafNode<K, V> leafNode = findLeafNode(key);

            int dpIndex = store.indexOfKey(leafNode, key);

            if (dpIndex < 0) {
                return false;
            } else {

                // Successfully delete the dictionary pair
                store.removeChildAt(leafNode, dpIndex);

                Node<K, V> nn = leafNode;
                while (nn != null) {
                    // Check for deficiencies
                    if (BPlusTreeHelper.isEmpty(nn)) {
                        if (nn.isLeaf()) {
                            LeafNode<K, V> ln = (LeafNode<K, V>) nn;
                            LeafNode<K, V> oldLeft = ln.leftSibling();
                            LeafNode<K, V> oldRight = ln.rightSibling();
                            if (oldRight != null) {
                                store.updateLeftSibling(oldRight, oldLeft);
                            }
                            if (oldLeft != null) {
                                store.updateRightSibling(oldLeft, oldRight);
                            }
                            if (ln.parent() != null) {
                                int indexOfChild = store.findIndexOfChild(ln.parent(), ln);
                                store.removeChildAt(ln.parent(), indexOfChild);
                            }
                        } else {
                            IntermediateNode<K, V> ln = (IntermediateNode<K, V>) nn;
                            IntermediateNode<K, V> oldLeft = ln.leftSibling();
                            IntermediateNode<K, V> oldRight = ln.rightSibling();
                            if (oldRight != null) {
                                store.updateLeftSibling(oldRight, oldLeft);
                            }
                            if (oldLeft != null) {
                                store.updateRightSibling(oldLeft, oldRight);
                            }
                            if (ln.parent() != null) {
                                int indexOfChild = store.findIndexOfChild(ln.parent(), ln);
                                store.removeChildAt(ln.parent(), indexOfChild);
                            }
                        }
                        store.free(nn);
                    } else if (BPlusTreeHelper.isDeficient(nn)) {
                        if (nn.isLeaf()) {
                            LeafNode<K, V> ln = (LeafNode<K, V>) nn;
                            LeafNode<K, V> sibling;
                            IntermediateNode<K, V> parent = nn.parent();

                            // Borrow: First, check the left sibling, then the right sibling
                            if (ln.leftSibling() != null
                                    && BPlusTreeHelper.eq(ln.leftSibling().parent(), ln.parent())
                                    && BPlusTreeHelper.isLendable(ln.leftSibling())) {

                                sibling = ln.leftSibling();
                                Entry<K, V> borrowedDP = sibling.entryAt(sibling.size() - 1);

                        /* Insert borrowed dictionary pair, sort dictionary,
						   and delete dictionary pair from sibling */
                                store.addEntry(ln, borrowedDP.getKey(),borrowedDP.getValue());
                                store.removeChildAt(sibling, sibling.size() - 1);
                            } else if (ln.rightSibling() != null
                                    && BPlusTreeHelper.eq(ln.rightSibling().parent(), ln.parent())
                                    && BPlusTreeHelper.isLendable(ln.rightSibling())) {

                                sibling = ln.rightSibling();
                                Entry<K, V> borrowedDP = sibling.entryAt(0);

                        /* Insert borrowed dictionary pair, sort dictionary,
					       and delete dictionary pair from sibling */
                                store.addEntry(ln, borrowedDP.getKey(),borrowedDP.getValue());
                                store.removeChildAt(sibling, 0);

                            } // Merge: First, check the left sibling, then the right sibling
                            else if (ln.leftSibling() != null
                                    && ln.leftSibling().parent() == ln.parent()
                                    && BPlusTreeHelper.isMergeable(ln.leftSibling())) {

                                sibling = ln.leftSibling();
                                store.removeChildAt(parent, store.findIndexOfChild(parent, ln));

                                // Update sibling pointer
                                store.updateRightSibling(sibling, ln.rightSibling());

                                // Check for deficiencies in parent
                                handleDeficiency(parent);

                            } else if (ln.rightSibling() != null
                                    && BPlusTreeHelper.eq(ln.rightSibling().parent(), ln.parent())
                                    && BPlusTreeHelper.isMergeable(ln.rightSibling())) {

                                sibling = ln.rightSibling();
                                int pointerIndex = store.findIndexOfChild(parent, ln);

                                // Remove key and child pointer from parent
                                store.removeChildAt(parent, pointerIndex);

                                // Update sibling pointer
                                store.updateLeftSibling(sibling, ln.leftSibling());
                                if (sibling.leftSibling() == null) {
                                    store.updateFirstLeaf(sibling);
                                }

                                handleDeficiency(parent);
                            }
                        } else {

                        }

                    } else if (store.root() == null && store.firstLeaf().size() == 0) {

                    /* Flow of execution goes here when the deleted dictionary
					   pair was the only pair within the tree */
                        // Set first leaf as null to indicate B+ tree is empty
                        store.updateFirstLeaf(null);
                        break;
                    } else {
                        break;
                    }
                    nn = nn.parent();
                }
            }
            store.incSize(-1);
            store.save();
            return true;
        }
    }

    /**
     * Given an integer key and floating point value, this method inserts a
     * dictionary pair accordingly into the B+ tree.
     *
     * @param key:   an integer key to be used in the dictionary pair
     * @param value: a floating point number to be used in the dictionary pair
     */
    public V put(K key, V value) {
        return add(key, value, store.isAllowDuplicates());
    }

    @Override
    public V putIfAbsent(K key, V value) {
        NOptional<V> v = getOptional(key);
        if (v.isNotPresent()) {
            return put(key, value);
        }
        return v.get();
    }

    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        NOptional<V> u = getOptional(key);
        if (u.isNotPresent()) {
            V newValue = mappingFunction.apply(key);
            put(key, newValue);
            return newValue;
        }
        return u.get();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new AbstractSet<Map.Entry<K, V>>() {
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return entryIterator();
            }

            @Override
            public int size() {
                return BPlusTree.this.size();
            }
        };
    }


    public V add(K key, V value, boolean allowDuplicate) {
        allowDuplicate &= store.isAllowDuplicates();
        if (isEmpty()) {

            /* Flow of execution goes here only when first insert takes place */
            // Create leaf node as first node in B plus tree (root is null)
            LeafNode<K, V> ln = store.createLeafNode(null);
            store.addEntry(ln, key, value);
            store.incSize(1);
            // Set as first leaf node (can be used later for in-order leaf traversal)
            store.updateFirstLeaf(ln);
            store.save();
            return null;
        } else {
            // Find leaf node to insert into
            LeafNode<K, V> ln = findLeafNode(key);
            if (!allowDuplicate) {
                int index = store.indexOfKey(ln, key);
                if (index >= 0) {
                    V oldValue = store.updateValueAt(ln, index, value);
                    store.save();
                    return oldValue;
                }
            }
            // Insert into leaf node fails if node becomes overfull
            if (BPlusTreeHelper.isFull(ln)) {
                // Sort all the dictionary pairs with the included pair to be inserted
                // Split the sorted pairs into two halves
                int midpoint = getMidpoint();
                int lnSize = ln.size();
                Entry<K, V>[] halfDict = new Entry[lnSize - midpoint + 1];
                for (int i = lnSize-1; i >=midpoint ; i--) {
                    halfDict[i - midpoint] = ln.entryAt(i);
                    store.removeChildAt(ln, i);
                }
                halfDict[halfDict.length - 1] = new AbstractMap.SimpleEntry<>(key, value);
                K k0 = null;
                if (ln.parent() == null) {
                    /* Flow of execution goes here when there is 1 node in tree */
                    // Create internal node to serve as parent, use dictionary midpoint key
                    IntermediateNode<K, V> parent = store.createInternalNode();
                    store.updateParent(ln, parent);
                    store.addChild(parent, ln, -1);
                } else {
                    /* Flow of execution goes here when parent exists */
                    // Add new key to parent for proper indexing
                    K newParentKey = halfDict[0].getKey();
                    k0 = newParentKey;
//                    store.updateKeyAt(ln.parent(), ln.parent().degree() - 1, newParentKey);
                }

                // Create new LeafNode that holds the other half
                LeafNode<K, V> newLeafNode = store.createLeafNode(ln.parent());
                store.addEntries(newLeafNode, halfDict);

                // Update child pointers of parent node
                int pointerIndex = store.findIndexOfChild(ln.parent(), ln) + 1;
                store.addChild(ln.parent(), newLeafNode, pointerIndex);

                // Make leaf nodes siblings of one another
                store.updateRightSibling(newLeafNode, ln.rightSibling());
                if (newLeafNode.rightSibling() != null) {
                    store.updateLeftSibling(newLeafNode.rightSibling(), newLeafNode);
                }
                store.updateRightSibling(ln, newLeafNode);
                store.updateLeftSibling(newLeafNode, ln);

                if (store.root() == null) {

                    // Set the root of B+ tree to be the parent
                    store.updateRoot(ln.parent());

                } else {

                    /* If parent is overfull, repeat the process up the tree,
			   		   until no deficiencies are found */
                    IntermediateNode<K, V> in = ln.parent();
                    while (in != null) {
                        if (BPlusTreeHelper.isOverfull(in)) {
                            splitInternalNode(in);
                        } else {
                            break;
                        }
                        in = in.parent();
                    }
                }
                store.incSize(1);
                store.save();
            } else {
                store.addEntry(ln, key, value);
                store.incSize(1);
                store.save();
                return null;
            }
        }
        return null;
    }

    public Iterator<Map.Entry<K, V>> entryIterator() {
        return new Iterator<Map.Entry<K, V>>() {
            private LeafNode<K, V> curr = store.firstLeaf();
            private int index;
            private Map.Entry e;

            @Override
            public boolean hasNext() {
                while(true) {
                    if (curr == null) {
                        return false;
                    }
                    if (index < curr.size()) {
                        e = curr.entryAt(index);
                        index++;
                        if (index >= curr.size()) {
                            index = 0;
                            curr = curr.rightSibling();
                        }
                        return true;
                    } else {
                        index = 0;
                        curr = curr.rightSibling();
                    }
                }
            }

            @Override
            public Map.Entry<K, V> next() {
                return e;
            }
        };
    }
    public Iterator<K> keyIterator() {
        return new Iterator<K>() {
            private LeafNode<K, V> curr = store.firstLeaf();
            private int index;
            private K e;

            @Override
            public boolean hasNext() {
                while(true) {
                    if (curr == null) {
                        return false;
                    }
                    if (index < curr.size()) {
                        e = curr.keyAt(index);
                        index++;
                        if (index >= curr.size()) {
                            index = 0;
                            curr = curr.rightSibling();
                        }
                        return true;
                    } else {
                        index = 0;
                        curr = curr.rightSibling();
                    }
                }
            }

            @Override
            public K next() {
                return e;
            }
        };
    }

    public List<V> search(K key) {

        // If B+ tree is completely empty, simply return null
        if (isEmpty()) {
            return new ArrayList<>();
        }

        // Find leaf node that holds the dictionary key
        LeafNode<K, V> ln = findLeafNode(key);

        int index = store.indexOfKey(ln, key);

        // If index negative, the key doesn't exist in B+ tree
        if (index < 0) {
            return new ArrayList<>();
        } else {
            List<V> all = new ArrayList<>();
            all.add(ln.valueAt(index));
            for (int i = index + 1; i < ln.size(); i++) {
                K u = ln.keyAt(i);
                if (Objects.equals(key, u)) {
                    all.add(ln.valueAt(i));
                } else {
                    break;
                }
            }
            return all;
        }
    }

    public V get(K key) {

        // If B+ tree is completely empty, simply return null
        if (isEmpty()) {
            return null;
        }

        // Find leaf node that holds the dictionary key
        LeafNode<K, V> ln = findLeafNode(key);

        // Perform binary search to find index of key within dictionary
        int index = store.indexOfKey(ln, key);

        // If index negative, the key doesn't exist in B+ tree
        if (index < 0) {
            return null;
        } else {
            return ln.valueAt(index);
        }
    }

    public NOptional<V> getOptional(K key) {

        // If B+ tree is completely empty, simply return null
        if (isEmpty()) {
            return NOptional.ofNamedEmpty("key " + key);
        }

        // Find leaf node that holds the dictionary key
        LeafNode<K, V> ln = findLeafNode(key);

        // Perform binary search to find index of key within dictionary
        int index = store.indexOfKey(ln, key);

        // If index negative, the key doesn't exist in B+ tree
        if (index < 0) {
            return NOptional.ofNamedEmpty("key " + key);
        } else {
            return NOptional.ofNullable(ln.valueAt(index));
        }
    }

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
    public List<V> search(K lowerBound, K upperBound) {

        // Instantiate Double array to hold values
        List<V> values = new ArrayList<>();

        // Iterate through the doubly linked list of leaves
        LeafNode<K, V> currNode = store.firstLeaf();
        while (currNode != null) {

            // Iterate through the dictionary of each node
            int max = currNode.size();
            for (int i = 0; i < max; i++) {
                K dp = currNode.keyAt(i);

                /* Stop searching the dictionary once a null value is encountered
				   as this the indicates the end of non-null values */
                if (dp == null) {
                    break;
                }

                // Include value if its key fits within the provided range
                if (BPlusTreeHelper.compareKey(lowerBound, dp) <= 0 && BPlusTreeHelper.compareKey(dp, upperBound) <= 0) {
                    values.add(currNode.valueAt(i));
                }
            }

            /* Update the current node to be the right sibling,
			   leaf traversal is from left to right */
            currNode = currNode.rightSibling();

        }
        return values;
    }


    public interface Visitor<K extends Comparable<K>, V> {
        void visitLeaf(LeafNode<K, V> node, int level);

        void visitIntermediate(IntermediateNode<K, V> node, int level);
    }

    public void dump() {
        dump(System.out);
    }

    public void dump(PrintStream out) {
        visit(new Visitor<K, V>() {
            @Override
            public void visitLeaf(LeafNode<K, V> node, int level) {
                out.print(lvlStr(level));
                out.println(node);
//                out.println("LEAF"+node.keys());
            }

            @Override
            public void visitIntermediate(IntermediateNode<K, V> node, int level) {
                out.print(lvlStr(level));
//                out.println("INTERMEDIATE("+node.degree()+")");
                out.println(node);
            }

            private String lvlStr(int level) {
                char[] cc = new char[level * 2];
                Arrays.fill(cc, ' ');
                return new String(cc);
            }
        });
    }

    public void visit(Visitor<K, V> visitor) {
        class NodeAndLevel {
            Node<K, V> node;
            int level;

            public NodeAndLevel(Node<K, V> node, int level) {
                this.node = node;
                this.level = level;
            }
        }
        Stack<NodeAndLevel> stack = new Stack<>();
        IntermediateNode<K, V> root = store.root();
        if (root != null) {
            stack.push(new NodeAndLevel(root, 0));
        } else {
            LeafNode<K, V> kvLeafNode = store.firstLeaf();
            if (kvLeafNode != null) {
                stack.push(new NodeAndLevel(kvLeafNode, 0));
            }
        }
        while (!stack.isEmpty()) {
            NodeAndLevel n = stack.pop();
            if (n.node == null) {
                //bug
                visitor.visitLeaf(null, n.level);
            } else if (n.node.isLeaf()) {
                visitor.visitLeaf((LeafNode<K, V>) n.node, n.level);
            } else {
                IntermediateNode<K, V> nn = (IntermediateNode<K, V>) n.node;
                visitor.visitIntermediate(nn, n.level);
                int count = nn.size();
                for (int i = count - 1; i >= 0; i--) {
                    stack.push(new NodeAndLevel(nn.child(i), n.level + 1));
                }
            }
        }
    }

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
