package net.vpc.app.nuts.clown;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspaceOpenMode;
import net.vpc.app.nuts.NutsWorkspaceOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NutsClownApplication {

    public static void main(String[] args) {
        Nuts.openWorkspace(new NutsWorkspaceOptions()
            .setOpenMode(NutsWorkspaceOpenMode.DEFAULT)
//            .setReadOnly(false)
            .setSkipPostCreateInstallCompanionTools(true)
        );

        SpringApplication.run(NutsClownApplication.class, args);
    }
}
