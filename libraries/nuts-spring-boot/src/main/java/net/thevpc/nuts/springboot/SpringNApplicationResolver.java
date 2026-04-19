package net.thevpc.nuts.springboot;

import net.thevpc.nuts.app.NAppDefinition;
import net.thevpc.nuts.spi.NAppResolver;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class SpringNApplicationResolver implements NAppResolver {
    public static ApplicationContext globalApplicationContext;
    @Override
    public Object resolveCurrentApplication() {
        if(globalApplicationContext!=null){
            Map<String, Object> all = globalApplicationContext.getBeansWithAnnotation(NAppDefinition.class);
            if(all.size()==1){
                return all.values().iterator().next();
            }
            if(all.size()>1){
                throw new IllegalArgumentException("more than one NAppDefinition found : "+all.keySet());
            }
            throw new IllegalArgumentException("no NAppDefinition found");
        }
        return null;
    }
}
