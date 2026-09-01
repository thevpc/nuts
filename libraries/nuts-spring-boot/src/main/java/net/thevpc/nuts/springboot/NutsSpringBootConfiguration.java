package net.thevpc.nuts.springboot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.app.*;
import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.internal.cmdline.NBootCmdLine;
import net.thevpc.nuts.concurrent.NConcurrent;

import net.thevpc.nuts.concurrent.NScopedStack;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.reflect.NBeanContainer;
import net.thevpc.nuts.reflect.NReflect;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminal;
import net.thevpc.nuts.text.NMsg;
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
    public NBeanContainer nutsBeanContainer(NWorkspace workspace) {
        NutsSpringBeanContainer u = new NutsSpringBeanContainer(sac);
        workspace.runWith(() -> {
            NScopedStack<NBeanContainer> nBeanContainerNScopedValue = NReflect.of().scopedBeanContainerStack();
            nBeanContainerNScopedValue.defaultSupplier(() -> u);
        });
        return u;
    }

    @Bean
    public NTerminal nTerminal(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).terminal();
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
        return nutsSession(applicationArguments).terminal();
    }

    @Bean
    public NIO nutsIO(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NIO.of();
        });
    }

    @Bean
    public NExtensions nutsExtensions(@Autowired ApplicationArguments applicationArguments) {
        return nutsSession(applicationArguments).callWith(() -> {
            return NExtensions.of();
        });
    }

    private Object resolveValidSpringBootApplication(NWorkspace workspace, ApplicationArguments applicationArguments) {
        Map<String, Object> bootApps = new HashMap<>();
        for (Map.Entry<String, Object> e : sac.getBeansWithAnnotation(SpringBootApplication.class).entrySet()) {
            Object o = e.getValue();
            if (o instanceof NApplicationHandler) {
                return o;
            } else if (NApplicationHandler.isAnnotatedApplicationClass(o.getClass())) {
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
    public NApplicationHandler nutsApplication(@Autowired NWorkspace workspace, @Autowired ApplicationArguments applicationArguments) {
        NApplicationHandler validApp = null;
        try {
            Object validAppBean = resolveValidSpringBootApplication(workspace, applicationArguments);
            if (validAppBean instanceof NApplicationHandler) {
                validApp = (NApplicationHandler) validAppBean;
            } else {
                validApp = NApplicationHandler.createApplicationInstanceFromAnnotatedInstance(validAppBean);
            }
        }catch (Exception e) {
            NLog.of(NApplicationHandler.class).info(NMsg.ofC("Error configuring the application : %s",e));
            validApp=new NApplicationHandler() {
                @Override
                public void run() {
                    // do nothing
                }
            };
        }
//        Object finalValidAppBean = validAppBean;
//        workspace.runWith(() -> {
//            NApp a = NApplication.of();
//            a.setArguments(applicationArguments.getSourceArgs());
//            a.prepare(new NAppInitInfo(applicationArguments.getSourceArgs(), NApplications.unproxyType(finalValidAppBean.getClass()), now));
//        });
        return validApp;
    }


    @Bean
    public NWorkspace nutsWorkspace(@Autowired ApplicationArguments applicationArguments) {
        if (SpringNApplicationResolverSPI.globalApplicationContext == null) {
            SpringNApplicationResolverSPI.globalApplicationContext = sac;
        }
        NWorkspace workspace = Nuts.openWorkspace(
                NBootArguments.of(resolveNutsArgs())
                        .appArgs(applicationArguments.getSourceArgs())
        );
        // prepare app early
        NApplication.builder(applicationArguments.getSourceArgs())
                .instance(nutsApplication(workspace, applicationArguments))
                .nutsArgs(resolveNutsArgs())
                .propagateErrors().prepare();
        return workspace;
    }

    @Bean
    public CommandLineRunner nutsCommandLineRunner(@Autowired NWorkspace workspace) {
        return args -> workspace.runApplication(NApplicationHandleMode.HANDLE);
    }

    private String[] resolveNutsArgs() {
        List<String> args = new ArrayList<>(Arrays.asList(NBootCmdLine.parseDefault(env.getProperty("nuts.args"))));
        //always enable main instance in spring apps
        if (args.isEmpty()) {
            args.add("--sandbox");
        }
        args.add("--shared-instance=true");
        args.add("--yes");
        return args.toArray(new String[0]);
    }

}
