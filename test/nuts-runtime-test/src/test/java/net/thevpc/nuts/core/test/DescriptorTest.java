/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class DescriptorTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--verbose");
    }

    @Test
    public void testSearchDescriptor() {
        NDefinition u = NSearch.of().addId("org.springframework.boot:spring-boot-cli#2.4.1")
                .getResultDefinitions().findFirst().get();
        TestUtils.println(u.descriptor());
        TestUtils.println(u.id()+":"+(u.descriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.id()+":"+(u.descriptor().isNutsApplication() ? "app" : "non-app"));
        Assertions.assertTrue(!u.descriptor().isExecutable());
        Assertions.assertTrue(!u.descriptor().isNutsApplication());
    }

    @Test
    public void testSearchDescriptor2() {

        NDefinition u = NFetch.of("org.openjfx:javafx-controls#17.0.0.1")
                .getResultDefinition();
        for (NDependency dependency : u.descriptor().getDependencies()) {
            System.out.println(dependency.toString());
        }
        TestUtils.println(u.descriptor());
        TestUtils.println(u.effectiveDescriptor().get());
        TestUtils.println(u.id()+":"+(u.descriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.id()+":"+(u.descriptor().isNutsApplication() ? "app" : "non-app"));
        Assertions.assertTrue(!u.descriptor().isExecutable());
        Assertions.assertTrue(!u.descriptor().isNutsApplication());
    }

    @Test
    public void testSearchDescriptor3() {
        NDefinition u = NFetch.of("ch.qos.logback:logback-classic#1.2.11")
                .getResultDefinition();
        for (NDependency dependency : u.descriptor().getDependencies()) {
            System.out.println(dependency.toString());
        }
        TestUtils.println(u.descriptor());
        TestUtils.println(u.effectiveDescriptor().get());
        TestUtils.println(u.id()+":"+(u.descriptor().isExecutable() ? "executable" : "non-executable"));
        TestUtils.println(u.id()+":"+(u.descriptor().isNutsApplication() ? "app" : "non-app"));
        Assertions.assertTrue(!u.descriptor().isExecutable());
        Assertions.assertTrue(!u.descriptor().isNutsApplication());
    }

}
