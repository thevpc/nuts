package net.thevpc.nuts.boot.internal.cmdline;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NBootOptionRegistry {
    private static final List<NBootOptionSpec> SPECS = buildSpecs();
    private static final Map<String, NBootOptionSpec> BY_NAME = index(SPECS);

    private NBootOptionRegistry() {}

    public static NBootOptionSpec byName(String name) { return BY_NAME.get(name); }
    public static List<String> allNames() { return new ArrayList<>(BY_NAME.keySet()); }

    private static Map<String, NBootOptionSpec> index(List<NBootOptionSpec> specs) {
        Map<String, NBootOptionSpec> m = new LinkedHashMap<>();
        for (NBootOptionSpec s : specs) {
            for (String n : s.names()) {
                if (m.put(n, s) != null) {
                    throw new IllegalStateException("duplicate option name registered: " + n);
                }
            }
        }
        return m;
    }

    private static List<NBootOptionSpec> buildSpecs() {
        List<NBootOptionSpec> l = new ArrayList<>();

        // ---- create exported options ----
        l.add(NBootOptionSpec.fileValue("-w", "--workspace"));
        l.add(NBootOptionSpec.freeValue("--user", "-u"));
        l.add(NBootOptionSpec.freeValue("--password", "-p")); // sensitive: value never enumerated, only the flag name
        l.add(NBootOptionSpec.freeValue("-V", "--boot-version", "--boot-api-version"));
        l.add(NBootOptionSpec.freeValue("--boot-runtime"));
        l.add(NBootOptionSpec.fileValue("--java", "--boot-java", "-j"));
        l.add(NBootOptionSpec.fileValue("--java-home", "--boot-java-home"));
        l.add(NBootOptionSpec.freeValue("--java-options", "--boot-java-options", "-J"));

        // ---- create options ----
        l.add(NBootOptionSpec.freeValue("--name"));
        l.add(NBootOptionSpec.freeValue("--archetype", "-A"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--store-strategy"}, "exploded", "standalone"));
        l.add(NBootOptionSpec.flag("-S", "--standalone", "--standalone-workspace"));
        l.add(NBootOptionSpec.flag("-E", "--exploded", "--exploded-workspace"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--repo-store-strategy"}, "exploded", "standalone"));
        l.add(NBootOptionSpec.flag("--exploded-repositories"));
        l.add(NBootOptionSpec.flag("--standalone-repositories"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--store-layout"}, "windows", "linux", "macos", "unix"));
        l.add(NBootOptionSpec.flag("--system-layout"));
        l.add(NBootOptionSpec.flag("--windows-layout"));
        l.add(NBootOptionSpec.flag("--macos-layout"));
        l.add(NBootOptionSpec.flag("--linux-layout"));
        l.add(NBootOptionSpec.flag("--unix-layout"));

        for (String loc : new String[]{"--bin-location", "--config-location", "--var-location",
                "--log-location", "--temp-location", "--cache-location", "--lib-location"}) {
            l.add(NBootOptionSpec.fileValue(loc));
        }
        for (String os : new String[]{"system", "windows", "macos", "linux", "unix"}) {
            for (String folder : new String[]{"bin", "conf", "var", "log", "temp", "cache", "lib", "run"}) {
                l.add(NBootOptionSpec.fileValue("--" + os + "-" + folder + "-home"));
            }
        }

        l.add(NBootOptionSpec.flag("--install-companions", "-k"));
        l.add(NBootOptionSpec.flag("--skip-welcome", "-K"));
        l.add(NBootOptionSpec.flag("--skip-boot", "-Q"));
        l.add(NBootOptionSpec.flag("--switch"));

        // ---- open exported options ----
        l.add(NBootOptionSpec.flag("-g", "--global"));
        l.add(NBootOptionSpec.flag("--shared-instance"));
        l.add(NBootOptionSpec.flag("--gui"));
        l.add(NBootOptionSpec.optionalEnumValue(new String[]{"--color", "-c"},
                "default", "inherited", "ansi", "formatted", "filtered"));
        l.add(NBootOptionSpec.flag("-B", "--bot"));
        l.add(NBootOptionSpec.flag("-U", "--preview-repo"));
        l.add(NBootOptionSpec.flag("-R", "--read-only"));
        l.add(NBootOptionSpec.flag("-t", "--trace"));
        l.add(NBootOptionSpec.optionalValue("-P", "--progress"));
        l.add(NBootOptionSpec.freeValue("--solver"));
        l.add(NBootOptionSpec.flag("--dry", "-D"));
        l.add(NBootOptionSpec.flag("-d", "--stacktrace"));
        l.add(NBootOptionSpec.optionalValue("--debug"));

        // ---- logging ----
        l.add(NBootOptionSpec.flag("-l", "--verbose"));
        for (String lvl : new String[]{"verbose", "finest", "finer", "fine", "info", "warning", "severe", "config", "all", "off"}) {
            l.add(NBootOptionSpec.flag("--log-" + lvl));
            l.add(NBootOptionSpec.flag("--log-term-" + lvl));
            l.add(NBootOptionSpec.flag("--log-file-" + lvl));
        }
        l.add(NBootOptionSpec.freeValue("--log-file-size"));
        l.add(NBootOptionSpec.freeValue("--log-file-name"));
        l.add(NBootOptionSpec.freeValue("--log-file-base"));
        l.add(NBootOptionSpec.freeValue("--log-file-count"));

        l.add(NBootOptionSpec.freeValue("-X", "--exclude-extension"));
        l.add(NBootOptionSpec.freeValue("--repository", "--repositories", "--repo", "--repos", "-r"));
        l.add(NBootOptionSpec.freeValue("--boot-repository", "--boot-repositories", "--boot-repo", "--boot-repos"));

        l.add(NBootOptionSpec.freeValue("--output-format-option", "-T"));
        l.add(NBootOptionSpec.enumValue(new String[]{"-O", "--output-format"},
                "tson", "yaml", "json", "plain", "xml", "table", "tree", "props"));
        for (String fmt : new String[]{"tson", "yaml", "json", "plain", "xml", "table", "tree", "props"}) {
            l.add(NBootOptionSpec.optionalValue("--" + fmt));
        }

        l.add(NBootOptionSpec.flag("--yes", "-y"));
        l.add(NBootOptionSpec.flag("--no", "-n"));
        l.add(NBootOptionSpec.flag("--error"));
        l.add(NBootOptionSpec.flag("--ask"));
        l.add(NBootOptionSpec.flag("--cached"));
        l.add(NBootOptionSpec.flag("--indexed"));
        l.add(NBootOptionSpec.flag("--transitive"));
        l.add(NBootOptionSpec.enumValue(new String[]{"-f", "--fetch"}, "offline", "online", "anywhere", "remote"));
        l.add(NBootOptionSpec.flag("-a", "--anywhere"));
        l.add(NBootOptionSpec.flag("-F", "--offline"));
        l.add(NBootOptionSpec.flag("--online"));
        l.add(NBootOptionSpec.flag("--remote"));

        // ---- open options ----
        l.add(NBootOptionSpec.flag("--embedded", "-b"));
        l.add(NBootOptionSpec.flag("--open-file"));
        l.add(NBootOptionSpec.flag("--external", "--spawn", "-x"));
        l.add(NBootOptionSpec.flag("--user-cmd", "--system"));
        l.add(NBootOptionSpec.flag("--root-cmd", "--as-root"));
        l.add(NBootOptionSpec.flag("--current-user"));
        l.add(NBootOptionSpec.freeValue("--run-as"));
        l.add(NBootOptionSpec.flag("--sudo"));
        l.add(NBootOptionSpec.enumValue(new String[]{"-o", "--open-mode"},
                "open-or-create", "create", "open", "try-open"));
        l.add(NBootOptionSpec.flag("--open-or-error", "--open"));
        l.add(NBootOptionSpec.flag("--create-or-error", "--create"));
        l.add(NBootOptionSpec.flag("--open-or-create"));
        l.add(NBootOptionSpec.flag("--open-or-null"));

        // ---- commands ----
        l.add(NBootOptionSpec.terminal("-"));       // consumes the rest of the line unconditionally
        l.add(NBootOptionSpec.terminal("-e", "--exec")); // ditto — see nextNutsArgument, both branches consume the rest

        l.add(NBootOptionSpec.flag("-version", "-v", "--version"));
        l.add(NBootOptionSpec.flag("-Z", "--reset"));
        l.add(NBootOptionSpec.flag("--reset-hard"));
        l.add(NBootOptionSpec.flag("-z", "--recover"));
        l.add(NBootOptionSpec.optionalValue("-N", "--expire"));
        l.add(NBootOptionSpec.freeValue("--out-line-prefix"));
        l.add(NBootOptionSpec.freeValue("--err-line-prefix"));
        l.add(NBootOptionSpec.freeValue("--line-prefix"));

        l.add(NBootOptionSpec.flag("-?", "--help", "-h"));
        l.add(NBootOptionSpec.flag("--skip-errors"));
        l.add(NBootOptionSpec.freeValue("-L", "--locale"));
        l.add(NBootOptionSpec.freeValue("--theme"));
        l.add(NBootOptionSpec.flag("--sandbox"));
        l.add(NBootOptionSpec.flag("--in-memory"));
        l.add(NBootOptionSpec.flag("--confined"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--isolation-level"}, "system", "user", "confined", "sandbox"));
        l.add(NBootOptionSpec.flag("--reset-options"));
        l.add(NBootOptionSpec.flag("--init-launchers"));
        l.add(NBootOptionSpec.flag("--init-java"));
        l.add(NBootOptionSpec.flag("--init-platforms"));
        l.add(NBootOptionSpec.flag("--init-scripts"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--desktop-launcher"}, "supported", "preferred", "always", "never"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--menu-launcher"}, "supported", "preferred", "always", "never"));
        l.add(NBootOptionSpec.enumValue(new String[]{"--user-launcher"}, "supported", "preferred", "always", "never"));

        return l;
    }
}
