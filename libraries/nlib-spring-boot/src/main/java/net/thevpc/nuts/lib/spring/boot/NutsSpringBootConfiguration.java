package net.thevpc.nuts.lib.spring.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(Nuts.class)
public class NutsSpringBootConfiguration {
    @Autowired
    private ApplicationContext sac;
    @Autowired
    private Environment env;

    @Bean
    public NSessionTerminal nutsSessionTerminal(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NPrintStream nutsOut(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).out();
    }

    @Bean
    public NWorkspace nutsWorkspace(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getWorkspace();
    }

    @Bean
    public NSession nutsSession(ApplicationArguments applicationArguments) {
        return Nuts.openInheritedWorkspace(NCmdLine.parseDefault(env.getProperty("nuts.args")).get().toStringArray(), applicationArguments.getSourceArgs());
    }

    @Bean
    public NApplication nutsApplication() {
        Map<String, Object> bootApps = new HashMap<>();
        for (Map.Entry<String, Object> e : sac.getBeansWithAnnotation(SpringBootApplication.class).entrySet()) {
            Object o = e.getValue();
            if (o instanceof NApplication) {
                return (NApplication) o;
            } else {
                bootApps.put(e.getKey(), o);
            }
        }
        if (bootApps.isEmpty()) {
            throw new IllegalArgumentException("nuts application not found. missing bean with @SpringBootApplication");
        } else if (bootApps.size() == 1) {
            throw new IllegalArgumentException("nuts application not found : found the following bean but it does not implement NApplication interface : " + bootApps.keySet().toArray()[0]);
        } else {
            throw new IllegalArgumentException("nuts application not found : found the following beans but they do not implement NApplication interface : " + bootApps.keySet());
        }
    }


    @Bean
    public CommandLineRunner nutsCommandLineRunner(ApplicationArguments applicationArguments) {
        return args -> NApplications.runApplication(nutsApplication(),
                NCmdLine.parseDefault(env.getProperty("nuts.args")).get().toStringArray(),
                applicationArguments.getSourceArgs()
                );
    }

}
