package net.thevpc.nuts.indexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

public class NIndexerApplication implements NApplication {
    private static NApplicationContext __bootApplicationContext;

    public static void main(String[] args) {
        new NIndexerApplication().runAndExit(args);
    }

    @Override
    public void run(NApplicationContext applicationContext) {
        __bootApplicationContext =applicationContext;
        ConfigurableApplicationContext c = SpringApplication.run(Config.class, new String[0]);
        final Config cc = c.getBean(Config.class);
        cc.applicationContext = applicationContext;
        final Object lock = new Object();
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(NIndexerApplication.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    @SpringBootApplication
    @EnableScheduling
    public static class Config {

        private NApplicationContext applicationContext;

        public NApplicationContext getApplicationContext() {
            if (applicationContext == null) {
                if(__bootApplicationContext ==null){
                    throw new IllegalStateException("missing Boot Application context");
                }
                applicationContext= __bootApplicationContext;
            }
            return applicationContext;
        }

    }

}
