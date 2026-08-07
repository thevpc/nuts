package net.thevpc.nuts.core.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NDataSerializer;
import net.thevpc.nuts.io.NPageStore;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.collections.NBPlusTree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NBPlusTreeStoreFixedDiskTest {

    private File dbFile;

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

        NPageStore pageStore = NPageStore.ofFile(NPath.of(dbFile), 4096);
        NBPlusTree<String, String> tree = NBPlusTree.of(pageStore, 5, false, serializer, serializer);

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
        NBPlusTree<String, String> tree2 = NBPlusTree.of(pageStore2, 5, false, serializer, serializer);

        assertEquals("val1", tree2.get("key1"));
        assertEquals("val6", tree2.get("key6"));
        assertNull(tree2.get("key3"));
        assertEquals(5, tree2.size());

        tree2.close();
    }
}
