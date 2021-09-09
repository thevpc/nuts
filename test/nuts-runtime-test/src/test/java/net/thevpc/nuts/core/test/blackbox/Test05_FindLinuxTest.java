/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test05_FindLinuxTest {

    private static String baseFolder;

    @Test
    public void find1() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        try {
            NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                    "--archetype", "minimal",
                    //            "--skip-companions",
                    "--verbose").getWorkspace();
        }catch (NutsInvalidWorkspaceException ex){
            Assertions.fail();
        }catch (NutsNotFoundException ex){
            Assertions.fail();
        }
    }

    @Test()
    public void find2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "minimal",
                "--skip-companions" //            "--verbose"
        ).getWorkspace();

        NutsResultList<NutsId> result = ws.search()
                .setSession(ws.createSession().setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        //There is one result because nuts id is always installed
        Assertions.assertEquals(1, result.count());
    }

    @Test()
    public void find3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--skip-companions").getWorkspace();

        int count = 0;
        NutsResultList<NutsId> result = ws.search()
                .setSession(ws.createSession().setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        Assertions.assertTrue(result.count() > 0);
    }

    @Test()
    public void find4() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--skip-companions").getWorkspace();

        ws = ws.createSession().getWorkspace();
        List<NutsId> result1 = ws.search().setLatest(true).addId("nuts-runtime").getResultIds().list();
        List<NutsId> result2 = ws.search().setLatest(false).addId("nuts-runtime").getResultIds().list();
        TestUtils.println(result1);
        TestUtils.println(result2);
        Assertions.assertTrue(result1.size() > 0);
    }

    @Test()
    public void find5() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--skip-companions").getWorkspace();
        ws = ws.createSession().getWorkspace();

        List<NutsId> result1 = ws.search().configure(false, "nuts-runtime").getResultIds().list();
        List<NutsId> result2 = ws.search().configure(false, "--latest", "nuts-runtime").getResultIds().list();
        TestUtils.println("=====================");
        TestUtils.println(result1);
        TestUtils.println("=====================");
        TestUtils.println(result2);
        TestUtils.println("=====================");
        Assertions.assertTrue(result1.size() > 0);
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null,new File(baseFolder));
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
