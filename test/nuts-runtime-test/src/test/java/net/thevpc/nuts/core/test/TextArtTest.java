/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.format.NMutableTableModel;
import net.thevpc.nuts.format.NTableModel;
import net.thevpc.nuts.format.NTreeNode;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vpc
 */
public class TextArtTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test1() {
        NTextArt art = NTextArt.of();
        NText text = NText.of("hello world \uD83D\uDE80\uD83D\uDE80\uD83D\uDE80");
        NOut.println(art.getTextRenderer("figlet:standard").get().render(text));
        NOut.println(art.getTextRenderer("figlet:big").get().render(text));
        NOut.println(art.getTextRenderer("pixel:standard").get().render(text));
        NOut.println(art.getImageRenderer("pixel:standard").get().setFontSize(20).setOutputColumns(60).render(text));
        for (NTextArtRenderer renderer : art.getRenderers()) {
            NOut.println("----------------------------------------------------------------------------------------");
            NOut.println(renderer.getName());
            NOut.println("----------------------------------------------------------------------------------------");
            if (renderer instanceof NTextArtImageRenderer) {
                NOut.println(((NTextArtImageRenderer) renderer).setOutputColumns(0).render(NText.of(renderer.getName())));
            } else if (renderer instanceof NTextArtTextRenderer) {
                NOut.println(((NTextArtTextRenderer) renderer).render(NText.of(renderer.getName())));
            }
        }
    }

    @Test
    public void test2() {
        NTextArt art = NTextArt.of();
        NText text = NText.of("hello world \uD83D\uDE80\uD83D\uDE80\uD83D\uDE80");
        NOut.println(art.getTextRenderer("figlet:standard").get().render(text));
        NOut.println(art.getTextRenderer("figlet:big").get().render(text));
        NOut.println(art.getTextRenderer("pixel:standard").get().render(text));
        NOut.println(art.getImageRenderer("pixel:standard").get().setFontSize(20).setOutputColumns(60).render(text));
        for (NTextArtRenderer renderer : art.getRenderers()) {
            NOut.println("----------------------------------------------------------------------------------------");
            NOut.println(renderer.getName());
            NOut.println("----------------------------------------------------------------------------------------");
            if (renderer instanceof NTextArtImageRenderer) {
                NOut.println(((NTextArtImageRenderer) renderer).setOutputColumns(0).render(NText.of(renderer.getName())));
            } else if (renderer instanceof NTextArtTextRenderer) {
                NOut.println(((NTextArtTextRenderer) renderer).render(NText.of(renderer.getName())));
            }
        }
    }


    @Test
    public void test3() {
        NTextArt art = NTextArt.of();

        class MyNode implements NTreeNode {
            int value;

            public MyNode(int value) {
                this.value = value;
            }

            @Override
            public NText value() {
                return art.getTableRenderer().get().render(NTableModel.of().addRow(NText.of(value)));
            }

            @Override
            public List<NTreeNode> children() {
                return (value < 3) ? Arrays.<Integer>asList(value + 1, value + 2).stream().map(MyNode::new).collect(Collectors.toList())
                        : Collections.emptyList();
            }
        }
        NTreeNode tree = new MyNode(1);
        NOut.println(art.getTreeRenderer().get().render(tree));

    }


    @Test
    public void test4() {
        NTextArt art = NTextArt.of();
        NText text = NText.of("hello world");
        NOut.println(art.getTextRenderer("figlet:standard").get().render(text));
        NOut.println(art.getImageRenderer("pixel:standard").get()
                .setFontSize(20) .setOutputColumns(60) .render(text));

    }

    @Test
    public void test5() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addHeaderRow(NText.of("Name"), NText.of("Status"))
                .addRow(NText.of("adam"), NText.ofStyled("active", NTextStyle.italic()))
                .addRow(NText.of("eve"), NText.ofStyled("inactive", NTextStyle.success()));
        NOut.println(art.getTableRenderer().get().render(table));

        NOut.println(art.getTableRenderer("table:spaces").get().render(table));
    }
    @Test
    public void test6() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addHeaderRow(NText.of("Name"), NText.of("Status"))
                .addRow(NText.of("adam\nwas\nhere"), NText.ofStyled("active", NTextStyle.italic()))
                .addRow(NText.of("eve"), NText.ofStyled("inactive", NTextStyle.success()));
        for (NTextArtTableRenderer renderer : art.getTableRenderers()) {
            NOut.println(renderer.getName()+"::");
//            NOut.println(art.getDefaultTextRenderer().get().render(NText.of(renderer.getName())));
            NOut.println(renderer.render(table));
        }
    }
    @Test
    public void test7() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addRow(NText.of("adam\nwas\nhere"),NText.of("adam\nwill be\nhere"))
                .addRow(NText.of("adam\nhere"),NText.of("adam\nis\nhere"))
                ;
        NOut.println(art.getTableRenderer().get().render(table));
    }
    @Test
    public void test8() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addRow(NText.of("adam\nwas\nhere"))
                .addRow(NText.of("adam\nhere"),NText.of("adam\nis\nhere"))
                .setCellColSpan(0, 0, 2)
                ;
        NOut.println(art.getTableRenderer().get().render(table));
    }

    @Test
    public void test9() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addRow(NText.of("adam\nwas\nhere"))
                .addRow(NText.of("adam\nhere"),NText.of("adam\nis\nhere"),NText.of(3))
                .setCellColSpan(0, 0, 3)
                ;
        NOut.println(art.getTableRenderer().get().render(table));
    }
    @Test
    public void test10() {
        NTextArt art = NTextArt.of();
        NMutableTableModel table = NTableModel.of()
                .addRow(NText.of("tall\ncell"), NText.of("short"))
                .addRow(NText.of(""), NText.of("another"))
                .setCellRowSpan(0, 0, 2)  // First cell spans 2 rows
                ;
        NOut.println(art.getTableRenderer().get().render(table));
    }
}
