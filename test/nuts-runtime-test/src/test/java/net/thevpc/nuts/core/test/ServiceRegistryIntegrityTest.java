package net.thevpc.nuts.core.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ARCH-06: every declared service provider must exist exactly once and implement its SPI.
 */
public class ServiceRegistryIntegrityTest {

    private static final String[] SERVICES = {
            "net.thevpc.nuts.spi.NComponent",
            "net.thevpc.nuts.boot.NBootWorkspaceFactory"
    };

    @Test
    public void serviceDeclarationsAreUniqueAndAssignable() throws Exception {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        for (String service : SERVICES) {
            List<String> names = readServiceEntries(service, loader);
            Assertions.assertFalse(names.isEmpty(), "no providers declared for " + service);
            Set<String> unique = new HashSet<>(names);
            Assertions.assertEquals(unique.size(), names.size(),
                    "duplicate provider declaration in " + service + ": " + names);

            Class<?> spi = Class.forName(service, false, loader);
            for (String name : names) {
                Class<?> provider = Class.forName(name, false, loader);
                Assertions.assertTrue(spi.isAssignableFrom(provider),
                        name + " does not implement " + service);
            }
        }
    }

    private static List<String> readServiceEntries(String service, ClassLoader loader) throws Exception {
        List<String> result = new ArrayList<>();
        Enumeration<URL> resources = loader.getResources("META-INF/services/" + service);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            try (InputStream in = resource.openStream();
                 BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"))) {
                String line;
                while ((line = r.readLine()) != null) {
                    int comment = line.indexOf('#');
                    if (comment >= 0) {
                        line = line.substring(0, comment);
                    }
                    line = line.trim();
                    if (!line.isEmpty()) {
                        result.add(line);
                    }
                }
            }
        }
        return result;
    }
}
