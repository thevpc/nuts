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
import net.thevpc.nuts.text.NText;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.thevpc.nuts.text.NTextArt;
import net.thevpc.nuts.text.NTextArtImageRenderer;
import net.thevpc.nuts.text.NTextArtRenderer;

/**
 *
 * @author vpc
 */
public class NTextArtTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test1() {
        NTextArt art = NTextArt.of();
        NText text = NText.of("hello world \uD83D\uDE80\uD83D\uDE80\uD83D\uDE80");
        NOut.println(art.getRenderer("figlet:standard").get().render(text));
        NOut.println(art.getRenderer("figlet:big").get().render(text));
        NOut.println(art.getRenderer("pixel:standard").get().render(text));
        NOut.println(art.getImageRenderer("pixel:standard").get().setFontSize(20).setOutputColumns(60).render(text));
        for (NTextArtRenderer renderer : art.getRenderers()) {
            NOut.println("----------------------------------------------------------------------------------------");
            NOut.println(renderer.getName());
            NOut.println("----------------------------------------------------------------------------------------");
            if (renderer instanceof NTextArtImageRenderer) {
                NOut.println(((NTextArtImageRenderer) renderer).setOutputColumns(0).render(NText.of(renderer.getName())));
            } else {
                NOut.println(renderer.render(NText.of(renderer.getName())));
            }
        }
    }
}
