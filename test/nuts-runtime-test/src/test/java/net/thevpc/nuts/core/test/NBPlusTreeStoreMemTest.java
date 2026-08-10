package net.thevpc.nuts.core.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.collections.NBPlusTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NBPlusTreeStoreMemTest {

    @BeforeAll
    public static void initWorkspace() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void testBasicCrud() throws Exception {
        try (NBPlusTree<String, String> tree = NBPlusTree.of(5, false)) {
            tree.put("key1", "val1");
            tree.put("key2", "val2");
            tree.put("key3", "val3");
            tree.put("key4", "val4");
            tree.put("key5", "val5");
            tree.put("key6", "val6"); // triggers splits

            assertEquals("val1", tree.get("key1"));
            assertEquals("val6", tree.get("key6"));
            assertEquals(6, tree.size());

            tree.remove("key3");
            assertNull(tree.get("key3"));
            assertEquals(5, tree.size());
        }
    }

    @Test
    public void testPageStoreMem() throws IOException {
        NDataSerializer<String> serializer = new NDataSerializer<String>() {
            @Override
            public void serialize(String obj, DataOutputStream dos) throws IOException {
                if (obj == null) {
                    dos.writeBoolean(false);
                } else {
                    dos.writeBoolean(true);
                    dos.writeUTF(obj);
                }
            }

            @Override
            public String deserialize(DataInputStream dis) throws IOException {
                if (dis.readBoolean()) {
                    return dis.readUTF();
                }
                return null;
            }
        };

        try (NBPlusTree<String, String> tree = NBPlusTree.ofInMemory(4096, 5, false, serializer, serializer)) {
            tree.put("key1", "val1");
            tree.put("key2", "val2");
            tree.put("key3", "val3");
            tree.put("key4", "val4");
            tree.put("key5", "val5");
            tree.put("key6", "val6"); // triggers splits

            assertEquals("val1", tree.get("key1"));
            assertEquals("val6", tree.get("key6"));
            assertEquals(6, tree.size());

            tree.remove("key3");
            assertNull(tree.get("key3"));
            assertEquals(5, tree.size());
        }
    }

    @Test
    public void testDuplicatesAndSearch() throws Exception {
        // Order 3 is small and triggers splits easily
        try (NBPlusTree<String, String> tree = NBPlusTree.of(3, true)) {
            tree.put("key1", "val1_1");
            tree.put("key2", "val2_1");
            tree.put("key1", "val1_2");
            tree.put("key1", "val1_3");
            tree.put("key3", "val3_1");
            tree.put("key1", "val1_4"); // forces splits with duplicate keys

            List<String> key1Vals = tree.search("key1");
            assertEquals(4, key1Vals.size());
            assertTrue(key1Vals.contains("val1_1"));
            assertTrue(key1Vals.contains("val1_2"));
            assertTrue(key1Vals.contains("val1_3"));
            assertTrue(key1Vals.contains("val1_4"));

            List<String> key2Vals = tree.search("key2");
            assertEquals(1, key2Vals.size());
            assertEquals("val2_1", key2Vals.get(0));

            List<String> key4Vals = tree.search("key4");
            assertTrue(key4Vals.isEmpty());
        }
    }

    @Test
    public void testRangeSearch() throws Exception {
        try (NBPlusTree<Integer, String> tree = NBPlusTree.of(5, false)) {
            for (int i = 1; i <= 10; i++) {
                tree.put(i, "val" + i);
            }

            List<String> range = tree.search(3, 7);
            assertEquals(5, range.size());
            assertTrue(range.contains("val3"));
            assertTrue(range.contains("val4"));
            assertTrue(range.contains("val5"));
            assertTrue(range.contains("val6"));
            assertTrue(range.contains("val7"));
            assertFalse(range.contains("val2"));
            assertFalse(range.contains("val8"));
        }
    }

    @Test
    public void testStressUniqueKeys() throws Exception {
        try (NBPlusTree<Integer, Integer> tree = NBPlusTree.of(5, false)) {
            int total = 1000;
            for (int i = 0; i < total; i++) {
                tree.put(i, i * 10);
            }
            assertEquals(total, tree.size());

            for (int i = 0; i < total; i++) {
                assertEquals(i * 10, tree.get(i));
            }

            // Remove odd keys
            int removedCount = 0;
            for (int i = 1; i < total; i += 2) {
                if (tree.remove(i,null)) {
                    removedCount++;
                } else {
                    System.out.println("First memory deletion failure key: " + i);
                    dumpTree(tree);
                    break;
                }
                if (i <= 27) {
                    System.out.println("Tree after removing key: " + i);
                    dumpTree(tree);
                }
            }
            assertEquals(total / 2, tree.size());

            for (int i = 0; i < total; i++) {
                if (i % 2 == 1) {
                    assertNull(tree.get(i));
                } else {
                    assertEquals(i * 10, tree.get(i));
                }
            }
        }
    }

    private void dumpTree(NBPlusTree<Integer, Integer> tree) {
        net.thevpc.nuts.runtime.standalone.collections.NBPlusTreeImpl<Integer, Integer> impl = (net.thevpc.nuts.runtime.standalone.collections.NBPlusTreeImpl<Integer, Integer>) tree;
        impl.visit(new NBPlusTree.Visitor<Integer, Integer>() {
            @Override
            public void visitIntermediate(NBPlusTree.IntermediateNode<Integer, Integer> node, int level) {
                char[] chars = new char[level * 2];
                java.util.Arrays.fill(chars, ' ');
                java.util.List<Integer> keysList = new java.util.ArrayList<>();
                for (int i = 1; i < node.size(); i++) {
                    keysList.add(node.key(i));
                }
                System.out.println(new String(chars) + "Intermediate: keys=" + keysList + " firstKey=" + node.firstKey());
            }

            @Override
            public void visitLeaf(NBPlusTree.LeafNode<Integer, Integer> node, int level) {
                char[] chars = new char[level * 2];
                java.util.Arrays.fill(chars, ' ');
                System.out.println(new String(chars) + "Leaf: keys=" + (node == null ? "null" : node.keys()) + " parentKeys=" + (node == null || node.parent() == null ? "null" : "hasParent"));
            }
        });
    }
}
