/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test01_CreateTest {

    @Test
    public void minimal1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NutsSession session = TestUtils.openNewTestWorkspace("--workspace", wsPath,
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
        NutsWorkspace ws= session.getWorkspace();
        Assertions.assertEquals(wsPath + "/cache", ws.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(wsPath + "/cache/" + NutsConstants.Folders.REPOSITORIES + "/"+
                        ws.repos().getRepositories()[0].getName()+
                        "/"+ws.repos().getRepositories()[0].getUuid(),
                ws.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());

//        String str="     __        __    \n" +
//                "  /\\ \\ \\ _  __/ /______\n" +
//                " /  \\/ / / / / __/ ___/\n" +
//                "/ /\\  / /_/ / /_(__  )\n" +
//                "\\_\\ \\/\\__,_/\\__/____/\n";
//
//        String str="  /\\ _";
//        String str=" ```underlined prototype``` ";

//        System.out.println("---------------------------------");
//        System.out.println(str);
//        System.out.println("---------------------------------");
//        session.out().println(str);
//        NutsLogger _log = session.log().of("example");
//        _log.with()
//                .level(Level.INFO)
//                .log(str);
        String str=
                "a\n\nb"
                ;
        System.out.println("-----------------------");
        System.out.println(str);
        NutsText txt = session.text().parse(str);
        System.out.println("-----------------------");
        System.out.println(txt);
    }

    @Test
    public void minimal2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = TestUtils.openNewTestWorkspace(
                "--standalone",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions").getWorkspace();
    }

    @Test
    public void minimal3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = TestUtils.openNewTestWorkspace(
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions").getWorkspace();
    }

    @Test
    public void default1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);
        String wsPath = TestUtils.getTestBaseFolder().getPath();

        NutsWorkspace ws = TestUtils.openNewTestWorkspace(
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions").getWorkspace();
        NutsSession session = ws.createSession();
        ws=session.getWorkspace();
        Assertions.assertEquals(System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName(),
                ws.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(
                System.getProperty("user.home") + "/.cache/nuts/" + new File(wsPath).getName() + "/"
                + NutsConstants.Folders.REPOSITORIES + "/"
                + ws.repos().getRepositories()[0].getName()
                + "/" + ws.repos().getRepositories()[0].getUuid(),
                ws.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE).toString());
    }

    @Test
    public void default2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = TestUtils.openNewTestWorkspace(
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions").getWorkspace();
    }

    @Test
    public void default3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        NutsWorkspace ws = TestUtils.openNewTestWorkspace(
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions").getWorkspace();
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
