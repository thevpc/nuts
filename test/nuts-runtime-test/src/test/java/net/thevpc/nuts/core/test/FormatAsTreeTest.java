package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.io.util.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.text.art.tree.DefaultNTextArtTreeRenderer;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMaps;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class FormatAsTreeTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        Map<String,Object> m = NMaps.of(
                "a",2,
                "b", NMaps.of("c",3)
        );

        NMemoryPrintStream a = NPrintStream.ofMem(NTerminalMode.FILTERED);
        a.print(m);
        TestUtils.println(a);

//        out.print(NObjectWriter.of(value.duration())
//                .configure(true,
//                        "--mode",
//                        (formatMode == null ? NDurationFormatMode.DEFAULT : formatMode).id())
//                .format(value.duration()));


//        TestUtils.println(NObjectWriter.of(m)
//                .formatPlain(m));
    }

    @Test
    public void test02() {
        Map<String,Object> m = NMaps.of(
                "a",2,
                "b", NMaps.of("c",new Object[]{NMaps.of("e",3),NMaps.of("e",3),3},"d",3),
                "d", NMaps.of("e",3)
        );
        ByteArrayPrintStream bos = new ByteArrayPrintStream();
        NObjectObjectWriter.of()
                .setOutputFormat(NContentType.TREE)
                .println(m, bos);

        TestUtils.println("\n"+bos);
    }

    @Test
    public void test03() {
    Map<String, Object> m = NMaps.of(
            "name", "root",
            "version", 42,
            "enabled", true,
            "score", 3.14,
            "nothing", null,
            "tags", new String[]{"alpha", "beta", "gamma"},
            "emptyList", new Object[]{},
            "child", NMaps.of(
                    "name", "child-1",
                    "value", 100,
                    "nested", NMaps.of(
                            "deep", NMaps.of(
                                    "deeper", NMaps.of(
                                            "deepest", "bottom",
                                            "count", 0
                                    ),
                                    "sibling", "next-to-deepest"
                            ),
                            "items", new Object[]{1, "two", 3.0, null, true}
                    )
            ),
            "siblings", new Object[]{
                    NMaps.of("id", 1, "label", "first",  "active", true),
                    NMaps.of("id", 2, "label", "second", "active", false),
                    NMaps.of("id", 3, "label", "third",  "active", true,
                            "extra", NMaps.of("note", "only-on-third", "codes", new int[]{10, 20, 30}))
            },
            "unicode", "こんにちは",
            "special", "line1\nline2\ttabbed"
    );
        ByteArrayPrintStream bos = new ByteArrayPrintStream();
        NObjectObjectWriter.of()
                .setOutputFormat(NContentType.TREE)
                .println(m, bos);

        TestUtils.println("\n"+bos);
    }

    @Test
    public void test02_siblings_only() {
        Map<String,Object> m = NMaps.of(
                "siblings", new Object[]{
                        NMaps.of("id",1, "label","first"),
                        NMaps.of("id",2, "label","second"),
//                        NMaps.of("id",3, "label","third")
                }
        );
        ByteArrayPrintStream bos = new ByteArrayPrintStream();
        NObjectObjectWriter.of()
                .setOutputFormat(NContentType.TREE)
                .println(m, bos);
        TestUtils.println("\n"+bos);
    }

    @Test
    public void test_renderer_prefix() {
        // manually build:
        // root
        // └── siblings
        //       ├── id=1, label=first
        //       └── id=2, label=second

        NTreeNode tree = NTreeNode.of(NText.ofBlank(),
                NTreeNode.of(NText.of("siblings"),
                        NTreeNode.of(NText.of("id=1"),
                                NTreeNode.of(NText.of("label=first"))
                        ),
                        NTreeNode.of(NText.of("id=2"),
                                NTreeNode.of(NText.of("label=second"))
                        )
                )
        );

        DefaultNTextArtTreeRenderer renderer = new DefaultNTextArtTreeRenderer("test");
        TestUtils.println("\n"+renderer.render(tree).filteredText());
    }

    @Test
    public void test_renderer_anonymous_wrapper() {
        // simulating what XNode produces for an array of maps:
        // root
        // └── siblings
        //       ├── [blank anonymous node]   ← the invisible wrapper
        //       │     ├── id=1
        //       │     └── label=first
        //       └── [blank anonymous node]
        //             ├── id=2
        //             └── label=second

        NTreeNode tree = NTreeNode.of(NText.ofBlank(),
                NTreeNode.of(NText.of("siblings"),
                        NTreeNode.of(NText.ofBlank(),          // ← anonymous
                                NTreeNode.of(NText.of("id=1")),
                                NTreeNode.of(NText.of("label=first"))
                        ),
                        NTreeNode.of(NText.ofBlank(),          // ← anonymous
                                NTreeNode.of(NText.of("id=2")),
                                NTreeNode.of(NText.of("label=second"))
                        )
                )
        );

        DefaultNTextArtTreeRenderer renderer = new DefaultNTextArtTreeRenderer("test");
        TestUtils.println("\n"+renderer.render(tree).filteredText());

        // simulating what XNode produces for an array of maps:
        // root
        // └── siblings
        //       ├── [blank anonymous node]   ← the invisible wrapper
        //       │     ├── id=1
        //       │     └── label=first
        //       └── [blank anonymous node]
        //             ├── id=2
        //             └── label=second

        tree = NTreeNode.of(NText.ofBlank(),
                NTreeNode.of(NText.of("siblings"),
                        NTreeNode.of(NText.of("A"),          // ← anonymous
                                NTreeNode.of(NText.of("id=1")),
                                NTreeNode.of(NText.of("label=first"))
                        ),
                        NTreeNode.of(NText.of("B"),          // ← anonymous
                                NTreeNode.of(NText.of("id=2")),
                                NTreeNode.of(NText.of("label=second"))
                        )
                )
        );

        renderer = new DefaultNTextArtTreeRenderer("test");
        TestUtils.println("\n"+renderer.render(tree).filteredText());
    }
}
