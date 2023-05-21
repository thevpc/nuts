package net.thevpc.nuts.tutorial.naf.spring.installevents;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AppExample implements NApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppExample.class, args);
    }

    @Override
    public void onInstallApplication(NSession session) {
        session.out().println("write your business logic that will be processed when the application is being installed here...");
    }

    @Override
    public void onUpdateApplication(NSession session) {
        session.out().println("write your business logic that will be processed when the application is being updated/upgraded here...");
    }

    @Override
    public void onUninstallApplication(NSession session) {
        session.out().println("write your business logic that will be processed when the application is being uninstalled/removed here...");
    }

    /**
     * This method will be called to run you application or to process auto-complete arguments
     *
     * @param session nuts application context
     */
    @Override
    public void run(NSession session) {
        NCmdLine cmd = session.getAppCommandLine();
        NArg a;
        String someStringOption = null;
        Boolean someBooleanOption = null;
        List<String> nonOptions = new ArrayList<>();
        while ((a = cmd.peek().orNull()) != null) {
            switch (a.key()) {
                case "--some-string-option": {
                    // example of calls
                    // your-app --some-string-option=yourValue
                    // your-app --some-string-option yourValue

                    a = cmd.nextEntry().get(session);
                    if (a.isActive()) {
                        someStringOption = a.getStringValue().get(session);
                    }
                    break;
                }
                case "--some-boolean-option": {
                    // example of calls
                    // your-app --some-boolean-option=true
                    // your-app --some-boolean-option
                    // your-app --!some-string-option
                    a = cmd.nextFlag().get(session);
                    if (a.isActive()) {
                        someBooleanOption = a.getBooleanValue().get(session);
                    }
                    break;
                }
                default: {
                    if (a.isNonOption()) {
                        nonOptions.add(cmd.next().flatMap(NLiteral::asString).get(session));
                    } else {
                        // this is an unsupported options!
                        cmd.throwUnexpectedArgument();
                    }
                }
            }
        }
        // this will fire an exception if no option is provided!
        if (someStringOption == null) {
            //cmd.next("--some-string-option").get(session);
        }
        //the application can be run in one of 'execMode' and 'autoCompleteMode' modes
        if (session.isAppExecMode()) {
            //only run if in execMode
            //just display the options as an example of execution
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("someStringOption", someStringOption);
            result.put("someBooleanOption", someBooleanOption);
            result.put("nonOptions", nonOptions);
            session.out().println(result);
        }
    }


}
