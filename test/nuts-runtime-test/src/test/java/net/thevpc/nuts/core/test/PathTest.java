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

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathExtensionType;
import net.thevpc.nuts.io.NPathNameParts;
import net.thevpc.nuts.runtime.standalone.io.path.NPathFromSPI;
import net.thevpc.nuts.runtime.standalone.io.path.spi.GenericFilePath;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;
import net.thevpc.nuts.spi.NPathSPI;
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
public class PathTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void testPathCreation() {
        TestUtils.println(NPath.of("./"));
        TestUtils.println(NPath.of("."));
        TestUtils.println(NPath.of(".."));
        TestUtils.println(NPath.of("/*"));
        Assertions.assertTrue(true);
    }

    @Test
    public void testPathTypes1() {
        NPath s = NPath.of("http://maven.ibiblio.org/maven2/archetype-catalog.xml");

        //this is a remote file
        Assertions.assertFalse(s.isLocal());
        //the file is actually a http url
        Assertions.assertTrue(s.isURL());
    }

    @Test
    public void testPathTypes2() {

        NPath s = NPath.of("file://maven.ibiblio.org/maven2/archetype-catalog.xml");
        //the file has an 'authority' (//) so it cannot be converted to a valid file
        Assertions.assertTrue(s.isLocal());
        //the file is actually a file url
        Assertions.assertTrue(s.isURL());

    }

    @Test
    public void testPathTypes3() {

        NPath s = NPath.of("file:/maven.ibiblio.org/maven2/archetype-catalog.xml");
        //the file is actually a file url
        Assertions.assertTrue(s.isLocal());
        //the file is actually a URL
        Assertions.assertTrue(s.isURL());

    }

    @Test
    public void testPathTypes4() {

//        s = CoreIOUtils.createInputSource("zip://maven.ibiblio.org/maven2/toto.zip?archetype-catalog.xml");
//        Assertions.assertFalse(s.isPath());
//        Assertions.assertTrue(s.isURL());
        NPath s = NPath.of("/maven.ibiblio.org/maven2/archetype-catalog.xml");
        //the file is actually a file
        Assertions.assertTrue(s.isLocal());
        //the file can be converted to URL
        Assertions.assertTrue(s.isURL());
    }

    @Test
    public void testHtmlfs1() {
        NPath s = NPath.of("dotfilefs:https://raw.githubusercontent.com/thevpc/nuts-preview/master/net/thevpc/nuts/nuts/");
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
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
        NPath s = NPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-tomcat-01.html"));
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
    }

    @Test
    public void testHtmlfs3() {
        NPath s = NPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-tomcat-02.html"));
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
    }

    @Test
    public void testHtmlfs4() {
        NPath s = NPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-archive-apache-01.html"));
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(3, children.size());
        TestUtils.println("------------ WALK ----------");
    }


    @Test
    public void testHtmlfs5() {
        NPath s = NPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-maven-central-01.html"));
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
        Assertions.assertEquals(13, children.size());
    }

    @Test
    public void testHtmlfs6() {
        NPath s = NPath.of("htmlfs:" + getClass().getResource("/net/thevpc/nuts/core/test/htmlfs-jetty-01.html"));
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
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
        NPath s = NPath.of("githubfs:https://api.github.com/repos/thevpc/nuts/contents");
        TestUtils.println("------------ LIST ----------");
        Set<String> children = new HashSet<>();
        for (NPath nutsPath : s.stream()) {
            TestUtils.println(nutsPath);
            children.add(nutsPath.toString());
        }
//        Assertions.assertTrue(children.contains("htmlfs:https://maven.thevpc.net/net/thevpc/nuts/nuts/0.8.3"));
//        Assertions.assertTrue(children.contains("https://maven.thevpc.net/net/thevpc/nuts/nuts/maven-metadata-local.xml"));
//        Assertions.assertTrue(children.contains("htmlfs:https://maven.thevpc.net/net/thevpc/nuts/nuts/0.8.2"));
//        TestUtils.println("------------ WALK ----------");
//        s.walk(1).forEach(x -> {
//            TestUtils.println(x);
//        });
//        long c = s.walk().count();
//        Assertions.assertTrue(c>=12);
    }

    @Test
    public void testHome() {
        Assertions.assertEquals(System.getProperty("user.home"), NPath.ofUserHome().toString());
    }

    @Test
    public void testInvalidPath02() {
        NPath a = NPath.of(System.getProperty("user.home") + "/*");
        List<NPath> found = a.walkGlob().toList();
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

        NPath a = NPath.of("*");
        List<NPath> found = a.walkGlob().toList();
        TestUtils.println(found);
        String[] expected = new File(".").list();
        Assertions.assertEquals(expected==null?0:expected.length,found.size());
    }

    @Test
    public void testInvalidPath04() {
        NPath a = NPath.of("/*");
        List<NPath> nutsPaths = a.walkGlob().toList();
        System.out.println(nutsPaths);
    }

    @Test
    public void testThevpcPath() {
        NPath a = NPath.of("htmlfs:https://maven.thevpc.net/net/thevpc/nuts/toolbox/noapi/");
        List<NPath> nutsPaths = a.list();
        System.out.println(nutsPaths);
    }


    @Test
    public void testGenericFile() {
        NCallableSupport<NPathSPI> p = new GenericFilePath.GenericPathFactory().createPath("C:", null, null);
        NPathFromSPI u = new NPathFromSPI(p.call());
        NPath root = u.getRoot();
        System.out.println(root);
    }

    @Test
    public void testGenericFile2() {
        NCallableSupport<NPathSPI> p = new GenericFilePath.GenericPathFactory().createPath("/C:", null, null);
        NPathFromSPI u = new NPathFromSPI(p.call());
        NPath root = u.getRoot();
        System.out.println(root);
    }

    //--------------

    @Test
    public void test01() {
        NPathParts h = new NPathParts("http://a:password@here.com/a/b/c?a=toz");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com", h.getAuthority());
        Assertions.assertEquals("/a/b/c", h.getFile());
        Assertions.assertEquals("a=toz", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test02() {
        NPathParts h = new NPathParts("http://a:password@here.com:12?a=toz/be");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("a=toz/be", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test03() {
        NPathParts h = new NPathParts("http://a:password@here.com:12/");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("/", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("", h.getRef());
    }

    @Test
    public void test04() {
        NPathParts h = new NPathParts("http://a:password@here.com:12#something");
        Assertions.assertEquals(NPathParts.Type.URL, h.getType());
        Assertions.assertEquals("http", h.getProtocol());
        Assertions.assertEquals("a:password@here.com:12", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("something", h.getRef());
    }

    @Test
    public void test05() {
        NPathParts h = new NPathParts("#something");
        Assertions.assertEquals(NPathParts.Type.REF, h.getType());
        Assertions.assertEquals("", h.getProtocol());
        Assertions.assertEquals("", h.getAuthority());
        Assertions.assertEquals("", h.getFile());
        Assertions.assertEquals("", h.getQuery());
        Assertions.assertEquals("something", h.getRef());
    }

    @Test
    public void test06() {
        Assertions.assertArrayEquals(new String[]{"a1.2", "", ""}, d("a1.2"));
        Assertions.assertArrayEquals(new String[]{"a1.2", "a", ".a"}, d("a1.2.a"));
        Assertions.assertArrayEquals(new String[]{"", "", ""}, d(""));
        Assertions.assertArrayEquals(new String[]{"", "", "."}, d("."));
        Assertions.assertArrayEquals(new String[]{"a", "", ""}, d("a"));
        Assertions.assertArrayEquals(new String[]{"a", "", "."}, d("a."));
        Assertions.assertArrayEquals(new String[]{"", "a", ".a"}, d(".a"));
        Assertions.assertArrayEquals(new String[]{"", "1", ".1"}, d(".1"));
        Assertions.assertArrayEquals(new String[]{"a", "1", ".1"}, d("a.1"));
        Assertions.assertArrayEquals(new String[]{"1.1", "", ""}, d("1.1"));
        Assertions.assertArrayEquals(new String[]{"1", "a1", ".a1"}, d("1.a1"));
        Assertions.assertArrayEquals(new String[]{"1", "1a", ".1a"}, d("1.1a"));
        Assertions.assertArrayEquals(new String[]{"1", "3af", ".3af"}, d("1.3af"));
        Assertions.assertArrayEquals(new String[]{"a1", "2s", ".2s"}, d("a1.2s"));
        Assertions.assertArrayEquals(new String[]{"a.config", "json", ".json"}, d("a.config.json"));
        Assertions.assertArrayEquals(new String[]{"some-value-1.8-20250206-external", "pdf", ".pdf"}, d("some-value-1.8-20250206-external.pdf"));
    }

    @Test
    public void test07() {
        Assertions.assertEquals(NPathNameParts.ofLong("a", "b.c"), NIOUtils.getPathNameParts("a.b.c", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", null), NIOUtils.getPathNameParts("", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", ""), NIOUtils.getPathNameParts(".", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("", "a"), NIOUtils.getPathNameParts(".a", NPathExtensionType.LONG));
        Assertions.assertEquals(NPathNameParts.ofLong("a", ""), NIOUtils.getPathNameParts("a.", NPathExtensionType.LONG));

        Assertions.assertEquals(NPathNameParts.ofShort("a.b", "c"), NIOUtils.getPathNameParts("a.b.c", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", null), NIOUtils.getPathNameParts("", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", ""), NIOUtils.getPathNameParts(".", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("", "a"), NIOUtils.getPathNameParts(".a", NPathExtensionType.SHORT));
        Assertions.assertEquals(NPathNameParts.ofShort("a", ""), NIOUtils.getPathNameParts("a.", NPathExtensionType.SHORT));


    }

    private String[] d(String n) {
        NPath p = NPath.of(n);
        if (p == null) {
            return new String[]{"", "", ""};
        }
        NPathNameParts nameParts = p.nameParts(NPathExtensionType.SMART);
        String[] strings = {nameParts.getBaseName(), nameParts.getExtension(), nameParts.getFullExtension()};
        return strings;
    }


}
