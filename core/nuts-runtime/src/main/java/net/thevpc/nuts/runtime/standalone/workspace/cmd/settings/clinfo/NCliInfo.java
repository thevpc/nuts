package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NPlatformHome;
import net.thevpc.nuts.util.NStringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class NCliInfo {
    public static Map<String, String> loadConfigMap() {
        Path userConfig = getConfigFile();
        Map m = null;
        NElements elems = NElements.of();
        if (Files.exists(userConfig)) {
            try {
                m = elems.json().parse(userConfig, Map.class);
            } catch (Exception ex) {/*IGNORE*/
            }
            if (m != null) {
                return m;
            }
        }
        return new LinkedHashMap<>();
    }

    public static String loadVar(String name, Supplier<String> generator) {
        Map<String, String> m = loadConfigMap();
        String _uuid = NStringUtils.trimToNull(m.get(name));
        if (!NBlankable.isBlank(_uuid)) {
            return _uuid;
        } else {
            String varVal = null;
            if (generator != null) {
                varVal = generator.get();
                if (varVal != null) {
                    m.put(name, varVal);
                    NElements elems = NElements.of();
                    try {
                        Path userConfig = getConfigFile();
                        elems.json().setValue(m).print(userConfig);
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            return varVal;
        }
    }

    public static String saveVar(String name, String value) {
        Map<String, String> m = loadConfigMap();
        String old = NStringUtils.trimToNull(m.get(name));
        value = NStringUtils.trimToNull(value);
        if (Objects.equals(old, value)) {
            if (value == null) {
                m.remove(name);
            } else {
                m.put(name, value);
            }
            NElements elems = NElements.of();
            try {
                Path userConfig = getConfigFile();
                elems.json().setValue(m).print(userConfig);
            } catch (Exception ex) {
                //ignore
            }
        }
        return old;
    }

    public static String loadCliId() {
        return loadCliId(true);
    }

    public static String loadCliId(boolean auto) {
        return loadVar("user", auto ? () -> UUID.randomUUID().toString() : null);
    }

    public static String saveCliId(String value) {
        return saveVar("user", value);
    }

    private static Path getConfigFile() {
        return Paths.get(NPlatformHome.USER.getHome()).resolve(".nuts-user-config");
    }
}
