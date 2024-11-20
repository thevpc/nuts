package net.thevpc.nuts.indexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NIndexerApplication implements NApplication {

    public static void main(String[] args) {
        new NIndexerApplication().runAndExit(args);
    }

    @Override
    public void run() {

    }


}
