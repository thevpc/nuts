/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.util.List;

import net.thevpc.nuts.util.NStream;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test05_FindTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void find1() throws Exception {
        List<NId> def = NSearchCmd.of().addId("nuts").setLatest(true).setFailFast(false)
//                .repository("maven-local")
                .setDefinitionFilter(NDefinitionFilters.of().byDeployed(true).and(NDefinitionFilters.of().byDefaultVersion(true)))
                .getResultIds().toList();

        TestUtils.println(def);
    }

    @Test()
    public void find2() {
        NStream<NId> result = NSearchCmd.of()
                .setLatest(true).addId(NConstants.Ids.NUTS_API).getResultIds();
        //There is one result because nuts id is always installed
        Assertions.assertTrue(result.count()>0);
    }

    @Test()
    public void find3() {
        NStream<NId> result = NSearchCmd.of()
                .setLatest(true).addId(NConstants.Ids.NUTS_API).getResultIds();
        Assertions.assertTrue(result.count() > 0);
    }

    @Test()
    public void find4() throws Exception {

        List<NId> result1 = NSearchCmd.of().setLatest(true).addId("nuts-runtime").getResultIds().toList();
        List<NId> result2 = NSearchCmd.of().setLatest(false).addId("nuts-runtime").getResultIds().toList();
        TestUtils.println(result1);
        TestUtils.println(result2);
        Assertions.assertTrue(result1.size() > 0);
    }

    @Test()
    public void find5() throws Exception {
        List<NId> result1 = NSearchCmd.of().configure(false, "nuts-runtime").getResultIds().toList();
        List<NId> result2 = NSearchCmd.of().configure(false, "--latest", "nuts-runtime").getResultIds().toList();
        TestUtils.println("=====================");
        TestUtils.println(result1);
        TestUtils.println("=====================");
        TestUtils.println(result2);
        TestUtils.println("=====================");
        Assertions.assertTrue(result1.size() > 0);
    }


    @Test
    public void find6() throws Exception {
        NDefinition def = NSearchCmd.of().setFetchStrategy(NFetchStrategy.ONLINE).addId(
                        "net.thevpc.common:thevpc-common-io#1.3.12"
//                "netbeans-launcher#1.1.0"
                )
                .setDependencyFilter(
                        NDependencyFilters.of().byOptional(false)
                )
                .setInlineDependencies(true).failFast()
                .setLatest(true).getResultDefinitions().findFirst().get();
        TestUtils.println(def);
    }

    @Test
    public void find7() throws Exception {
        NStream<NId> resultIds = NSearchCmd.of().addId("net.thevpc.scholar.doovos.kernel:doovos-kernel-core")
                .setLatest(true).setInlineDependencies(true).getResultIds();
        TestUtils.println(resultIds.toList());
    }



}
