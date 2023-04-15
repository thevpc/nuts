package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NPlatformUtils;
import net.thevpc.nuts.util.NStringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class NCliInfo {
    public static Map<String, String> loadConfigMap(NSession session) {
        Path userConfig = getConfigFile();
        Map m = null;
        NElements elems = NElements.of(session);
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

    public static String loadVar(String name, NSession session, Function<NSession, String> generator) {
        Map<String, String> m = loadConfigMap(session);
        String _uuid = NStringUtils.trimToNull(m.get(name));
        if (!NBlankable.isBlank(_uuid)) {
            return _uuid;
        } else {
            String varVal = null;
            if (generator != null) {
                varVal = generator.apply(session);
                if (varVal != null) {
                    m.put(name, varVal);
                    NElements elems = NElements.of(session);
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

    public static String saveVar(String name, String value, NSession session) {
        Map<String, String> m = loadConfigMap(session);
        String old = NStringUtils.trimToNull(m.get(name));
        value = NStringUtils.trimToNull(value);
        if (Objects.equals(old, value)) {
            if (value == null) {
                m.remove(name);
            } else {
                m.put(name, value);
            }
            NElements elems = NElements.of(session);
            try {
                Path userConfig = getConfigFile();
                elems.json().setValue(m).print(userConfig);
            } catch (Exception ex) {
                //ignore
            }
        }
        return old;
    }

    public static String loadCliId(NSession session) {
        return loadCliId(session, true);
    }

    public static String loadCliId(NSession session, boolean auto) {
        return loadVar("user", session, auto ? s -> UUID.randomUUID().toString() : null);
    }

    public static String saveCliId(String value, NSession session) {
        return saveVar("user", value, session);
    }

    @NotNull
    private static Path getConfigFile() {
        return Paths.get(NPlatformUtils.getBaseLocation(NOsFamily.getCurrent(), false)).resolve(".nuts-user-config");
    }
}
