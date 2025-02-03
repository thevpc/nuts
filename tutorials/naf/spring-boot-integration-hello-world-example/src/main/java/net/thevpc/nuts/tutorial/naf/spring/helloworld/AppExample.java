package net.thevpc.nuts.tutorial.naf.spring.helloworld;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPrintStream;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppExample implements NApplication {
    public static void main(String[] args) {
        SpringApplication.run(AppExample.class, args);
    }

    @Override
    public void run() {
        NOut.println("Hello ##World##");
    }
}
