package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.util.NutsPlatformUtils;
import net.thevpc.nuts.util.NutsStringUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class NutsCliInfo {
    public static Map<String, String> loadConfigMap(NutsSession session) {
        Path userConfig = getConfigFile();
        Map m = null;
        NutsElements elems = NutsElements.of(session);
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

    public static String loadVar(String name, NutsSession session, Function<NutsSession, String> generator) {
        Map<String, String> m = loadConfigMap(session);
        String _uuid = NutsStringUtils.trimToNull(m.get(name));
        if (!NutsBlankable.isBlank(_uuid)) {
            return _uuid;
        } else {
            String varVal = null;
            if (generator != null) {
                varVal = generator.apply(session);
                if (varVal != null) {
                    m.put(name, varVal);
                    NutsElements elems = NutsElements.of(session);
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

    public static String saveVar(String name, String value, NutsSession session) {
        Map<String, String> m = loadConfigMap(session);
        String old = NutsStringUtils.trimToNull(m.get(name));
        value = NutsStringUtils.trimToNull(value);
        if (Objects.equals(old, value)) {
            if (value == null) {
                m.remove(name);
            } else {
                m.put(name, value);
            }
            NutsElements elems = NutsElements.of(session);
            try {
                Path userConfig = getConfigFile();
                elems.json().setValue(m).print(userConfig);
            } catch (Exception ex) {
                //ignore
            }
        }
        return old;
    }

    public static String loadCliId(NutsSession session) {
        return loadCliId(session,true);
    }

    public static String loadCliId(NutsSession session, boolean auto) {
        return loadVar("user", session, auto ? s -> UUID.randomUUID().toString() : null);
    }

    public static String saveCliId(String value, NutsSession session) {
        return saveVar("user", value, session);
    }

    @NotNull
    private static Path getConfigFile() {
        return Paths.get(NutsPlatformUtils.getWorkspaceLocation(
                NutsOsFamily.getCurrent(),
                false,
                null
        )).getParent().resolve(".nuts-user-config");
    }
}
