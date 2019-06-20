package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * type: Command Class
 *
 * @author vpc
 */
public class DefaultInfoFormat extends DefaultFormatBase<NutsWorkspaceInfoFormat> implements NutsWorkspaceInfoFormat {

    private final Properties extraProperties = new Properties();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private List<String> requests = new ArrayList<>();

    public DefaultInfoFormat(NutsWorkspace ws) {
        super(ws, "info");
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories() {
        showRepositories(true);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat showRepositories(boolean enable) {
        return setShowRepositories(enable);
    }

    @Override
    public NutsWorkspaceInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsWorkspaceInfoFormat addProperties(Properties p) {
        if (p != null) {
            extraProperties.putAll(p);
        }
        return this;
    }

    @Override
    public boolean isFancy() {
        return fancy;
    }

    @Override
    public NutsWorkspaceInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print(Writer w) {
        LinkedHashMap<String, Object> r = null;
        if (requests.isEmpty()) {
            r = buildWorkspaceMap(isShowRepositories());
        } else {
            final LinkedHashMap<String, Object> t = buildWorkspaceMap(true);
            r = new LinkedHashMap<>();
            NutsCommandLine requestCmd = ws.commandLine().setArgs(requests);
            while (!requestCmd.isEmpty()) {
                NutsArgument a = requestCmd.next();
                if (t.containsKey(a.toString())) {
                    r.put(a.toString(), t.get(a.toString()));
                }
            }
        }
        NutsObjectFormat m = ws.format().object().session(getValidSession()).value(r);
        List<String> args = new ArrayList<>();
        args.add("--escape-text=false");
        if (isFancy()) {
            args.add("--multiline-property=nuts-runtime-path=:|;");
            args.add("--multiline-property=nuts-boot-runtime-path=:|;");
            args.add("--multiline-property=java.class.path=" + File.pathSeparator);
            args.add("--multiline-property=java-class-path=" + File.pathSeparator);
            args.add("--multiline-property=java.library.path=" + File.pathSeparator);
            args.add("--multiline-property=java-library-path=" + File.pathSeparator);
        }
        m.configure(true, args.toArray(new String[0]));
        m.print(w);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--show-repos": {
                this.setShowRepositories(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--fancy": {
                this.setFancy(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--add": {
                NutsArgument r = cmdLine.nextString().getArgumentValue();
                extraProperties.put(r.getStringKey(), r.getStringValue());
                return true;
            }
            default: {
                if (getValidSession().configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String key(String prefix, String key) {
        if (CoreStringUtils.isBlank(prefix)) {
            return key;
        }
        return prefix + "." + key;
    }

//    @Override
    private LinkedHashMap<String, Object> buildWorkspaceMap(boolean deep) {
        String prefix = null;
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        NutsWorkspaceConfigManager configManager = ws.config();
        NutsBootContext rtcontext = configManager.getContext(NutsBootContextType.RUNTIME);
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }

        props.put("nuts-api-version", stringValue(rtcontext.getApiId().getVersion().toString()));
        props.put("nuts-api-id", stringValue(rtcontext.getApiId().toString()));
        props.put("nuts-runtime-id", stringValue(rtcontext.getRuntimeId().toString()));
        URL[] cl = configManager.getBootClassWorldURLs();
        List<String> runtimeClassPath = new ArrayList<>();
        if (cl != null) {
            for (URL url : cl) {
                if (url != null) {
                    String s = url.toString();
                    try {
                        s = Paths.get(url.toURI()).toFile().getPath();
                    } catch (URISyntaxException ex) {
                        s = s.replace(":", "\\:");
                    }
                    runtimeClassPath.add(s);
                }
            }
        }

        props.put("nuts-runtime-path", stringValue(CoreStringUtils.join(";", runtimeClassPath)));
        props.put("nuts-workspace", stringValue(configManager.getWorkspaceLocation().toString()));
        props.put("nuts-workspace-id", stringValue(configManager.getUuid()));
        props.put("nuts-secure", (ws.security().isSecure()));
        props.put("nuts-store-layout", stringValue(configManager.getStoreLocationLayout()));
        props.put("nuts-store-strategy", stringValue(configManager.getStoreLocationStrategy()));
        props.put("nuts-repo-store-strategy", stringValue(configManager.getRepositoryStoreLocationStrategy()));
        props.put("nuts-open-mode", stringValue(configManager.options().getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : configManager.options().getOpenMode()));
        props.put("nuts-trace", (configManager.options().isTrace()));
        props.put("nuts-read-only", (configManager.options().isReadOnly()));
        props.put("nuts-skip-companions", (configManager.options().isSkipInstallCompanions()));
        props.put("nuts-skip-welcome", (configManager.options().isSkipWelcome()));
        for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.name().toLowerCase(), configManager.getStoreLocation(folderType).toString());
        }
        props.put("java-version", System.getProperty("java.version"));
        props.put("java-executable", CoreIOUtils.resolveJavaCommand(null));
        props.put("java-classpath", System.getProperty("java.class.path"));
        props.put("java-library-path", System.getProperty("java.library.path"));
        props.put("os-name", ws.config().getPlatformOs().toString());
        props.put("os-family", stringValue(ws.config().getPlatformOsFamily()));
        if (ws.config().getPlatformOsDist() != null) {
            props.put("os-dist", ws.config().getPlatformOsDist().toString());
        }
        props.put("os-arch", ws.config().getPlatformArch().toString());
        props.put("user-name", System.getProperty("user.name"));
        props.put("user-home", System.getProperty("user.home"));
        props.put("user-dir", System.getProperty("user.dir"));
        props.put("command-line-long", ws.config().options().format().compact(false).getBootCommandLine());
        props.put("command-line-short", ws.config().options().format().compact(true).getBootCommandLine());
        props.put("inherited", ws.config().options().isInherited());
        props.put("inherited-nuts-boot-args", System.getProperty("nuts.boot.args"));
        props.put("inherited-nuts-args", System.getProperty("nuts.args"));
        props.put("creation-started", stringValue(Instant.ofEpochMilli(ws.config().getCreationStartTimeMillis())));
        props.put("creation-finished", stringValue(Instant.ofEpochMilli(ws.config().getCreationFinishTimeMillis())));
        props.put("creation-within", CoreCommonUtils.formatPeriodMilli(ws.config().getCreationTimeMillis()).trim());
        props.put("repositories-count", (ws.config().getRepositories().length));
        for (String extraKey : extraKeys) {
            props.put(extraKey, extraProperties.getProperty(extraKey));
        }
        if (deep) {
            Map<String, Object> repositories = new LinkedHashMap<>();
            props.put("repos", repositories);
            for (NutsRepository repository : ws.config().getRepositories()) {
                repositories.put(repository.config().name(), buildRepoRepoMap(repository, deep, prefix));
            }
        }

        return props;
    }

    private Map<String, Object> buildRepoRepoMap(NutsRepository repo, boolean deep, String prefix) {
        LinkedHashMap<String, Object> props = new LinkedHashMap<>();
        props.put(key(prefix, "name"), stringValue(repo.config().getName()));
        props.put(key(prefix, "global-name"), repo.config().getGlobalName());
        props.put(key(prefix, "uuid"), stringValue(repo.config().getUuid()));
        props.put(key(prefix, "type"), repo.config().getType());
        props.put(key(prefix, "speed"), (repo.config().getSpeed()));
        props.put(key(prefix, "enabled"), (repo.config().isEnabled()));
        props.put(key(prefix, "index-enabled"), (repo.config().isIndexEnabled()));
        props.put(key(prefix, "index-subscribed"), (repo.config().isIndexSubscribed()));
        props.put(key(prefix, "location"), repo.config().getLocation(false));
        if (repo.config().getLocation(false) != null) {
            props.put(key(prefix, "location-expanded"), repo.config().getLocation(true));
        }
        props.put(key(prefix, "deploy-order"), stringValue(repo.config().getDeployOrder()));
        props.put(key(prefix, "store-location-strategy"), stringValue(repo.config().getStoreLocationStrategy()));
        props.put(key(prefix, "store-location"), stringValue(repo.config().getStoreLocation()));
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            props.put(key(prefix, "store-location-" + value.name().toLowerCase()), stringValue(repo.config().getStoreLocation(value)));
        }
        props.put(key(prefix, "supported-mirroring"), (repo.config().isSupportedMirroring()));
        if (repo.config().isSupportedMirroring()) {
            props.put(key(prefix, "mirrors-count"), ((!repo.config().isSupportedMirroring()) ? 0 : repo.config().getMirrors().length));
        }
        if (deep) {
            if (repo.config().isSupportedMirroring()) {
                Map<String, Object> mirrors = new LinkedHashMap<>();
                props.put("mirrors", mirrors);
                for (NutsRepository mirror : repo.config().getMirrors()) {
                    mirrors.put(mirror.config().name(), buildRepoRepoMap(mirror, deep, null));
                }
            }
        }
        return props;
    }

    private String stringValue(Object s) {
        return CoreCommonUtils.stringValue(s);
    }
}
