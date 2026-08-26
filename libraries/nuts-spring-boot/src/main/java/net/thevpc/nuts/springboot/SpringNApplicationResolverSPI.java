package net.thevpc.nuts.springboot;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NApplicationHandler;
import net.thevpc.nuts.spi.NAppResolverSPI;
import org.springframework.context.ApplicationContext;

import java.util.Map;

public class SpringNApplicationResolverSPI implements NAppResolverSPI {
    public static ApplicationContext globalApplicationContext;
    @Override
    public Object resolveCurrentApplication() {
        if(globalApplicationContext!=null){
            Map<String, Object> all = globalApplicationContext.getBeansWithAnnotation(NApp.class);
            if(all.size()==1){
                return all.values().iterator().next();
            }
            if(all.size()>1){
                throw new IllegalArgumentException("more than one @NApp found : "+all.keySet());
            }
            all= (Map) globalApplicationContext.getBeansOfType(NApplicationHandler.class);
            if(all.size()==1){
                return all.values().iterator().next();
            }
            if(all.size()>1){
                throw new IllegalArgumentException("more than one NApplication found : "+all.keySet());
            }
            throw new IllegalArgumentException("no @NApp found");
        }
        return null;
    }
}
