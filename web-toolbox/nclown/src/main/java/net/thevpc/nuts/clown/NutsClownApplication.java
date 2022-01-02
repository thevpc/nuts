package net.thevpc.nuts.clown;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspaceListManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NutsClownApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext cc=SpringApplication.run(NutsClownApplication.class, args);
    }

    @Bean
    NutsSession defaultSession(){
        return Nuts.openWorkspace();
    }

    @Bean
    NutsWorkspaceListManager workspaceList(){
        return NutsWorkspaceListManager.of(defaultSession())
                .setName("clown");
    }
}
