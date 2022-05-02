package net.thevpc.nuts.lib.spring.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSessionTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NutsSpringBootConfig {
    @Autowired
    private ApplicationContext sac;

    @Bean
    public NutsSessionTerminal nutsSessionTerminal(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NutsPrintStream nutsOut(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).out();
    }

    @Bean
    public NutsWorkspace nutsWorkspace(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getWorkspace();
    }

    @Bean
    public NutsSession nutsSession(ApplicationArguments applicationArguments) {
        return nutsAppContext(applicationArguments).getSession();
    }

    @Bean
    public NutsApplication nutsApplication() {
        for (Object o : sac.getBeansWithAnnotation(SpringBootApplication.class).values()) {
            if (o instanceof NutsApplication) {
                return (NutsApplication) o;
            }
        }
        throw new IllegalArgumentException("Nuts application not found");
    }

    @Bean
    NutsApplicationContext nutsAppContext(ApplicationArguments applicationArguments) {
        return NutsApplications.createApplicationContext(nutsApplication(),
                applicationArguments.getSourceArgs(),
                null
        );
    }

    @Bean
    public CommandLineRunner nutsCommandLineRunner(ApplicationContext ctx, ApplicationArguments applicationArguments) {
        return args -> NutsApplications.runApplication(nutsApplication(), nutsAppContext(applicationArguments));
    }

}
