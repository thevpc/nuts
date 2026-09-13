package net.thevpc.nuts.core.test.heavy;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HeavyInstallTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("---local-urls");
    }

    @Test
    public void testInstallNsh() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("nsh", "-c", "echo", "hello world");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertEquals(0, e);
        Assertions.assertEquals("hello world", str.trim());
    }

    @Test
    public void testInstallKifkif() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("net.thevpc.kifkif:kifkif", "--version");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertEquals(0, e);
    }

    @Test
    public void testInstallNetbeans() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("org.apache.netbeans:netbeans#31", "--help");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertTrue(str.contains("General options:"));
        Assertions.assertTrue(str.contains("--close-group"));
        Assertions.assertEquals(2, e);
    }


    @Test
    public void testInstallTomcat() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("org.apache.catalina:apache-tomcat", "version");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertTrue(str.contains("Server version: Apache Tomcat"));
        Assertions.assertEquals(0, e);
    }


    @Test
    public void testInstallPostgres() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("org.postgresql:postgresql-server#18.6.0", "postgres", "--version");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertTrue(str.contains("postgres (PostgreSQL)"));
        Assertions.assertEquals(0, e);
    }

    @Test
    public void testInstallMvn() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NExec ee = NExec.of("org.apache.maven:mvn#4.0.0-rc-6", "mvn", "--version");
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertTrue(str.contains("Apache Maven"));
        Assertions.assertTrue(str.contains("Maven home"));
        Assertions.assertEquals(0, e);
    }

    @Test
    public void testInstallNoApi() {
        if (!NPath.ofUserHome().resolve(".nuts/local-urls").isDirectory()) {
            return;
        }
        NPath folder = NPath.ofTempFolder();
        NPath.of("classpath://net/thevpc/nuts/core/test/sample-api-01.json").copyTo(folder.resolve("api.json"));
        NExec ee = NExec.of("noapi").directory(folder);
        ee.grabAll();
        int e = ee.run().exitCode();
        String str = ee.grabbedAll();
        Assertions.assertTrue(str.contains("read open-api file"));
        Assertions.assertTrue(str.contains("generate  pdf file"));
        Assertions.assertEquals(0, e);
        Assertions.assertTrue(folder.resolve("dist-version-1.2.3/api-1.2.3.pdf").isRegularFile());
    }

}
