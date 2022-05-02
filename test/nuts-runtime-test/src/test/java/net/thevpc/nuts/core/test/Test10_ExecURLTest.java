/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNutsWorkspaceOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

import java.util.List;


/**
 *
 * @author thevpc
 */
public class Test10_ExecURLTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void execURL() {
        TestUtils.println(NutsVersionFormat.of(session));
        NutsSearchCommand q = session.search()
                .setId("net.thevpc.hl:hadra-build-tool#0.1.0")
                .setRepositoryFilter("maven-central")
                .setLatest(true);
        session.out().printlnf(q.getResultQueryPlan());
        List<NutsId> nutsIds = q
                .getResultIds()
                .toList();
        TestUtils.println(nutsIds);
        List<NutsDependencies> allDeps = session.search().addId("net.thevpc.hl:hl#0.1.0")
                .setDependencies(true)
                .getResultDependencies().toList();
        for (NutsDependencies ds : allDeps) {
            for (NutsDependency d : ds.transitiveWithSource()) {
                TestUtils.println(d);
            }
        }
        TestUtils.println("=============");
        for (NutsDependencies ds : allDeps) {
            for (NutsDependencyTreeNode d : ds.transitiveNodes()) {
                printlnNode(d,"");
            }
        }
        String result = session.exec()
                .addWorkspaceOptions(new DefaultNutsWorkspaceOptionsBuilder()
                        .setBot(true)
                        .setWorkspace(session.locations().getWorkspaceLocation().resolve("temp-ws").toString())
                )
                //.addExecutorOption("--main-class=Version")
                .addCommand(
                        "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar",
                        "--version"
                ).setRedirectErrorStream(true).grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    private void printlnNode(NutsDependencyTreeNode d, String s) {
        TestUtils.println(s+d.getDependency());
        for (NutsDependencyTreeNode child : d.getChildren()) {
            printlnNode(child,"  ");
        }
    }

    //disabled, unless we find a good executable example jar
    //@Test
    public void execURL2() {
        TestUtils.println(NutsVersionFormat.of(session));
        String result = session.exec()
                //there are three classes and no main-class, so need to specify the one
                .addExecutorOption("--main-class=Version")
//                .addExecutorOption("--main-class=junit.runner.Version")
                //get the command
                .addCommand(
//                        "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar"
                        "https://search.maven.org/remotecontent?filepath=net/java/sezpoz/demo/app/1.6/app-1.6.jar"
//                "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "--version"
        ).setRedirectErrorStream(true).grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

}
