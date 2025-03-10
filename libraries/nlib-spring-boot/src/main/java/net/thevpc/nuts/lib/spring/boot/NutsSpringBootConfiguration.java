package net.thevpc.nuts.lib.spring.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.concurrent.NScheduler;

import net.thevpc.nuts.expr.NExprs;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.time.NProgressMonitors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.*;

@Configuration
@ConditionalOnClass(Nuts.class)
public class NutsSpringBootConfiguration {
    @Autowired
    private ApplicationContext sac;
    @Autowired
    private Environment env;

    @Bean
    public NTerminal nTerminal(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NPrintStream nOut(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).out();
    }

    @Bean
    public NWorkspace nutsWorkspace(ApplicationArguments applicationArguments) {
        return Nuts.openInheritedWorkspace(resolveNutsArgs(), applicationArguments.getSourceArgs());
    }

    @Bean
    public NSession nutsSession(ApplicationArguments applicationArguments) {
        return nutsWorkspace(applicationArguments).currentSession();
    }


    @Bean
    public NTerminal nutsTerminal(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NExprs nutsNExprs(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NExprs.of();
        });
    }
    @Bean
    public NIdFilters nutsIdFilters(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NIdFilters.of();
        });
    }

    @Bean
    public NDependencyFilters nutsDependencyFilters(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NDependencyFilters.of();
        });
    }

    @Bean
    public NDescriptorFilters nutsDescriptorFilters(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NDescriptorFilters.of();
        });
    }
    @Bean
    public NLibPaths nutsLibPaths(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NLibPaths.of();
        });
    }


    @Bean
    public NProgressMonitors nutsProgressMonitors(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NProgressMonitors.of();
        });
    }
    @Bean
    public NIO nutsIO(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NIO.of();
        });
    }
    @Bean
    public NFormats nutsFormats(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NFormats.of();
        });
    }

    @Bean
    public NExtensions nutsExtensions(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NExtensions.of();
        });
    }

    @Bean
    public NScheduler nutsScheduler(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NScheduler.of();
        });
    }

    @Bean
    public NCmdLines nutsCmdLines(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(()->{
            return NCmdLines.of();
        });
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
        return args -> NApplications.runApplication(
                new NMainArgs()
                        .setApplicationInstance(nutsApplication())
                        .setNutsArgs(resolveNutsArgs())
                        .setArgs(applicationArguments.getSourceArgs())
                        .setHandleMode(NApplicationHandleMode.PROPAGATE))
        ;
    }

    private String[] resolveNutsArgs(){
        List<String> args = NCmdLine.parseDefault(env.getProperty("nuts.args")).get().toStringList();
        //always enable main instance in spring apps
        args.add("--shared-instance=true");
        return args.toArray(new String[0]);
    }

}
