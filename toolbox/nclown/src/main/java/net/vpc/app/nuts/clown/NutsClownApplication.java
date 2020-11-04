package net.thevpc.nuts.clown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class NutsClownApplication {

    public static void main(String[] args) {
//        Nuts.openWorkspace(new NutsWorkspaceOptions()
//            .setOpenMode(NutsWorkspaceOpenMode.OPEN_OR_CREATE)
////            .setReadOnly(false)
//            .setSkipPostCreateInstallCompanionTools(true)
//        );

        ConfigurableApplicationContext cc=SpringApplication.run(NutsClownApplication.class, args);
        for (String beanDefinitionName : cc.getBeanDefinitionNames()) {
            System.out.println(beanDefinitionName);
        }
    }
}
