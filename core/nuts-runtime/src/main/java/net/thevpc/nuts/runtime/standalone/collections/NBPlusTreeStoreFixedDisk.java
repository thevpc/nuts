package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.collections.NBPlusTree;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NExceptions;

import java.io.*;
import java.util.*;

public class NBPlusTreeStoreFixedDisk<K extends Comparable<K>, V> implements NBPlusTreeStore<K, V>, Closeable {

    private NBFixedBlockFile blockFile;
    private int order = -1;
    private boolean allowDuplicates;
    private long size = 0;
    
    private long rootId = -1;
    private long firstLeafId = -1;

    private NDataSerializer<K> keySerializer;
    private NDataSerializer<V> valSerializer;

    private final ReusableByteArrayOutputStream serializeStream = new ReusableByteArrayOutputStream(4096);
    private final DataOutputStream serializeDataStream = new DataOutputStream(serializeStream);

    private final ReusableByteArrayInputStream deserializeStream = new ReusableByteArrayInputStream();
    private final DataInputStream deserializeDataStream = new DataInputStream(deserializeStream);

    private Map<Long, NBPlusTreeStoreFixedDiskNode<K, V>> cache = new LinkedHashMap<Long, NBPlusTreeStoreFixedDiskNode<K, V>>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, NBPlusTreeStoreFixedDiskNode<K, V>> eldest) {
            if (size() > 500) {
                if (eldest.getValue().dirty) {
                    try {
                        saveNode(eldest.getValue());
                    } catch (IOException e) {
                        throw new NIOException(e);
                    }
                }
                return true;
            }
            return false;
        }
    };

    public NBPlusTreeStoreFixedDisk(NPageStore pageStore, int order, boolean allowDuplicates, NDataSerializer<K> keySerializer, NDataSerializer<V> valSerializer) throws IOException {
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
        this.blockFile = new NBFixedBlockFile(pageStore);
        if (order <= 0) {
            order=5;
        }
        NAssert.requireTrue(order>=3,() -> NMsg.ofC("B+Tree order must be >=3"));
        long storedM = this.blockFile.getUserData4();
        if (storedM != -1) {
            this.order = (int) storedM;
            this.allowDuplicates = this.blockFile.getUserData5() == 1;
            this.rootId = this.blockFile.getUserData1();
            this.firstLeafId = this.blockFile.getUserData2();
            this.size = this.blockFile.getUserData3();
        } else {
            this.order = order;
            this.allowDuplicates = allowDuplicates;
            this.blockFile.setUserData4(order);
            this.blockFile.setUserData5(allowDuplicates ? 1 : 0);
        }
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public void incSize(long sizeDiff) {
        this.size += sizeDiff;
        this.blockFile.setUserData3(this.size);
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> root() {
        return rootId == -1 ? null : (NBPlusTree.IntermediateNode<K, V>) loadNode(rootId);
    }

    @Override
    public NBPlusTree.LeafNode<K, V> firstLeaf() {
        return firstLeafId == -1 ? null : (NBPlusTree.LeafNode<K, V>) loadNode(firstLeafId);
    }

    @Override
    public void updateRoot(NBPlusTree.IntermediateNode<K, V> root) {
        if (root == null) {
            this.rootId = -1;
        } else {
            NBPlusTreeStoreFixedDiskIntermediateNode<K, V> r = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) root;
            if (r.blockId == -1) {
                try {
                    saveNode(r);
                } catch (IOException e) {
                    throw new NIOException(e);
                }
            }
            this.rootId = r.blockId;
        }
        this.blockFile.setUserData1(this.rootId);
    }

    @Override
    public void updateFirstLeaf(NBPlusTree.LeafNode<K, V> node) {
        if (node == null) {
            this.firstLeafId = -1;
        } else {
            NBPlusTreeStoreFixedDiskLeafNode<K, V> l = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
            if (l.blockId == -1) {
                try {
                    saveNode(l);
                } catch (IOException e) {
                    throw new NIOException(e);
                }
            }
            this.firstLeafId = l.blockId;
        }
        this.blockFile.setUserData2(this.firstLeafId);
    }

    @Override
    public NBPlusTree.LeafNode<K, V> createLeafNode(NBPlusTree.IntermediateNode<K, V> parent) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> node = new NBPlusTreeStoreFixedDiskLeafNode<>(this, order);
        try {
            saveNode(node);
        } catch (IOException e) {
            throw new NIOException(e);
        }
        updateParent(node, parent);
        return node;
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> createInternalNode() {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> node = new NBPlusTreeStoreFixedDiskIntermediateNode<>(this, order);
        try {
            saveNode(node);
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return node;
    }

    @Override
    public void updateParent(NBPlusTree.Node<K, V> root, NBPlusTree.IntermediateNode<K, V> parent) {
        NBPlusTreeStoreFixedDiskNode<K, V> n = (NBPlusTreeStoreFixedDiskNode<K, V>) root;
        n.parentId = parent == null ? -1 : ((NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) parent).blockId;
        n.dirty = true;
    }

    @Override
    public void updateLeftSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> n = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        n.leftSiblingId = value == null ? -1 : ((NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) value).blockId;
        n.dirty = true;
    }

    @Override
    public void updateRightSibling(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.IntermediateNode<K, V> value) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> n = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        n.rightSiblingId = value == null ? -1 : ((NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) value).blockId;
        n.dirty = true;
    }

    @Override
    public void updateLeftSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> n = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        n.leftSiblingId = value == null ? -1 : ((NBPlusTreeStoreFixedDiskLeafNode<K, V>) value).blockId;
        n.dirty = true;
    }

    @Override
    public void updateRightSibling(NBPlusTree.LeafNode<K, V> node, NBPlusTree.LeafNode<K, V> value) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> n = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        n.rightSiblingId = value == null ? -1 : ((NBPlusTreeStoreFixedDiskLeafNode<K, V>) value).blockId;
        n.dirty = true;
    }

    @Override
    public void addEntry(NBPlusTree.LeafNode<K, V> node, K k, V v) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        int index = Arrays.binarySearch(ln.keys, 0, ln.size, k);
        if (index >= 0) {
            while (index < ln.size && Objects.equals(ln.keys[index], k)) {
                index++;
            }
        } else {
            index = -index - 1;
        }
        System.arraycopy(ln.keys, index, ln.keys, index + 1, ln.size - index);
        System.arraycopy(ln.values, index, ln.values, index + 1, ln.size - index);
        ln.keys[index] = k;
        ln.values[index] = v;
        ln.size++;
        ln.dirty = true;
    }

    @Override
    public void addEntries(NBPlusTree.LeafNode<K, V> node, NBPlusTree.Entry<K, V>[] orderedElements) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        if (orderedElements != null && orderedElements.length > 0) {
            K[] arr1Keys = Arrays.copyOf(ln.keys, ln.size);
            V[] arr1Values = Arrays.copyOf(ln.values, ln.size);
            int i = 0, j = 0, k = 0;
            int n1 = ln.size;
            int n2 = orderedElements.length;
            while (i < n1 && j < n2) {
                K key1 = arr1Keys[i];
                K key2 = orderedElements[j].getKey();
                if (key1.compareTo(key2) < 0) {
                    ln.keys[k] = arr1Keys[i];
                    ln.values[k] = arr1Values[i];
                    i++;
                } else {
                    ln.keys[k] = orderedElements[j].getKey();
                    ln.values[k] = orderedElements[j].getValue();
                    j++;
                }
                k++;
            }
            if (i < n1) {
                System.arraycopy(arr1Keys, i, ln.keys, k, n1 - i);
                System.arraycopy(arr1Values, i, ln.values, k, n1 - i);
            }
            if (j < n2) {
                for (int idx = j; idx < n2; idx++) {
                    ln.keys[k + idx - j] = orderedElements[idx].getKey();
                    ln.values[k + idx - j] = orderedElements[idx].getValue();
                }
            }
            ln.size += orderedElements.length;
            ln.dirty = true;
        }
    }

    @Override
    public int findIndexOfChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> child) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> in = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        long childId = ((NBPlusTreeStoreFixedDiskNode<K, V>) child).blockId;
        for (int i = 0; i < in.size; i++) {
            if (in.childrenIds[i] == childId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void addChild(NBPlusTree.IntermediateNode<K, V> node, NBPlusTree.Node<K, V> pointer, int index) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> in = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        NBPlusTreeStoreFixedDiskNode<K, V> childNode = (NBPlusTreeStoreFixedDiskNode<K, V>) pointer;
        if (childNode.blockId == -1) {
            try {
                saveNode(childNode);
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        
        if (index < 0) {
            index = in.size;
        }
        System.arraycopy(in.childrenIds, index, in.childrenIds, index + 1, in.size - index);
        in.childrenIds[index] = childNode.blockId;
        in.size++;
        if (index == 0) {
            in.firstKey = childNode.firstKey();
        }
        in.dirty = true;
    }

    @Override
    public void updateChildAt(NBPlusTree.IntermediateNode<K, V> node, int index, K key, NBPlusTree.Node<K, V> child) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> in = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        NBPlusTreeStoreFixedDiskNode<K, V> childNode = (NBPlusTreeStoreFixedDiskNode<K, V>) child;
        if (childNode.blockId == -1) {
            try {
                saveNode(childNode);
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
        in.childrenIds[index] = childNode.blockId;
        if (index == 0) {
            in.firstKey = childNode.firstKey();
        }
        in.dirty = true;
    }

    @Override
    public V updateValueAt(NBPlusTree.LeafNode<K, V> node, int index, V value) {
        return ((NBPlusTreeStoreFixedDiskLeafNode<K, V>) node).setValueAt(index, value);
    }

    @Override
    public void removeChildAt(NBPlusTree.IntermediateNode<K, V> node, int index) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> in = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        System.arraycopy(in.childrenIds, index + 1, in.childrenIds, index, in.size - index - 1);
        in.size--;
        if (index == 0 && in.size > 0) {
            in.firstKey = loadNode(in.childrenIds[0]).firstKey();
        } else if (in.size == 0) {
            in.firstKey = null;
        }
        in.dirty = true;
    }

    @Override
    public void removeChildAt(NBPlusTree.LeafNode<K, V> node, int index) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        System.arraycopy(ln.keys, index + 1, ln.keys, index, ln.size - index - 1);
        System.arraycopy(ln.values, index + 1, ln.values, index, ln.size - index - 1);
        ln.size--;
        ln.keys[ln.size] = null;
        ln.values[ln.size] = null;
        ln.dirty = true;
    }

    @Override
    public int indexOfKey(NBPlusTree.LeafNode<K, V> leafNode, K key) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) leafNode;
        return Arrays.binarySearch(ln.keys, 0, ln.size, key);
    }

    @Override
    public void free(NBPlusTree.Node<K, V> node) {
        NBPlusTreeStoreFixedDiskNode<K, V> n = (NBPlusTreeStoreFixedDiskNode<K, V>) node;
        if (n.blockId != -1) {
            try {
                blockFile.freeBlockChain(n.blockId);
            } catch (IOException e) {
                throw new NIOException(e);
            }
            cache.remove(n.blockId);
            n.blockId = -1;
        }
    }

    @Override
    public void save() {
        try {
            for (NBPlusTreeStoreFixedDiskNode<K, V> node : new ArrayList<>(cache.values())) {
                if (node.dirty) {
                    saveNode(node);
                }
            }
            blockFile.flushHeader();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    public NBPlusTreeStoreFixedDiskNode<K, V> loadNode(long blockId) {
        if (blockId == -1) return null;
        NBPlusTreeStoreFixedDiskNode<K, V> cached = cache.get(blockId);
        if (cached != null) {
            return cached;
        }

        try {
            byte[] data = blockFile.readData(blockId);
            if (data == null) return null;
            
            deserializeStream.setBuffer(data, 0, data.length);
            boolean isLeaf = deserializeDataStream.readBoolean();
            
            NBPlusTreeStoreFixedDiskNode<K, V> node;
            if (isLeaf) {
                node = new NBPlusTreeStoreFixedDiskLeafNode<>(this, order);
            } else {
                node = new NBPlusTreeStoreFixedDiskIntermediateNode<>(this, order);
            }
            
            node.deserialize(deserializeDataStream);
            node.blockId = blockId;
            node.dirty = false;
            
            cache.put(blockId, node);
            return node;
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    private void saveNode(NBPlusTreeStoreFixedDiskNode<K, V> node) throws IOException {
        serializeStream.reset();
        node.serialize(serializeDataStream);
        serializeDataStream.flush();
        
        if (node.blockId == -1) {
            node.blockId = blockFile.writeData(serializeStream.getBuffer(), 0, serializeStream.size());
            cache.put(node.blockId, node);
        } else {
            blockFile.updateDataSafe(node.blockId, serializeStream.getBuffer(), 0, serializeStream.size());
        }
        node.dirty = false;
    }

    public void serializeKey(K key, DataOutputStream dos) throws IOException {
        keySerializer.serialize(key, dos);
    }

    public K deserializeKey(DataInputStream dis) throws IOException {
        return keySerializer.deserialize(dis);
    }

    public void serializeValue(V value, DataOutputStream dos) throws IOException {
        valSerializer.serialize(value, dos);
    }

    public V deserializeValue(DataInputStream dis) throws IOException {
        return valSerializer.deserialize(dis);
    }

    @Override
    public void updateFirstKey(NBPlusTree.IntermediateNode<K, V> node, K firstKey) {
        NBPlusTreeStoreFixedDiskIntermediateNode<K, V> in = (NBPlusTreeStoreFixedDiskIntermediateNode<K, V>) node;
        in.firstKey = firstKey;
        in.dirty = true;
    }

    @Override
    public void close() {
        save();
        try {
            blockFile.close();
        } catch (IOException e) {
            throw NExceptions.ofUncheckedException(e);
        }
    }
}
