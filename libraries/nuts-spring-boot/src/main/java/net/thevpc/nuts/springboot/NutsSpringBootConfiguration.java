package net.thevpc.nuts.springboot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.reserved.cmdline.NBootCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.concurrent.NScheduler;

import net.thevpc.nuts.expr.NExprs;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.time.NClock;
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
    public NTerminal nTerminal(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NPrintStream nOut(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).out();
    }


    @Bean
    public NSession nutsSession(@Autowired ApplicationArguments applicationArguments) {
        return nutsWorkspace(applicationArguments).currentSession();
    }


    @Bean
    public NTerminal nutsTerminal(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).getTerminal();
    }

    @Bean
    public NExprs nutsNExprs(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NExprs.of();
        });
    }

    @Bean
    public NIdFilters nutsIdFilters(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NIdFilters.of();
        });
    }

    @Bean
    public NDependencyFilters nutsDependencyFilters(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NDependencyFilters.of();
        });
    }

    @Bean
    public NDefinitionFilters nutsDefinitionFilters(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NDefinitionFilters.of();
        });
    }

    @Bean
    public NLibPaths nutsLibPaths(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NLibPaths.of();
        });
    }


    @Bean
    public NProgressMonitors nutsProgressMonitors(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NProgressMonitors.of();
        });
    }

    @Bean
    public NIO nutsIO(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NIO.of();
        });
    }

    @Bean
    public NFormats nutsFormats(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NFormats.of();
        });
    }

    @Bean
    public NExtensions nutsExtensions(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NExtensions.of();
        });
    }

    @Bean
    public NScheduler nutsScheduler(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NScheduler.of();
        });
    }

    @Bean
    public NCmdLines nutsCmdLines(ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NCmdLines.of();
        });
    }

    private Object resolveValidSpringBootApplication(NWorkspace workspace, ApplicationArguments applicationArguments) {
        Map<String, Object> bootApps = new HashMap<>();
        for (Map.Entry<String, Object> e : sac.getBeansWithAnnotation(SpringBootApplication.class).entrySet()) {
            Object o = e.getValue();
            if (o instanceof NApplication) {
                return o;
            } else if (NApplications.isAnnotatedApplicationClass(o.getClass())) {
                return o;
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
    public NApplication nutsApplication(@Autowired NWorkspace workspace, @Autowired ApplicationArguments applicationArguments) {
        NClock now = NClock.now();
        NApplication validApp = null;
        Object validAppBean = resolveValidSpringBootApplication(workspace, applicationArguments);
        if (validAppBean instanceof NApplication) {
            validApp = (NApplication) validAppBean;
        } else {
            validApp = NApplications.createApplicationInstanceFromAnnotatedInstance(validAppBean);
        }
//        Object finalValidAppBean = validAppBean;
//        workspace.runWith(() -> {
//            NApp a = NApp.of();
//            a.setArguments(applicationArguments.getSourceArgs());
//            a.prepare(new NAppInitInfo(applicationArguments.getSourceArgs(), NApplications.unproxyType(finalValidAppBean.getClass()), now));
//        });
        return validApp;
    }


    @Bean
    public NWorkspace nutsWorkspace(@Autowired ApplicationArguments applicationArguments) {
        return Nuts.openWorkspace(
                NBootArguments.of(resolveNutsArgs())
                        .setAppArgs(applicationArguments.getSourceArgs())
        );
    }

    @Bean
    public CommandLineRunner nutsCommandLineRunner(@Autowired NWorkspace workspace, @Autowired ApplicationArguments applicationArguments) {
        return args -> NApp.builder(applicationArguments.getSourceArgs())
                .instance(nutsApplication(workspace, applicationArguments))
                .setNutsArgs(resolveNutsArgs())
                .propagateErrors()
                .run();
    }

    private String[] resolveNutsArgs() {
        List<String> args = new ArrayList<>(Arrays.asList(NBootCmdLine.parseDefault(env.getProperty("nuts.args"))));
        //always enable main instance in spring apps
        args.add("--shared-instance=true");
        return args.toArray(new String[0]);
    }
}
