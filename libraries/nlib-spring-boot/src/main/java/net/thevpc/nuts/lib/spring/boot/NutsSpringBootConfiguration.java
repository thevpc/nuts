package net.thevpc.nuts.lib.spring.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.io.NutsSessionTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnClass(Nuts.class)
public class NutsSpringBootConfiguration {
    @Autowired
    private ApplicationContext sac;
    @Autowired
    private Environment env;

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
        Map<String, Object> bootApps = new HashMap<>();
        for (Map.Entry<String, Object> e : sac.getBeansWithAnnotation(SpringBootApplication.class).entrySet()) {
            Object o = e.getValue();
            if (o instanceof NutsApplication) {
                return (NutsApplication) o;
            } else {
                bootApps.put(e.getKey(), o);
            }
        }
        if (bootApps.isEmpty()) {
            throw new IllegalArgumentException("nuts application not found. missing bean with @SpringBootApplication");
        } else if (bootApps.size() == 1) {
            throw new IllegalArgumentException("nuts application not found : found the following bean but it does not implement NutsApplication interface : " + bootApps.keySet().toArray()[0]);
        } else {
            throw new IllegalArgumentException("nuts application not found : found the following beans but they do not implement NutsApplication interface : " + bootApps.keySet());
        }
    }

    @Bean
    NutsApplicationContext nutsAppContext(ApplicationArguments applicationArguments) {
        return NutsApplications.createApplicationContext(nutsApplication(),
                NutsCommandLine.parseDefault(env.getProperty("nuts.args")).get().toStringArray(),
                applicationArguments.getSourceArgs(),
                null
        );
    }

    @Bean
    public CommandLineRunner nutsCommandLineRunner(ApplicationContext ctx, ApplicationArguments applicationArguments) {
        return args -> NutsApplications.runApplication(nutsApplication(), nutsAppContext(applicationArguments));
    }

}
