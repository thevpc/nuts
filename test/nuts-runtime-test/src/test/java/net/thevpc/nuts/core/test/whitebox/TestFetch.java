package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import org.junit.jupiter.api.Test;

public class TestFetch {

    @Test
    public void test(){
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsDefinition resultDefinition = ws.fetch().setId("org.springframework.boot:spring-boot#2.4.1")
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
        Nuts.runWorkspace("-w","./temp","-Zyk"
                ,"--repository=maven+https://repo.spring.io/release"
                ,"--exclude-repository=maven-local"
                ,"org.springframework.boot:spring-boot#2.4.1");
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
