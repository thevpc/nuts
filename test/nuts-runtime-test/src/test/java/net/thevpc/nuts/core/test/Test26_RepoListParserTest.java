package net.thevpc.nuts.core.test;

import net.thevpc.nuts.spi.NutsRepositoryLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test26_RepoListParserTest {


    @Test
    public void test01() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("maven-local=maven@/home/me/.m2/repository");
        Assertions.assertEquals(r.getName(), "maven-local");
        Assertions.assertEquals(r.getType(), "maven");
        Assertions.assertEquals(r.getLocation(), "/home/me/.m2/repository");
    }

    @Test
    public void test02() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("maven@/home/me/.m2/repository");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), "maven");
        Assertions.assertEquals(r.getLocation(), "/home/me/.m2/repository");
    }

    @Test
    public void test03() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("/home/me/.m2/repository");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), "/home/me/.m2/repository");
    }

    @Test
    public void test04() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("repository");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), "repository");
    }

    @Test
    public void test05() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("repository=");
        Assertions.assertEquals(r.getName(), "repository");
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test06() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("maven@");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), "maven");
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test07() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test08() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of(null);
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test09() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("@");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test10() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("=");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test11() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("=@");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), null);
    }

    @Test
    public void test12() {
        NutsRepositoryLocation r = NutsRepositoryLocation.of("@=");
        Assertions.assertEquals(r.getName(), null);
        Assertions.assertEquals(r.getType(), null);
        Assertions.assertEquals(r.getLocation(), "=");
    }
}
