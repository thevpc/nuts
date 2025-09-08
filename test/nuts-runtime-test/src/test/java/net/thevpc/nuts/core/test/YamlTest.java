/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author thevpc
 */
public class YamlTest {


    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test1() throws Exception {
        String path = "net/thevpc/nuts/core/test/blackbox/yaml1.yml";
        NElement e = NElementParser.ofYaml().parse(getClass().getClassLoader().getResource(path), NElement.class);
        NElementWriter.ofJson()
                .setCompact(false)
                .writeln(e);
        NElementWriter.ofTson()
                .setCompact(false)
                .writeln(e);
        NElementWriter.ofYaml()
                .setCompact(false)
                .writeln(e);
    }
    @Test
    public void test2() throws Exception {
        String ee = NElement.ofString("a").toString();
        NElement e = NElementParser.ofYaml().parse("\n" +
                "id: changelog070\n" +
                "title: Version 0.7.2.0 released\n" +
                "sub_title: Publishing 0.7.2.0 version\n" +
                "author: thevpc\n" +
                "author_title: Criticize the world Casually...\n" +
                "author_url: https://github.com/thevpc\n" +
                "author_image_url: https://avatars3.githubusercontent.com/u/10106809?s=460&u=28d1736bdf0b6e6f81981b3a2ebbd2db369b25c8&v=4\n" +
                "tags: [nuts]\n" +
                "publish_date: 2020-09-23", NElement.class);
        NElementWriter.ofJson()
                .setCompact(false)
                .writeln(e);
        NElementWriter.ofTson()
                .setCompact(false)
                .writeln(e);
        NElementWriter.ofYaml()
                .setCompact(false)
                .writeln(e);
    }

}
