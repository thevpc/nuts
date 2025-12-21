/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.text.NVersionFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NChronometer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;


/**
 * @author thevpc
 */
public class SearchTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace(
                "--verbose","--color"
        );
    }

    @Test
    public void find1() {
        TestUtils.println(NVersionFormat.of());
        NSearch q = NSearch.of()
                .setId("org.eclipse.jetty.orbit:javax.mail.glassfish#1.4.1.v201005082020")
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                .setLatest(true);
        for (NDefinition d : q.getResultDefinitions().toList()) {
            NPath c = d.getContent().orNull();
            NOut.println(c);
        }

    }

//    @Test
    public void find2() {
        TestUtils.println(NVersionFormat.of());
        NSearch q = NSearch.of()
                .setId("org.eclipse.jetty:jetty-home#9.4.44.v20210927\n")
//                .setInlineDependencies(true)
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                .setLatest(true);
        List<NDefinition> list = q.getResultDefinitions().toList();
        for (NDefinition d : list) {
            NPath c = d.getContent().orNull();
            for (NDependency nDependency : d.getDependencies().get().toList()) {
                NOut.println(nDependency);
            }
        }

    }

    @Test
    public void find3() {
        TestUtils.println(NVersionFormat.of());
        NSearch q = NSearch.of()
                .setId("net.thevpc.nuts:nuts-ssh")
//                .setInlineDependencies(true)
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                ;
        NWorkspace ws = NWorkspace.of();
        List<NRepository> repositories = ws.getRepositories();
        for (NRepository repository : repositories) {
            TestUtils.println(repository);
        }
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("toolbox"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("system"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("maven"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("nuts-public"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("dev"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("preview"));
//        q.setRepositoryFilter(NRepositoryFilters.of().byName("local"));
        NOut.println(q.getResultQueryPlan());
        NChronometer cr = NChronometer.startNow();
        for (NDefinition d : q.getResultDefinitions().toList()) {
            NPath c = d.getContent().orNull();
                NOut.println(c);
//            for (NDependency nDependency : d.getDependencies().get().toList()) {
//                NOut.println(nDependency);
//            }
        }
        cr.stop();
        NOut.println(cr);

    }



}
