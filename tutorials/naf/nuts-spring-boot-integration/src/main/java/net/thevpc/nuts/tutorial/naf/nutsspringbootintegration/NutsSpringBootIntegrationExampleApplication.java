package net.thevpc.nuts.tutorial.naf.nutsspringbootintegration;

import net.thevpc.nuts.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class NutsSpringBootIntegrationExampleApplication implements NutsApplication {
    public static void main(String[] args) {
        SpringApplication.run(NutsSpringBootIntegrationExampleApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx, ApplicationArguments applicationArguments) {
        return args -> NutsApplications.runApplication(this, nutsAppContext(applicationArguments));
    }

    @Bean
    NutsApplicationContext nutsAppContext(ApplicationArguments applicationArguments) {
        return NutsApplications.createApplicationContext(this,
                applicationArguments.getSourceArgs(),
                null
        );
    }

    @Override
    public void onInstallApplication(NutsApplicationContext applicationContext) {
        NutsSession session = applicationContext.getSession();
        session.out().printlnf("write your business logic that will be processed when the application is being installed here...");
    }

    @Override
    public void onUpdateApplication(NutsApplicationContext applicationContext) {
        NutsSession session = applicationContext.getSession();
        session.out().printlnf("write your business logic that will be processed when the application is being updated/upgraded here...");
    }

    @Override
    public void onUninstallApplication(NutsApplicationContext applicationContext) {
        NutsSession session = applicationContext.getSession();
        session.out().printlnf("write your business logic that will be processed when the application is being uninstalled/removed here...");
    }

    /**
     * This method will be called to run you application or to process auto-complete arguments
     *
     * @param context nuts application context
     */
    @Override
    public void run(NutsApplicationContext context) {
        NutsCommandLine cmd = context.getCommandLine();
        NutsArgument a;
        String someStringOption = null;
        Boolean someBooleanOption = null;
        List<String> nonOptions = new ArrayList<>();
        while ((a = cmd.peek()) != null) {
            switch (a.getStringKey()) {
                case "--some-string-option": {
                    // example of calls
                    // your-app --some-string-option=yourValue
                    // your-app --some-string-option yourValue

                    a = cmd.nextString();
                    if (a.isActive()) {
                        someStringOption = a.getStringValue();
                    }
                    break;
                }
                case "--some-boolean-option": {
                    // example of calls
                    // your-app --some-boolean-option=true
                    // your-app --some-boolean-option
                    // your-app --!some-string-option
                    a = cmd.nextBoolean();
                    if (a.isActive()) {
                        someBooleanOption = a.getBooleanValue();
                    }
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        nonOptions.add(cmd.next().getString());
                    } else {
                        // this is an unsupported options!
                        cmd.unexpectedArgument();
                    }
                }
            }
        }
        // this will fire an exception if no option is provided!
        if (someStringOption == null) {
            cmd.requiredOptions("--some-string-option");
        }
        //the application can be run in one of 'execMode' and 'autoCompleteMode' modes
        if (context.isExecMode()) {
            //only run if in execMode
            NutsSession session = context.getSession();
            //just display the options as an example of execution
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("someStringOption", someStringOption);
            result.put("someBooleanOption", someBooleanOption);
            result.put("nonOptions", nonOptions);
            session.out().printlnf(result);
        }
    }


}
