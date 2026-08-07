package net.thevpc.nuts.core.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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

        NPageStore pageStore = NPageStore.ofInMemory(4096);
        try (NBPlusTree<String, String> tree = NBPlusTree.of(pageStore, 5, false, serializer, serializer)) {
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
}
