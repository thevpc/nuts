package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class TestFetch {

    @Test
    public void test(){
        NutsSession session = TestUtils.openNewTestWorkspace();
        NutsDefinition resultDefinition = session.fetch().setId("org.springframework.boot:spring-boot#2.4.1")
                .setDependencies(true)
                .setContent(true)
                .getResultDefinition();
        System.out.println("-----------------");
        for (NutsDependency dependency : resultDefinition.getDescriptor().getDependencies()) {
            System.out.println(dependency);
        }
        System.out.println("-----------------");
        for (NutsDependency dependency : resultDefinition.getDependencies()) {
            System.out.println(dependency);
        }
        System.out.println("-----------------");
        show(resultDefinition.getDependencies().nodes().toArray(new NutsDependencyTreeNode[0]), "");
    }

    @Test
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
        System.out.println(prefix+n.getDependency()+(n.isPartial()?" (partial)":""));
        for (NutsDependencyTreeNode child : n.getChildren()) {
            show(child,prefix+"    ");
        }
    }
}
