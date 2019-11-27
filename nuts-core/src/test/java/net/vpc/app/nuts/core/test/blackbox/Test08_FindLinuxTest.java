/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.test.blackbox;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
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
        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--archetype", "default",
            "--yes",
            "--skip-companions",});

        NutsDefinition def = ws.search().id("netbeans-launcher#1.1.0")
                .optional(false).inlineDependencies().failFast().online().latest().getResultDefinitions().required();
        System.out.println(def);
    }

    @Test
    public void find4() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--archetype", "default",
            "--yes",
            "--skip-companions",});

        NutsSearchResult<NutsId> resultIds = ws.search().session(ws.createSession().silent()).id("net.vpc.scholar.doovos.kernel:doovos-kernel-core")
                .latest().inlineDependencies().getResultIds();
        System.out.println(resultIds.list());
    }

    @BeforeClass
    public static void setUpClass() throws IOException {
        baseFolder = new File("./runtime/test/" + TestUtils.getCallerClassSimpleName()).getCanonicalFile().getPath();
        CoreIOUtils.delete(null,new File(baseFolder));
        System.out.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @AfterClass
    public static void tearUpClass() throws IOException {
        CoreIOUtils.delete(null,new File(baseFolder));
    }

    @Before
    public void startup() throws IOException {
        Assume.assumeTrue(Nuts.getPlatformOsFamily()== NutsOsFamily.LINUX);
        TestUtils.unsetNutsSystemProperties();
    }

    @After
    public void cleanup() {
        TestUtils.unsetNutsSystemProperties();
    }

}
