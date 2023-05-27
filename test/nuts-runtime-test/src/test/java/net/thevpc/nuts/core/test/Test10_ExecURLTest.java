/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

import java.util.List;


/**
 *
 * @author thevpc
 */
public class Test10_ExecURLTest {
    static NSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void execURL() {
        TestUtils.println(NVersionFormat.of(session));
        NSearchCommand q = NSearchCommand.of(session)
                .setId("net.thevpc.hl:hadra-build-tool#0.1.0")
                .setRepositoryFilter("maven-central")
                .setLatest(true);
        session.out().println(q.getResultQueryPlan());
        List<NId> nutsIds = q
                .getResultIds()
                .toList();
        TestUtils.println(nutsIds);
        List<NDependencies> allDeps = NSearchCommand.of(session).addId("net.thevpc.hl:hl#0.1.0")
                .setDependencies(true)
                .getResultDependencies().toList();
        for (NDependencies ds : allDeps) {
            for (NDependency d : ds.transitiveWithSource()) {
                TestUtils.println(d);
            }
        }
        TestUtils.println("=============");
        for (NDependencies ds : allDeps) {
            for (NDependencyTreeNode d : ds.transitiveNodes()) {
                printlnNode(d,"");
            }
        }
        String result = NExecCommand.of(session)
                .addWorkspaceOptions(new DefaultNWorkspaceOptionsBuilder()
                        .setBot(true)
                        .setWorkspace(NLocations.of(session).getWorkspaceLocation().resolve("temp-ws").toString())
                )
                //.addExecutorOption("--main-class=Version")
                .addCommand(
                        "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar",
                        "--version"
                ).redirectErrorStream().grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    private void printlnNode(NDependencyTreeNode d, String s) {
        TestUtils.println(s+d.getDependency());
        for (NDependencyTreeNode child : d.getChildren()) {
            printlnNode(child,"  ");
        }
    }


        @Test
    public void testEmbeddedInfo() {
        TestUtils.println(NVersionFormat.of(session));
        String result = NExecCommand.of(session.copy()
                        .setBot(true).json())
                .addCommand("info")
                .grabOutputString()
                .getOutputString();
        session.out().println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    //disabled, unless we find a good executable example jar
    //@Test
    public void execURL2() {
        TestUtils.println(NVersionFormat.of(session));
        String result = NExecCommand.of(session)
                //there are three classes and no main-class, so need to specify the one
                .addExecutorOption("--main-class=Version")
//                .addExecutorOption("--main-class=junit.runner.Version")
                //get the command
                .addCommand(
//                        "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar"
                        "https://search.maven.org/remotecontent?filepath=net/java/sezpoz/demo/app/1.6/app-1.6.jar"
//                "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "--version"
        ).redirectErrorStream().grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    //@Test
    public void testNtf() {
        TestUtils.println(NVersionFormat.of(session));
        String result = NExecCommand.of(session.copy().setBot(true))
                //.addExecutorOption()
                .addCommand("nsh","-c","ls")
                .redirectErrorStream().grabOutputString().setFailFast(true).getOutputString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }



    //@Test
    public void testNtf2() {
        TestUtils.println(NVersionFormat.of(session));
        String result = NExecCommand.of(session.copy()
                        .setBot(true).json())
                .setTarget("ssh://vpc:a@192.168.1.98")
                //.addCommand("ls","-l")
                .addCommand("nuts","info")
                //.setExecutionType(NExecutionType.SYSTEM)
                .grabOutputString()
                .getOutputString();
        session.out().println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    //@Test
    public void testCallSpecialId() {
        TestUtils.println(NVersionFormat.of(session));
        NSession nSession = Nuts.openWorkspace("-y","--verbose");
        String result = NExecCommand.of(nSession.copy()
                        .setBot(true).json())
                .addExecutorOptions("--bot")
                //.setExecutionType(NExecutionType.EMBEDDED)
                .addCommand("com.cts.nuts.enterprise.postgres:pgcli")
                .addCommand("list","-i")
                .grabOutputString()
                .getOutputString();
        nSession.out().println(result);
        Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }


}
