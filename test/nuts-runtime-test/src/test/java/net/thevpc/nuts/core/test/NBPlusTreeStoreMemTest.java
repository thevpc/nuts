package net.thevpc.nuts.core.test;

import net.thevpc.nuts.collections.NBPlusTreeStore;
import net.thevpc.nuts.runtime.standalone.util.collections.NBPlusTreeImpl;
import net.thevpc.nuts.runtime.standalone.util.collections.NBPlusTreeStoreMem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NBPlusTreeStoreMemTest {

    @Test
    public void testBasicCrud() {
        NBPlusTreeStore<String, String> store = new NBPlusTreeStoreMem<>(5, false);
        NBPlusTreeImpl<String, String> tree = new NBPlusTreeImpl<>(store);

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
