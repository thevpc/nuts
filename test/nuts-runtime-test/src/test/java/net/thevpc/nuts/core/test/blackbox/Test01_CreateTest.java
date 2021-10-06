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
        Assertions.assertEquals(new File(wsPath, "cache").getPath(), session.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(0,session.repos().getRepositories().length);
//        Assertions.assertEquals(new File(wsPath,  "cache/" + NutsConstants.Folders.REPOSITORIES + "/" +
//                        session.repos().getRepositories()[0].getName() +
//                        "/" + session.repos().getRepositories()[0].getUuid()).getPath(),
//                session.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));

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
        String str =
                "a\n\nb";
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

        NutsSession session = TestUtils.openNewTestWorkspace(
                "--exploded",
                "--archetype", "minimal",
                "--verbose",
                "--skip-companions");
        String base = "";
        switch (NutsOsFamily.getCurrent()) {
            case WINDOWS: {
                base =new File(System.getProperty("user.home"),"AppData\\Local\\nuts\\cache").getPath();
                break;
            }
            case MACOS:
            case LINUX:
            case UNIX:
            case UNKNOWN:
            {
                base =new File(System.getProperty("user.home"),".cache/nuts").getPath();
                break;
            }
        }
        Assertions.assertEquals(new File(base, new File(wsPath).getName()).getPath(),
                session.locations().getStoreLocation(NutsStoreLocation.CACHE));
        Assertions.assertEquals(
                new File(base, new File(wsPath).getName() + "/"
                        + NutsConstants.Folders.REPOSITORIES + "/"
                        + session.repos().getRepositories()[0].getName()
                        + "/" + session.repos().getRepositories()[0].getUuid()
                ).getPath(),
                session.repos().getRepositories()[0].config().getStoreLocation(NutsStoreLocation.CACHE));
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
        TestUtils.println("####### RUNNING TEST @ " + TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
