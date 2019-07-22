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
public class DefaultNutsInfoFormat extends DefaultFormatBase<NutsInfoFormat> implements NutsInfoFormat {

    private final Properties extraProperties = new Properties();
    private boolean showRepositories = false;
    private boolean fancy = false;
    private List<String> requests = new ArrayList<>();
    private boolean lenient=false;

    public DefaultNutsInfoFormat(NutsWorkspace ws) {
        super(ws, "info");
    }

    @Override
    public NutsInfoFormat showRepositories() {
        showRepositories(true);
        return this;
    }

    @Override
    public NutsInfoFormat showRepositories(boolean enable) {
        return setShowRepositories(enable);
    }

    @Override
    public NutsInfoFormat setShowRepositories(boolean enable) {
        this.showRepositories = true;
        return this;
    }

    @Override
    public boolean isShowRepositories() {
        return showRepositories;
    }

    @Override
    public NutsInfoFormat addProperty(String key, String value) {
        extraProperties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsInfoFormat addProperties(Properties p) {
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
    public NutsInfoFormat setFancy(boolean fancy) {
        this.fancy = fancy;
        return this;
    }

    @Override
    public void print(Writer w) {
        NutsObjectFormat m = ws.object().session(getValidSession());
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

        LinkedHashMap<String, Object> r = null;
        if (requests.isEmpty()) {
            r = buildWorkspaceMap(isShowRepositories());
        } else if(requests.size()==1){
            final LinkedHashMap<String, Object> t = buildWorkspaceMap(true);
            String key = requests.get(0);
            Object v = t.get(key);
            if(v!=null){
                m.value(v).print(w);
            }else{
                if(!isLenient()) {
                    throw new NutsIllegalArgumentException(ws, "Property not found : " + key);
                }
            }
            return;
        } else {
            final LinkedHashMap<String, Object> t = buildWorkspaceMap(true);
            r = new LinkedHashMap<>();
            for (String request : requests) {
                if (t.containsKey(request)) {
                    r.put(request, t.get(request));
                }else{
                    if(!isLenient()) {
                        throw new NutsIllegalArgumentException(ws, "Property not found : " + request);
                    }
                }
            }
        }
        m.value(r).print(w);
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
            case "--lenient": {
                this.setLenient(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--add": {
                NutsArgument r = cmdLine.nextString().getArgumentValue();
                extraProperties.put(r.getStringKey(), r.getStringValue());
                return true;
            }
            case "--get": {
                String r = cmdLine.nextString().getStringValue();
                requests.add(r);
                while(true){
                    NutsArgument p = cmdLine.peek();
                    if(p!=null && !p.isOption()){
                        cmdLine.skip();
                        requests.add(p.getString());
                    }else{
                        break;
                    }
                }
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
        NutsWorkspaceConfigManager rt = ws.config();
        NutsWorkspaceOptions options = ws.config().getOptions();
        Set<String> extraKeys = new TreeSet<>();
        if (extraProperties != null) {
            extraKeys = new TreeSet(extraProperties.keySet());
        }

        props.put("name", stringValue(rt.getName()));
        props.put("nuts-api-version", stringValue(rt.getApiVersion()));
        props.put("nuts-api-id", stringValue(rt.getApiId().getLongName()));
        props.put("nuts-runtime-id", stringValue(rt.getRuntimeId().getLongName()));
        URL[] cl = rt.getBootClassWorldURLs();
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
        props.put("nuts-workspace-id", stringValue(rt.getUuid()));
        props.put("nuts-store-layout", stringValue(rt.getStoreLocationLayout()));
        props.put("nuts-store-strategy", stringValue(rt.getStoreLocationStrategy()));
        props.put("nuts-repo-store-strategy", stringValue(rt.getRepositoryStoreLocationStrategy()));
        props.put("nuts-global", options.isGlobal());
        props.put("nuts-workspace", stringValue(rt.getWorkspaceLocation().toString()));
        for (NutsStoreLocation folderType : NutsStoreLocation.values()) {
            props.put("nuts-workspace-" + folderType.name().toLowerCase(), rt.getStoreLocation(folderType).toString());
        }
        props.put("nuts-open-mode", stringValue(options.getOpenMode() == null ? NutsWorkspaceOpenMode.OPEN_OR_CREATE : options.getOpenMode()));
        props.put("nuts-secure", (ws.security().isSecure()));
        props.put("nuts-gui", options.isGui());
        props.put("nuts-inherited", options.isInherited());
        props.put("nuts-recover", options.isRecover());
        props.put("nuts-reset", options.isReset());
        props.put("nuts-debug", options.isDebug());
        props.put("nuts-trace", options.isTrace());
        props.put("nuts-read-only", (options.isReadOnly()));
        props.put("nuts-skip-companions", options.isSkipCompanions());
        props.put("nuts-skip-welcome", options.isSkipWelcome());
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

    public boolean isLenient() {
        return lenient;
    }

    public NutsInfoFormat setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }
}
