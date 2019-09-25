package net.vpc.app.nuts.indexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsApplication;
import net.vpc.app.nuts.NutsApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

public class NutsIndexerApplication extends NutsApplication {
    private static NutsApplicationContext __bootApplicationContext;

    public static void main(String[] args) {
        new NutsIndexerApplication().runAndExit(args);
    }

    @Override
    public void run(NutsApplicationContext applicationContext) {
        __bootApplicationContext =applicationContext;
        ConfigurableApplicationContext c = SpringApplication.run(Config.class, new String[0]);
        final Config cc = c.getBean(Config.class);
        cc.applicationContext = applicationContext;
        final Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(NutsIndexerApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    @SpringBootApplication
    @EnableScheduling
    public static class Config {

        private NutsApplicationContext applicationContext;

        public NutsApplicationContext getApplicationContext() {
            if (applicationContext == null) {
                if(__bootApplicationContext ==null){
                    throw new IllegalStateException("Missing Boot Application context");
                }
                applicationContext= __bootApplicationContext;
            }
            return applicationContext;
        }

    }

}
