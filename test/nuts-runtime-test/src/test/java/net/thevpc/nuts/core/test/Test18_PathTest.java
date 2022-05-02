/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NutsPath;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author thevpc
 */
public class Test18_PathTest {

    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewTestWorkspace();
    }


    @Test
    public void testPathCreation() {

        TestUtils.println(NutsPath.of("./", session));
        TestUtils.println(NutsPath.of(".", session));
        TestUtils.println(NutsPath.of("..", session));
        TestUtils.println(NutsPath.of("/*", session));
        Assertions.assertTrue(true);
    }

    @Test
    public void testPathTypes1() {
        NutsPath s = NutsPath.of("http://maven.ibiblio.org/maven2/archetype-catalog.xml", session);

        //this is a remote file
        Assertions.assertFalse(s.isFile());
        //the file is actually a http url
        Assertions.assertTrue(s.isURL());
    }

    @Test
    public void testPathTypes2() {

        NutsPath s = NutsPath.of("file://maven.ibiblio.org/maven2/archetype-catalog.xml", session);
        //the file has an 'authority' (//) so it cannot be converted to a valid file
        Assertions.assertFalse(s.isFile());
        //the file is actually a file url
        Assertions.assertTrue(s.isURL());

    }

    @Test
    public void testPathTypes3() {

        NutsPath s = NutsPath.of("file:/maven.ibiblio.org/maven2/archetype-catalog.xml", session);
        //the file is actually a file url
        Assertions.assertTrue(s.isFile());
        //the file is actually a URL
        Assertions.assertTrue(s.isURL());

    }

    @Test
    public void testPathTypes4() {

//        s = CoreIOUtils.createInputSource("zip://maven.ibiblio.org/maven2/toto.zip?archetype-catalog.xml");
//        Assertions.assertFalse(s.isPath());
//        Assertions.assertTrue(s.isURL());
        NutsPath s = NutsPath.of("/maven.ibiblio.org/maven2/archetype-catalog.xml", session);
        //the file is actually a file
        Assertions.assertTrue(s.isFile());
        //the file can be converted to URL
        Assertions.assertTrue(s.isURL());
    }

    @Test
    public void testHtmlfs1() {
        NutsPath s = NutsPath.of("dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/", session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertTrue(!children.isEmpty());
        TestUtils.println("------------ WALK ----------");
        s.walk().forEach(x -> {
            TestUtils.println(x);
        });
        long c = s.walk().count();
        Assertions.assertTrue(c >= 2);
    }

    @Test
    public void testHtmlfs2() {
        NutsPath s = NutsPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-tomcat-01.html"), session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
    }

    @Test
    public void testHtmlfs3() {
        NutsPath s = NutsPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-tomcat-02.html"), session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
    }

    @Test
    public void testHtmlfs4() {
        NutsPath s = NutsPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-archive-apache-01.html"), session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
        TestUtils.println("------------ WALK ----------");
    }


    @Test
    public void testHtmlfs5() {
        NutsPath s = NutsPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-maven-central-01.html"), session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(13, children.size());
    }

    @Test
    public void testHtmlfs6() {
        NutsPath s = NutsPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-jetty-01.html"), session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(2, children.size());
    }

    /**
     * cannot test because of rate limit
     * {"message":"API rate limit exceeded for 196.235.210.26. (But here's the good news: Authenticated requests get a higher rate limit. Check out the documentation for more details.)","documentation_url":"https://docs.github.com/rest/overview/resources-in-the-rest-api#rate-limiting"}
     */
    @Test
    public void testGithubfs() {
        NutsPath s = NutsPath.of("githubfs:https://api.github.com/repos/thevpc/nuts/contents", session);
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NutsPath nutsPath : s.list()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
//        Assertions.assertTrue(children.contains("htmlfs:https://thevpc.net/maven/net/thevpc/nuts/nuts/0.8.3"));
//        Assertions.assertTrue(children.contains("https://thevpc.net/maven/net/thevpc/nuts/nuts/maven-metadata-local.xml"));
//        Assertions.assertTrue(children.contains("htmlfs:https://thevpc.net/maven/net/thevpc/nuts/nuts/0.8.2"));
//        TestUtils.println("------------ WALK ----------");
//        s.walk(1).forEach(x -> {
//            TestUtils.println(x);
//        });
//        long c = s.walk().count();
//        Assertions.assertTrue(c>=12);
    }

    @Test
    public void testHome() {
        Assertions.assertEquals(System.getProperty("user.home"), NutsPath.ofUserHome(session).toString());
    }

    @Test
    public void testInvalidPath02() {
        NutsPath a = NutsPath.of(System.getProperty("user.home") + "/*", session);
        List<NutsPath> found = a.walkGlob().toList();
        TestUtils.println(found);
        String[] expected = new File(System.getProperty("user.home")).list();
        Assertions.assertEquals(expected==null?0:expected.length,found.size());
    }

    @Test
    public void testInvalidPath03() {
        TestUtils.println(Paths.get("c:/").getFileName());
        TestUtils.println(Paths.get("c:/").getNameCount());
        TestUtils.println(Paths.get("c:").getFileName());
        TestUtils.println(Paths.get("c:").getNameCount());
        TestUtils.println(Paths.get("/").getFileName());
        TestUtils.println(Paths.get("").getFileName());

        NutsPath a = NutsPath.of("*", session);
        List<NutsPath> found = a.walkGlob().toList();
        TestUtils.println(found);
        String[] expected = new File(".").list();
        Assertions.assertEquals(expected==null?0:expected.length,found.size());
    }

    @Test
    public void testInvalidPath04() {
        NutsPath a = NutsPath.of("/*", session);
        List<NutsPath> nutsPaths = a.walkGlob().toList();
        System.out.println(nutsPaths);
    }
}
