/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.util.List;

import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test05_FindTest {

    @Test
    public void find1() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession ws = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");
        List<NutsId> def = ws.search().addId("nuts").setOptional(false).setLatest(true).setFailFast(false)
//                .repository("maven-local")
                .setDefaultVersions(true)
                .setInstallStatus(NutsInstallStatusFilters.of(ws).byDeployed(true))
                .getResultIds().toList();

        TestUtils.println(def);
    }

    @Test()
    public void find2() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
//                "--archetype", "minimal",
                "--skip-companions" //            "--verbose"
        );

        NutsStream<NutsId> result = s.search()
                .setSession(s.setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        //There is one result because nuts id is always installed
        Assertions.assertEquals(1, result.count());
    }

    @Test()
    public void find3() throws Exception {
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        NutsStream<NutsId> result = s.search()
                .setSession(s.setFetchStrategy(NutsFetchStrategy.REMOTE))
                .setLatest(true).addId(NutsConstants.Ids.NUTS_API).getResultIds();
        Assertions.assertTrue(result.count() > 0);
    }

    @Test()
    public void find4() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        List<NutsId> result1 = s.search().setLatest(true).addId("nuts-runtime").getResultIds().toList();
        List<NutsId> result2 = s.search().setLatest(false).addId("nuts-runtime").getResultIds().toList();
        TestUtils.println(result1);
        TestUtils.println(result2);
        Assertions.assertTrue(result1.size() > 0);
    }

    @Test()
    public void find5() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        List<NutsId> result1 = s.search().configure(false, "nuts-runtime").getResultIds().toList();
        List<NutsId> result2 = s.search().configure(false, "--latest", "nuts-runtime").getResultIds().toList();
        TestUtils.println("=====================");
        TestUtils.println(result1);
        TestUtils.println("=====================");
        TestUtils.println(result2);
        TestUtils.println("=====================");
        Assertions.assertTrue(result1.size() > 0);
    }


    @Test
    public void find6() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession session = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        NutsDefinition def = session.search().addId(
                        "net.thevpc.common:thevpc-common-io#1.3.12"
//                "netbeans-launcher#1.1.0"
                )
                .setOptional(false).setInlineDependencies(true).setFailFast(true)
                .setSession(session.copy().setFetchStrategy(NutsFetchStrategy.ONLINE))
                .setLatest(true).getResultDefinitions().required();
        TestUtils.println(def);
    }

    @Test
    public void find7() throws Exception {
        //should throw NutsNotFoundException because
        //would not be able to install nsh and other companions
        NutsSession s = TestUtils.openNewTestWorkspace(
                "--archetype", "default",
                "--skip-companions");

        NutsStream<NutsId> resultIds = s.search().setSession(s).addId("net.thevpc.scholar.doovos.kernel:doovos-kernel-core")
                .setLatest(true).setInlineDependencies(true).getResultIds();
        TestUtils.println(resultIds.toList());
    }



}
