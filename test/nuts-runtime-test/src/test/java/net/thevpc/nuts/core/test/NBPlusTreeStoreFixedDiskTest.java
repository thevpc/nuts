package net.thevpc.nuts.core.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import net.thevpc.nuts.runtime.standalone.util.collections.NBPlusTreeStoreMem;
import net.thevpc.nuts.util.NBPlusTreeStore;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.thevpc.nuts.runtime.standalone.util.collections.NBPlusTreeImpl;
import net.thevpc.nuts.runtime.standalone.util.collections.NBPlusTreeStoreFixedDisk;

public class NBPlusTreeStoreFixedDiskTest {

    private File dbFile;

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
        if(!_TestConfig.ENABLE_WIP){
            return;
        }
        NBPlusTreeStoreFixedDisk.NBSerializer<String> serializer = new NBPlusTreeStoreFixedDisk.NBSerializer<String>() {
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

        NBPlusTreeStore<String, String> store = new NBPlusTreeStoreFixedDisk<>(dbFile, 5, false, serializer, serializer);
        //NBPlusTreeStore<String, String> store = new NBPlusTreeStoreMem<>();
        NBPlusTreeImpl<String, String> tree = new NBPlusTreeImpl<>(store);

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

        store.close();

        // Reopen to verify persistence
        NBPlusTreeStoreFixedDisk<String, String> store2 = new NBPlusTreeStoreFixedDisk<>(dbFile, 5, false, serializer, serializer);
        NBPlusTreeImpl<String, String> tree2 = new NBPlusTreeImpl<>(store2);

        assertEquals("val1", tree2.get("key1"));
        assertEquals("val6", tree2.get("key6"));
        assertNull(tree2.get("key3"));
        assertEquals(5, tree2.size());

        store2.close();
    }
}
