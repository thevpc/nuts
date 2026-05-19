package net.thevpc.nuts.core.test;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyTreeNode;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class FetchTest {

    @Test
    public void test(){
        TestUtils.openNewTestWorkspace();
        NDefinition resultDefinition = NFetch.of("org.springframework.boot:spring-boot#2.4.1")
                .getResultDefinition();
        TestUtils.println("-----------------");
        for (NDependency dependency : resultDefinition.descriptor().dependencies()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        for (NDependency dependency : resultDefinition.dependencies().get()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        show(resultDefinition.dependencies().get().transitiveNodes().toArray(NDependencyTreeNode[]::new), "");
    }

    // disable test because, for some reason it fails on Gitlab CI with
    // unable to install org.springframework.boot:spring-boot-cli#2.4.1. required dependency content is missing for org.codehaus.plexus:plexus-interpolation#1.25
    //@Test
    public void test2(){
        TestUtils.runNewTestWorkspace("--verbose",
                "--repositories=spring"
//                ,"org.springframework.boot:spring-boot#2.4.1"
                ,"exec"
                ,"--main-class=1" //multiple main classes are solved, the first is used
                ,"org.springframework.boot:spring-boot-cli#2.4.1"
                ,"--version"
        );
    }

    public void show(NDependencyTreeNode[] n, String prefix){
        for (NDependencyTreeNode nDependencyTreeNode : n) {
            show(nDependencyTreeNode,prefix);
        }
    }

    public void show(NDependencyTreeNode n, String prefix){
        TestUtils.println(prefix+n.dependency()+(n.isPartial()?" (partial)":""));
        for (NDependencyTreeNode child : n.children()) {
            show(child,prefix+"    ");
        }
    }
}
