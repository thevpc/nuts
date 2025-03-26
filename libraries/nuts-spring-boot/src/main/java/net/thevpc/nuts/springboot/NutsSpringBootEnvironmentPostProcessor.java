package net.thevpc.nuts.springboot;

import net.thevpc.nuts.NApp;
import net.thevpc.nuts.util.NOptional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class NutsSpringBootEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        NOptional<NApp> app = NApp.get();
        System.out.println(app);
//        if(AppPropsHelper.getAppClass()!=null && AppPropsHelper.getDefaultAppName()!=null) {
//            File file = new File(AppPropsHelper.getCustomExternalConfigFile("application.properties",
//                    AppPropsHelper.getAppClass(), AppPropsHelper.getDefaultAppName()));
//            if (file.exists()) {
//                Properties p = new Properties();
//                try (FileReader r = new FileReader(file)) {
//                    p.load(r);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                environment.getPropertySources().addAfter(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
//                        new PropertiesPropertySource("prod-properties", p)
//                );
//            }
//        }
    }
}
