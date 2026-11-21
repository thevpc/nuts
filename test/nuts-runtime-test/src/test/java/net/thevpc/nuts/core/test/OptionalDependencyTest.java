package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ARCH-03: the "optional" wrappers must load when jansi/mslinks are absent.
 * Previously they contained direct bytecode references to those classes.
 */
public class OptionalDependencyTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void optionalWrappersHaveNoBytecodeReferencesToOptionalLibraries() throws Exception {
        assertNoClassReference("net.thevpc.nuts.runtime.optional.jansi.OptionalJansi", "org/fusesource/jansi/");
        assertNoClassReference("net.thevpc.nuts.runtime.optional.mslink.OptionalMsLinkHelper", "mslinks/");
    }

    private static void assertNoClassReference(String className, String forbiddenPrefix) throws Exception {
        String resource = className.replace('.', '/') + ".class";
        byte[] bytes;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            Assertions.assertNotNull(in, "missing class resource " + resource);
            bytes = readAll(in);
        }
        List<String> referencedClasses = referencedClasses(bytes);
        List<String> forbidden = new ArrayList<>();
        for (String referenced : referencedClasses) {
            if (referenced.startsWith(forbiddenPrefix)) {
                forbidden.add(referenced);
            }
        }
        Assertions.assertTrue(forbidden.isEmpty(),
                className + " has direct bytecode references to optional libraries: " + forbidden);
    }

    /** Minimal constant-pool reader: collects CONSTANT_Class entries without needing ASM. */
    private static List<String> referencedClasses(byte[] classFile) throws Exception {
        DataInputStream in = new DataInputStream(new java.io.ByteArrayInputStream(classFile));
        Assertions.assertEquals(0xCAFEBABE, in.readInt());
        in.readUnsignedShort(); // minor
        in.readUnsignedShort(); // major
        int count = in.readUnsignedShort();
        Map<Integer, String> utf8 = new HashMap<>();
        List<Integer> classNames = new ArrayList<>();
        for (int i = 1; i < count; i++) {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 1: {
                    int length = in.readUnsignedShort();
                    byte[] value = new byte[length];
                    in.readFully(value);
                    utf8.put(i, new String(value, "UTF-8"));
                    break;
                }
                case 7:
                    classNames.add(in.readUnsignedShort());
                    break;
                case 8:
                case 16:
                case 19:
                case 20:
                    in.readUnsignedShort();
                    break;
                case 3:
                case 4:
                case 9:
                case 10:
                case 11:
                case 12:
                case 17:
                case 18:
                    in.readInt();
                    break;
                case 5:
                case 6:
                    in.readLong();
                    i++; // long/double occupy two constant-pool slots
                    break;
                case 15:
                    in.readUnsignedByte();
                    in.readUnsignedShort();
                    break;
                default:
                    throw new IllegalStateException("unknown constant-pool tag " + tag);
            }
        }
        List<String> result = new ArrayList<>();
        for (int index : classNames) {
            result.add(utf8.get(index));
        }
        return result;
    }

    private static byte[] readAll(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) > 0) {
            out.write(buffer, 0, n);
        }
        return out.toByteArray();
    }
}
