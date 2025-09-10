/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.io.NPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 * @author thevpc
 */
public class SearchTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--verbose");
    }

    @Test
    public void find1() {
        TestUtils.println(NVersionFormat.of());
        NSearchCmd q = NSearchCmd.of()
                .setId("org.eclipse.jetty.orbit:javax.mail.glassfish#1.4.1.v201005082020")
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                .setLatest(true);
        for (NDefinition d : q.getResultDefinitions().toList()) {
            NPath c = d.getContent().orNull();
        }

    }

    @Test
    public void find2() {
        TestUtils.println(NVersionFormat.of());
        NSearchCmd q = NSearchCmd.of()
                .setId("org.eclipse.jetty:jetty-home#9.4.44.v20210927\n")
                .setInlineDependencies(true)
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                .setLatest(true);
        for (NDefinition d : q.getResultDefinitions().toList()) {
            NPath c = d.getContent().orNull();
            for (NDependency nDependency : d.getDependencies().get().toList()) {
                System.out.println(nDependency);
            }
        }

    }



}
