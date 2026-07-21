package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.spi.NRepositorySelectorList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class RepoListParserTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test01() {
        NRepositoryLocation r = NRepositoryLocation.of("maven-local=maven@/home/me/.m2/repository");
        Assertions.assertEquals(r.name(), "maven-local");
        Assertions.assertEquals(r.locationType(), "maven");
        Assertions.assertEquals(r.path(), "/home/me/.m2/repository");
    }

    @Test
    public void test02() {
        NRepositoryLocation r = NRepositoryLocation.of("maven@/home/me/.m2/repository");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), "maven");
        Assertions.assertEquals(r.path(), "/home/me/.m2/repository");
    }

    @Test
    public void test03() {
        NRepositoryLocation r = NRepositoryLocation.of("/home/me/.m2/repository");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), "/home/me/.m2/repository");
    }

    @Test
    public void test04() {
        NRepositoryLocation r = NRepositoryLocation.of("repository");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), "repository");
    }

    @Test
    public void test05() {
        NRepositoryLocation r = NRepositoryLocation.of("repository=");
        Assertions.assertEquals(r.name(), "repository");
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test06() {
        NRepositoryLocation r = NRepositoryLocation.of("maven@");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), "maven");
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test07() {
        NRepositoryLocation r = NRepositoryLocation.of("");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test08() {
        NRepositoryLocation r = NRepositoryLocation.of(null);
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test09() {
        NRepositoryLocation r = NRepositoryLocation.of("@");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test10() {
        NRepositoryLocation r = NRepositoryLocation.of("=");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test11() {
        NRepositoryLocation r = NRepositoryLocation.of("=@");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), null);
    }

    @Test
    public void test12() {
        NRepositoryLocation r = NRepositoryLocation.of("@=");
        Assertions.assertEquals(r.name(), null);
        Assertions.assertEquals(r.locationType(), null);
        Assertions.assertEquals(r.path(), "=");
    }

    @Test
    public void test13() {
        NRepositorySelectorList li = NRepositoryUtils.createRepositorySelectorList(Arrays.asList("+/toto")).get();
        TestUtils.println(li);
    }
}
