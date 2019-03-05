package net.vpc.app.nuts.indexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NutsIndexerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NutsIndexerApplication.class, args);
    }
}
