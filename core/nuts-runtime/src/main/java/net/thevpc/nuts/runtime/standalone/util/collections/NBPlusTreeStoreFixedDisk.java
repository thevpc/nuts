package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.util.NBPlusTree;
import net.thevpc.nuts.util.NBPlusTreeStore;
import net.thevpc.nuts.util.NExceptions;

import java.io.*;
import java.util.*;

public class NBPlusTreeStoreFixedDisk<K extends Comparable<K>, V> implements NBPlusTreeStore<K, V>, Closeable {

    public interface NBSerializer<T> {
        void serialize(T obj, DataOutputStream dos) throws IOException;
        T deserialize(DataInputStream dis) throws IOException;
    }

    private NBFixedBlockFile blockFile;
    private int m = -1;
    private boolean allowDuplicates;
    private long size = 0;
    
    private long rootId = -1;
    private long firstLeafId = -1;

    private NBSerializer<K> keySerializer;
    private NBSerializer<V> valSerializer;

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

    public NBPlusTreeStoreFixedDisk(File file, int m, boolean allowDuplicates, NBSerializer<K> keySerializer, NBSerializer<V> valSerializer) throws IOException {
        this.keySerializer = keySerializer;
        this.valSerializer = valSerializer;
        this.blockFile = new NBFixedBlockFile(file, 4096);
        
        long storedM = this.blockFile.getUserData4();
        if (storedM != -1) {
            this.m = (int) storedM;
            this.allowDuplicates = this.blockFile.getUserData5() == 1;
            this.rootId = this.blockFile.getUserData1();
            this.firstLeafId = this.blockFile.getUserData2();
            this.size = this.blockFile.getUserData3();
        } else {
            this.m = m;
            this.allowDuplicates = allowDuplicates;
            this.blockFile.setUserData4(m);
            this.blockFile.setUserData5(allowDuplicates ? 1 : 0);
        }
    }

    @Override
    public int m() {
        return m;
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
        NBPlusTreeStoreFixedDiskLeafNode<K, V> node = new NBPlusTreeStoreFixedDiskLeafNode<>(this, m);
        updateParent(node, parent);
        return node;
    }

    @Override
    public NBPlusTree.IntermediateNode<K, V> createInternalNode() {
        return new NBPlusTreeStoreFixedDiskIntermediateNode<>(this, m);
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
        AbstractMap.SimpleEntry<K, V> nv = new AbstractMap.SimpleEntry<>(k, v);
        int index = Arrays.binarySearch(ln.dictionary, 0, ln.size, nv, NBPlusTreeHelper::compareEntries);
        if (index >= 0) {
            while (index < ln.size && Objects.equals(ln.dictionary[index].getKey(), k)) {
                index++;
            }
            System.arraycopy(ln.dictionary, index, ln.dictionary, index + 1, ln.size - index);
            ln.dictionary[index] = nv;
        } else {
            index = -index - 1;
            System.arraycopy(ln.dictionary, index, ln.dictionary, index + 1, ln.size - index);
            ln.dictionary[index] = nv;
        }
        ln.size++;
        ln.dirty = true;
    }

    @Override
    public void addEntries(NBPlusTree.LeafNode<K, V> node, NBPlusTree.Entry<K, V>[] orderedElements) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) node;
        if (orderedElements != null && orderedElements.length > 0) {
            NBPlusTree.Entry<K, V>[] arr1 = new NBPlusTree.Entry[ln.size];
            System.arraycopy(ln.dictionary, 0, arr1, 0, ln.size);
            NBPlusTree.Entry<K, V>[] arr2 = orderedElements;
            int i = 0, j = 0, k = 0;
            int n1 = ln.size;
            int n2 = arr2.length;
            while (i < n1 && j < n2) {
                if (NBPlusTreeHelper.compareEntries(arr1[i], arr2[j]) < 0) {
                    ln.dictionary[k++] = arr1[i++];
                } else {
                    ln.dictionary[k++] = arr2[j++];
                }
            }
            System.arraycopy(arr1, i, ln.dictionary, k, n1 - i);
            System.arraycopy(arr2, j, ln.dictionary, k + n1 - i, n2 - j);
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
        System.arraycopy(ln.dictionary, index + 1, ln.dictionary, index, ln.size - index - 1);
        ln.size--;
        ln.dictionary[ln.size] = null;
        ln.dirty = true;
    }

    @Override
    public int indexOfKey(NBPlusTree.LeafNode<K, V> leafNode, K key) {
        NBPlusTreeStoreFixedDiskLeafNode<K, V> ln = (NBPlusTreeStoreFixedDiskLeafNode<K, V>) leafNode;
        AbstractMap.SimpleEntry<K, V> e = new AbstractMap.SimpleEntry<>(key, null);
        return Arrays.binarySearch(ln.dictionary, 0, ln.size, e, NBPlusTreeHelper::compareEntries);
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
            
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
            boolean isLeaf = dis.readBoolean();
            
            NBPlusTreeStoreFixedDiskNode<K, V> node;
            if (isLeaf) {
                node = new NBPlusTreeStoreFixedDiskLeafNode<>(this, m);
            } else {
                node = new NBPlusTreeStoreFixedDiskIntermediateNode<>(this, m);
            }
            
            node.deserialize(dis);
            node.blockId = blockId;
            node.dirty = false;
            
            cache.put(blockId, node);
            return node;
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    private void saveNode(NBPlusTreeStoreFixedDiskNode<K, V> node) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        node.serialize(dos);
        dos.flush();
        byte[] data = baos.toByteArray();
        
        if (node.blockId == -1) {
            node.blockId = blockFile.writeData(data);
            cache.put(node.blockId, node);
        } else {
            blockFile.updateDataSafe(node.blockId, data);
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
    public void close() {
        save();
        try {
            blockFile.close();
        } catch (IOException e) {
            throw NExceptions.ofUncheckedException(e);
        }
    }
}
