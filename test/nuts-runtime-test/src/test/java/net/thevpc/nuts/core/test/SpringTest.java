package net.thevpc.nuts.core.test;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.reflect.NPlatformSignature;
import net.thevpc.nuts.reflect.NSignatureMap;
import net.thevpc.nuts.runtime.standalone.collections.NEvictingCharQueueImpl;
import net.thevpc.nuts.runtime.standalone.reflect.NPlatformSignatureImpl;
import net.thevpc.nuts.runtime.standalone.reflect.NSignatureMapImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;

public class SpringTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--verbose");
    }

    @Test
    public void testRunSpring() {
        net.thevpc.nuts.artifact.NDefinition def = net.thevpc.nuts.command.NFetch.of("net.thevpc.samples.springnuts:springnuts-app-jar#1.0.0")
                .failFast(true)
                .getResultDefinition();
        System.out.println("DEF ID: " + def.id());
        System.out.println("STANDARD DEPS COUNT: " + def.effectiveDescriptor().get().standardDependencies().size());
        for (net.thevpc.nuts.artifact.NDependency sdep : def.effectiveDescriptor().get().standardDependencies()) {
            if (sdep.artifactId().contains("jackson")) {
                System.out.println("  STD DEP: " + sdep);
            }
        }
        if (def.dependencies().isPresent()) {
            net.thevpc.nuts.artifact.NDependencies deps = def.dependencies().get();
            System.out.println("--- RESOLVED DEPENDENCIES TRANSITIVE WITH SOURCE ---");
            for (net.thevpc.nuts.artifact.NDependency dep : deps.transitiveWithSource().toList()) {
                System.out.println("  " + dep);
            }
            System.out.println("--- RESOLVED DEPENDENCIES NODES ---");
            for (net.thevpc.nuts.artifact.NDependencyTreeNode node : deps.transitiveNodes().toList()) {
                printNode(node, 0);
            }
        }
        net.thevpc.nuts.io.NPath path = def.content().orNull();
        System.out.println("DEF CONTENT PATH: " + path);
        if (path != null) {
            java.util.List<net.thevpc.nuts.command.NExecutionEntry> entries = net.thevpc.nuts.command.NExecutionEntry.parse(path);
            System.out.println("EXECUTION ENTRIES COUNT: " + entries.size());
            for (net.thevpc.nuts.command.NExecutionEntry e : entries) {
                System.out.println("  ENTRY: name=" + e.name() + ", default=" + e.isDefaultEntry() + ", app=" + e.isApp());
            }
        }

        NExec exec = NExec.of("net.thevpc.samples.springnuts:springnuts-app-jar")
//                .embedded()
                .executorOption("--show-command")
                .spawn()
                .out(net.thevpc.nuts.io.NExecOutput.ofInherit())
                .err(net.thevpc.nuts.io.NExecOutput.ofInherit())
                .failFast(false)
                .run();
        Assertions.assertEquals(0, exec.exitCode(), "Process failed with exit code " + exec.exitCode());
    }

    private void printNode(net.thevpc.nuts.artifact.NDependencyTreeNode node, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) sb.append("  ");
        sb.append(node.dependency());
        if (node.isOptional()) sb.append(" [optional]");
        if (node.isProvided()) sb.append(" [provided]");
        System.out.println(sb.toString());
        for (net.thevpc.nuts.artifact.NDependencyTreeNode child : node.children()) {
            printNode(child, indent + 1);
        }
    }
}
