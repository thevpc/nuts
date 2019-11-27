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
import java.util.List;
import java.util.Map;

import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vpc
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
            NutsWorkspace ws = Nuts.openWorkspace(new String[]{
                    "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
                    "--archetype", "minimal",
                    "--yes",
                    //            "--skip-companions",
                    "--verbose"
            });
        }catch (NutsInvalidWorkspaceException ex){
            Assert.fail();
        }catch (NutsNotFoundException ex){
            Assert.fail();
        }
    }

    @Test()
    public void find2() throws Exception {
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
        TestUtils.setSystemProperties(extraProperties);

        //should throw NutsNotFoundException because
        //would not be able to installe nsh and other companions
        NutsWorkspace ws = Nuts.openWorkspace(new String[]{
            "--workspace", baseFolder + "/" + TestUtils.getCallerMethodName(),
            "--archetype", "minimal",
            "--yes",
            "--skip-companions", //            "--verbose"
        });

        NutsSearchResult<NutsId> result = ws.search().remote().latest().id(NutsConstants.Ids.NUTS_API).getResultIds();
        //There is no result because only "local" repository is registered (minimal archetype)
        Assert.assertEquals(0, result.count());
    }

    @Test()
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

        int count = 0;
        NutsSearchResult<NutsId> result = ws.search().remote().latest().id(NutsConstants.Ids.NUTS_API).getResultIds();
        Assert.assertTrue(result.count() > 0);
    }

    @Test()
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

        List<NutsId> result1 = ws.search().latest().id("nuts-core").getResultIds().list();
        List<NutsId> result2 = ws.search().latest(false).id("nuts-core").getResultIds().list();
        System.out.println(result1);
        System.out.println(result2);
        Assert.assertTrue(result1.size() > 0);
    }

    @Test()
    public void find5() throws Exception {
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

        List<NutsId> result1 = ws.search().configure(false, "nuts-core").getResultIds().list();
        List<NutsId> result2 = ws.search().configure(false, "--latest", "nuts-core").getResultIds().list();
        System.out.println("=====================");
        System.out.println(result1);
        System.out.println("=====================");
        System.out.println(result2);
        System.out.println("=====================");
        Assert.assertTrue(result1.size() > 0);
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
