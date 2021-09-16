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
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test08_FindLinuxTest {

    private static String baseFolder;

    @Test
    public void find3() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--skip-companions").getWorkspace();

        NutsSession session = ws.createSession();
        NutsDefinition def = session.getWorkspace().search().addId(
                "net.thevpc.common:thevpc-common-io#1.3.12"
//                "netbeans-launcher#1.1.0"
                )
                .setOptional(false).setInlineDependencies(true).setFailFast(true)
                .setSession(ws.createSession().setFetchStrategy(NutsFetchStrategy.ONLINE))
                .setLatest(true).getResultDefinitions().required();
        TestUtils.println(def);
    }

    @Test
    public void find4() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = TestUtils.openTestWorkspace("--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                "--archetype", "default",
                "--skip-companions").getWorkspace();

        NutsStream<NutsId> resultIds = ws.search().setSession(ws.createSession()).addId("net.thevpc.scholar.doovos.kernel:doovos-kernel-core")
                .setLatest(true).setInlineDependencies(true).getResultIds();
        TestUtils.println(resultIds.toList());
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
