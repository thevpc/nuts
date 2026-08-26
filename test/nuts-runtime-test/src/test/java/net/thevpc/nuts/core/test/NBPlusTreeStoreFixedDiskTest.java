package net.thevpc.nuts.core.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.collections.NBPlusTree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NBPlusTreeStoreFixedDiskTest {

    private File dbFile;
    private static final NDataSerializer<String> STRING_SERIALIZER = new NDataSerializer<String>() {
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

    private static final NDataSerializer<Integer> INT_SERIALIZER = new NDataSerializer<Integer>() {
        @Override
        public void serialize(Integer obj, DataOutputStream dos) throws IOException {
            if (obj == null) {
                dos.writeBoolean(false);
            } else {
                dos.writeBoolean(true);
                dos.writeInt(obj);
            }
        }

        @Override
        public Integer deserialize(DataInputStream dis) throws IOException {
            if (dis.readBoolean()) {
                return dis.readInt();
            }
            return null;
        }
    };

    @BeforeAll
    public static void initWorkspace() {
        TestUtils.openNewMinTestWorkspace();
    }

    @BeforeEach
    public void setup() throws IOException {
        dbFile = File.createTempFile("nbtree_fixed_", ".db");
    }

    @AfterEach
    public void teardown() {
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    public void testBasicCrud() throws IOException {
        NPageStore pageStore = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<String, String> tree = NBPlusTree.of(pageStore, 5, false, STRING_SERIALIZER, STRING_SERIALIZER);

        tree.put("key1", "val1");
        tree.put("key2", "val2");
        tree.put("key3", "val3");
        tree.put("key4", "val4");
        tree.put("key5", "val5");
        tree.put("key6", "val6"); // should trigger splits

        assertEquals("val1", tree.get("key1"));
        assertEquals("val6", tree.get("key6"));
        assertEquals(6, tree.size());

        tree.remove("key3");
        assertNull(tree.get("key3"));
        assertEquals(5, tree.size());

        tree.close();

        // Reopen to verify persistence
        NPageStore pageStore2 = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<String, String> tree2 = NBPlusTree.of(pageStore2, 5, false, STRING_SERIALIZER, STRING_SERIALIZER);

        assertEquals("val1", tree2.get("key1"));
        assertEquals("val6", tree2.get("key6"));
        assertNull(tree2.get("key3"));
        assertEquals(5, tree2.size());

        tree2.close();
    }

    @Test
    public void testDuplicatesAndSearchOnDisk() throws IOException {
        NPageStore pageStore = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<String, String> tree = NBPlusTree.of(pageStore, 3, true, STRING_SERIALIZER, STRING_SERIALIZER);

        tree.put("dup", "val1");
        tree.put("unique", "val_unique");
        tree.put("dup", "val2");
        tree.put("dup", "val3");

        List<String> dups = tree.search("dup");
        assertEquals(3, dups.size());
        assertTrue(dups.contains("val1"));
        assertTrue(dups.contains("val2"));
        assertTrue(dups.contains("val3"));

        tree.close();

        // Reopen and search
        NPageStore pageStore2 = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<String, String> tree2 = NBPlusTree.of(pageStore2, 3, true, STRING_SERIALIZER, STRING_SERIALIZER);

        List<String> dups2 = tree2.search("dup");
        assertEquals(3, dups2.size());
        assertTrue(dups2.contains("val1"));
        assertEquals("val_unique", tree2.get("unique"));

        tree2.close();
    }

    @Test
    public void testStressUniqueKeysOnDisk() throws IOException {
        NPageStore pageStore = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<Integer, Integer> tree = NBPlusTree.of(pageStore, 5, false, INT_SERIALIZER, INT_SERIALIZER);

        int total = 1000;
        for (int i = 0; i < total; i++) {
            tree.put(i, i * 10);
        }
        assertEquals(total, tree.size());

        for (int i = 0; i < total; i++) {
            assertEquals(i * 10, tree.get(i), "Failed at index " + i);
        }

        // Delete odd keys
        int removedCount = 0;
        for (int i = 1; i < total; i += 2) {
            if (tree.remove(i,null)) {
                removedCount++;
            } else {
                System.out.println("First disk deletion failure key: " + i);
                break;
            }
        }

        tree.close();

        // Reopen to verify
        NPageStore pageStore2 = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<Integer, Integer> tree2 = NBPlusTree.of(pageStore2, 5, false, INT_SERIALIZER, INT_SERIALIZER);

        assertEquals(total / 2, tree2.size());
        for (int i = 0; i < total; i++) {
            if (i % 2 == 1) {
                assertNull(tree2.get(i));
            } else {
                assertEquals(i * 10, tree2.get(i));
            }
        }
        tree2.close();
    }
}
