package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class FetchTest {

    @Test
    public void test(){
        TestUtils.openNewTestWorkspace();
        NDefinition resultDefinition = NFetchCmd.of("org.springframework.boot:spring-boot#2.4.1")
                .getResultDefinition();
        TestUtils.println("-----------------");
        for (NDependency dependency : resultDefinition.getDescriptor().getDependencies()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        for (NDependency dependency : resultDefinition.getDependencies().get()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        show(resultDefinition.getDependencies().get().transitiveNodes().toArray(NDependencyTreeNode[]::new), "");
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
        TestUtils.println(prefix+n.getDependency()+(n.isPartial()?" (partial)":""));
        for (NDependencyTreeNode child : n.getChildren()) {
            show(child,prefix+"    ");
        }
    }
}
