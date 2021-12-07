package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class Test09_TestFetchTest {

    @Test
    public void test(){
        NutsSession session = TestUtils.openNewTestWorkspace();
        NutsDefinition resultDefinition = session.fetch().setId("org.springframework.boot:spring-boot#2.4.1")
                .setDependencies(true)
                .setContent(true)
                .getResultDefinition();
        TestUtils.println("-----------------");
        for (NutsDependency dependency : resultDefinition.getDescriptor().getDependencies()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        for (NutsDependency dependency : resultDefinition.getDependencies()) {
            TestUtils.println(dependency);
        }
        TestUtils.println("-----------------");
        show(resultDefinition.getDependencies().transitiveNodes().toArray(NutsDependencyTreeNode[]::new), "");
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

    public void show(NutsDependencyTreeNode[] n,String prefix){
        for (NutsDependencyTreeNode nutsDependencyTreeNode : n) {
            show(nutsDependencyTreeNode,prefix);
        }
    }

    public void show(NutsDependencyTreeNode n,String prefix){
        TestUtils.println(prefix+n.getDependency()+(n.isPartial()?" (partial)":""));
        for (NutsDependencyTreeNode child : n.getChildren()) {
            show(child,prefix+"    ");
        }
    }
}
