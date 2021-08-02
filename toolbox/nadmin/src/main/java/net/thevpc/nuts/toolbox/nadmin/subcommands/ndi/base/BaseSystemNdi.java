package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    public static final ReplaceString COMMENT_LINE_CONFIG_HEADER = new ReplaceString("net.thevpc.nuts configuration", "((net[.]thevpc[.]nuts)|(net[.]thevpc[.]nuts.toolbox[.]ndi)|(net[.]vpc[.]app[.]nuts)) configuration");

    private static final Logger LOG = Logger.getLogger(BaseSystemNdi.class.getName());

    public BaseSystemNdi(NutsApplicationContext appContext) {
        super(appContext);
    }
    public Path getNutsRcPath(String nutsVersion, String switchWorkspaceLocation) {
        return getNutsApiAppsFolder(nutsVersion, switchWorkspaceLocation).resolve(getExecFileName(".nutsrc"));
    }

    public Path getNutsStartPath() {
        return getNadminAppsFolder().resolve(getExecFileName("nuts"));
    }

    public Path getNutsApiAppsFolder(String nutsVersion, String switchWorkspaceLocation) {
        final NutsWorkspace ws = context.getWorkspace();
        NutsId nid = getNutsId(nutsVersion, switchWorkspaceLocation);
        if (switchWorkspaceLocation != null) {
            NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getStoreLocation(nid, NutsStoreLocation.APPS));
        } else {
            return Paths.get(ws.locations().getStoreLocation(nid, NutsStoreLocation.APPS));
        }
    }
    public NutsId getNutsId(String nutsVersion, String switchWorkspaceLocation) {
        final NutsWorkspace ws = context.getWorkspace();
        NutsId nid = ws.getApiId();
        if (nutsVersion != null && nutsVersion.length() > 0) {
            String c = ws.getApiId().getVersion().toString();
            if (!c.equals(nutsVersion)) {
                return nid.builder().setVersion(nutsVersion).build();
            }
        }
        return nid;
    }

    //ws.getApiId().getVersion()
    public Path getNadminAppsFolder() {
        final NutsWorkspace ws = context.getWorkspace();
        return Paths.get(ws.locations().getStoreLocation(context.getAppId(), NutsStoreLocation.APPS));
    }

    public Path getScriptFile(String name) {
        Path bin = Paths.get(context.getAppsFolder());
        return bin.resolve(getExecFileName(name));
    }

    public abstract boolean isComments(String line);

    //    @Override
    public abstract String trimComments(String line);

    public Path getNutsAppsFolder(String apiVersion, String switchWorkspaceLocation) {
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        apiId = ws.search().addId(apiId).setLatest(true).setFailFast(true).setContent(true).getResultDefinitions().singleton().getId();
        if (switchWorkspaceLocation != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS));
        } else {
            return Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
        }
    }

    protected abstract String createNutsScriptCommand(NutsId fnutsId, NdiScriptOptions options);

    public String createBootScriptCommand(NutsDefinition apiDef, boolean includeEnv) {
        String txt = NdiUtils.generateScriptAsString("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateNutsName(),
                ss -> {
                    switch (ss) {
                        case "NUTS_JAR":
                            return apiDef.getPath().toString();
                    }
                    return null;
                }
        );
        if (includeEnv) {
            Path nutsRcPath = getNutsRcPath(apiDef.getId().getVersion().toString(), null);
            txt = getCallScriptCommand(nutsRcPath.toString()) + "\n" + txt;
        }
        return txt;
    }

    public WorkspaceAndApiVersion persistConfig(String switchWorkspaceLocation, String apiVersion, String preferredName) {
        NutsWorkspace ws = context.getWorkspace();
        NutsWorkspaceBootConfig bootConfig = null;
        if (switchWorkspaceLocation != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
        }
        if (apiVersion == null) {
            if (switchWorkspaceLocation == null) {
                apiVersion = ws.getApiVersion().toString();
            } else {
                NutsVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                                    Paths.get(bootConfig.getStoreLocation(ws.getApiId(), NutsStoreLocation.CONFIG))
                                            .getParent())
                            .filter(
                                    f
                                            -> ws.version().parse(f.getFileName().toString()).getNumber(0, -1) != -1
                                            && Files.exists(f.resolve("nuts-api-config.json"))
                            ).map(
                                    f -> ws.version().parse(f.getFileName().toString())
                            ).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (_latestVersion == null) {
                    throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("missing nuts-api version to link to"));
                }
                apiVersion = _latestVersion.toString();
            }
        }
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        ws.fetch().setId(apiId).setFailFast(true).getResultDefinition();
        String wsEff = bootConfig != null ? bootConfig.getEffectiveWorkspace()
                : ws.locations().getWorkspaceLocation().toString();
        PathInfo[] t = persistConfig2(switchWorkspaceLocation, apiId, preferredName);
        return new WorkspaceAndApiVersion(wsEff, ws.version().parse(apiVersion), t);
    }

    @Override
    public List<NdiScriptInfo> createNutsScript(NdiScriptOptions options) {
        NutsId nid = context.getWorkspace().id().parser().parse(options.getId());
        if (isNutsBootId(nid)) {
            return createBootScript(
                    options.getPreferredScriptName(),
                    nid.getVersion().toString(),
                    options.isForceBoot() || options.getSession().isYes(), options.getSession().isTrace(), options.isIncludeEnv());
        } else {
            List<NdiScriptInfo> r = new ArrayList<>();
            if (options.isAddNutsScript()) {
                r.addAll(createBootScript(
                        null,
                        null,
                        false, false, options.isIncludeEnv()));
            }
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = context.getWorkspace().search()
                        .setSession(context.getSession().copy().setTrace(false))
                        .addId(options.getId()).setLatest(true).getResultDefinitions().required();
                nid = fetched.getId().getShortNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            boolean gen = true;
            if (exists) {
                if (!context.getSession().getTerminal().ask()
                        .resetLine()
                        .setDefaultValue(false).setSession(context.getSession())
                        .forBoolean("override existing script %s ?",
                                context.getWorkspace().text().forStyled(
                                        NdiUtils.betterPath(ff.toString()), NutsTextStyle.path()
                                )
                        ).getBooleanValue()) {
                    gen = false;
                }
            }
            if (gen) {
                final NutsId fnutsId = nid;
                NdiScriptInfo p = createScript(
                        options.isIncludeEnv() ? NdiScriptInfo.Type.ARTIFACT_WITH_ENV : NdiScriptInfo.Type.ARTIFACT_WITHOUT_ENV,
                        n, options.getPreferredScriptName(), fnutsId, options.getSession().isTrace(), nid.toString(),
                        x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return "RUN : " + fnutsId;
                                case "GENERATOR":
                                    return context.getAppId().toString();
                                case "BODY": {
                                    return createNutsScriptCommand(fnutsId, options);
                                }
                            }
                            return null;
                        }
                );
                r.add(p);
            }
            if (options.isCreateDesktop()) {
                for (PathInfo pathInfo : configurePathShortcut(
                        AppShortcutTarget.DESKTOP, nid,
                        context.getWorkspace().getApiId().getVersion().toString(),
                        null, options.getMenuPath(),
                        options.getIcon(),
                        options.getCwd(),
                        options.getAppArgs().toArray(new String[0])
                )) {
                    r.add(new NdiScriptInfo(
                            NdiScriptInfo.Type.DESKTOP_SHORTCUT,
                            pathInfo.getPath().getFileName().toString(),
                            nid, pathInfo.getPath(),
                            pathInfo.getStatus() == PathInfo.Status.OVERRIDDEN
                    ));
                }
            }
            if (options.isCreateMenu()) {
                for (PathInfo pathInfo : configurePathShortcut(
                        AppShortcutTarget.MENU, nid,
                        context.getWorkspace().getApiId().getVersion().toString(),
                        null, options.getMenuPath(),
                        options.getIcon(),
                        options.getCwd(),
                        options.getAppArgs().toArray(new String[0])
                )) {
                    r.add(new NdiScriptInfo(
                            NdiScriptInfo.Type.DESKTOP_MENU,
                            pathInfo.getPath().getFileName().toString(),
                            nid, pathInfo.getPath(),
                            pathInfo.getStatus() == PathInfo.Status.OVERRIDDEN
                    ));
                }
            }
            return r;
        }
    }

    @Override
    public void removeNutsScript(String id, NutsSession session) {
        NutsId nid = context.getWorkspace().id().parser().parse(id);
        Path f = getScriptFile(nid.getArtifactId());
        NutsTextManager factory = context.getWorkspace().text();
        if (Files.isRegularFile(f)) {
            if (session.getTerminal().ask()
                    .resetLine()
                    .forBoolean("tool %s will be removed. Confirm?",
                            factory.forStyled(NdiUtils.betterPath(f.toString()), NutsTextStyle.path())
                    )
                    .setDefaultValue(true)
                    .setSession(session)
                    .getBooleanValue()) {
                try {
                    Files.delete(f);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                if (session.isPlainTrace()) {
                    session.out().printf("tool %s removed.%n", factory.forStyled(NdiUtils.betterPath(f.toString()), NutsTextStyle.path()));
                }
            }
        }
    }

    @Override
    public void addNutsWorkspaceScript(String preferredScriptName, String switchWorkspaceLocation, String apiVersion) {
        WorkspaceAndApiVersion v = persistConfig(switchWorkspaceLocation, apiVersion, preferredScriptName);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("added script %s to point to %s %s\n", v.getWorkspace(), v.getApiVersion(), v.getUpdatedPathStrings());
        }
    }

    @Override
    public void switchWorkspace(String switchWorkspaceLocation, String apiVersion) {
        NutsWorkspaceBootConfig bootconfig = null;
        if (switchWorkspaceLocation != null) {
            bootconfig = context.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (bootconfig == null) {
                throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        WorkspaceAndApiVersion v = persistConfig(switchWorkspaceLocation, apiVersion, null);
        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("```sh nuts``` switched to workspace %s to point to %s\n", v.getWorkspace(), v.getApiVersion());
        }
    }

    @Override
    public boolean isNutsBootId(NutsId nid) {
        if ("nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName())) {
            return true;
        }
        return false;
    }

    public List<NdiScriptInfo> createBootScript(String preferredName, String apiVersion, boolean force, boolean trace, boolean includeEnv) {
        boolean currId = false;
        NutsId apiId = null;
        NutsId b = context.getWorkspace().getApiId();
        if (apiVersion == null || apiVersion.isEmpty()) {
            apiId = b;
            apiVersion = b.getVersion().toString();
            currId = true;
        } else {
            if (!apiVersion.equals(b.getVersion().toString())) {
                apiId = b.builder().setVersion(apiVersion).build();
            } else {
                apiId = b;
            }
        }
        NutsDefinition apiDef = context.getWorkspace().search()
                .addId(apiId).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
        Path script = null;
        boolean standardPath = true;
        if (preferredName != null && preferredName.length() > 0) {
            standardPath = false;
            if (preferredName.contains("%v")) {
                preferredName = preferredName.replace("%v", apiId.getVersion().toString());
            }
            script = Paths.get(preferredName);
        } else {
            if (currId) {
                script = getScriptFile("nuts");
            } else {
                script = getScriptFile("nuts-" + apiVersion);
            }
        }
        script = script.toAbsolutePath();
        String scriptString = script.toString();
        List<NdiScriptInfo> all = new ArrayList<>();
        boolean createPath = false;
        if (!force && Files.exists(script)) {
            if (context.getSession().getTerminal().ask()
                    .resetLine()
                    .setDefaultValue(true).setSession(context.getSession())
                    .forBoolean("override existing script %s ?",
                            context.getWorkspace().text().forStyled(
                                    NdiUtils.betterPath(script.toString()), NutsTextStyle.path()
                            )
                    ).getBooleanValue()) {
                createPath = true;
            }
        } else {
            createPath = true;
        }
        if (createPath) {
            all.add(
                    createScript(
                            includeEnv ? NdiScriptInfo.Type.NUTS_WITH_ENV : NdiScriptInfo.Type.NUTS_WITHOUT_ENV,
                            "nuts", script.toString(), b, trace, apiDef.getId().getLongName(),
                            x -> {
                                switch (x) {
                                    case "NUTS_ID":
                                        return apiDef.getId().toString();
                                    case "GENERATOR":
                                        return context.getAppId().toString();
                                    case "BODY":
                                        return createBootScriptCommand(apiDef, includeEnv);
                                }
                                return null;
                            }
                    ));
        }
        if (currId && standardPath) {
            Path ff2 = Paths.get(context.getWorkspace().locations().getWorkspaceLocation())
                    .resolve("nuts");
            boolean overridden = Files.exists(ff2);
            boolean gen = true;

            if (!force && Files.exists(ff2)) {
                if (!context.getSession().getTerminal().ask()
                        .resetLine()
                        .setSession(context.getSession())
                        .forBoolean("override existing script %s ?",
                                context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path()))
                        .setDefaultValue(false)
                        .getBooleanValue()) {
                    gen = false;
                }
            }
            if (gen) {

                if (trace && context.getSession().isPlainTrace()) {
                    context.getSession().out().printf((Files.exists(ff2) ? "re-install" : "install")
                                    + " script %s %n",
                            context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path())
                    );
                }
                try {
                    try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
                        NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return apiDef.getId().toString();
                                case "GENERATOR":
                                    return context.getAppId().toString();
                                case "BODY": {
                                    return getCallScriptCommand(NdiUtils.replaceFilePrefix(scriptString, ff2.toString(), ""));
                                }
                            }
                            return null;
                        });
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                NdiUtils.setExecutable(ff2);
                all.add(new NdiScriptInfo(
                        includeEnv ?
                                NdiScriptInfo.Type.NUTS_WITH_ENV :
                                NdiScriptInfo.Type.NUTS_WITHOUT_ENV
                        , "nuts", b, ff2, overridden));
            }
        }
        return all;
    }

    @Override
    public List<NdiScriptInfo> createNutsScript(
            CreateNutsScriptCommand cmd,
            NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Path workspaceLocation = Paths.get(ws.locations().getWorkspaceLocation());
        List<NdiScriptInfo> result = new ArrayList<>();
        boolean subTrace = context.getSession().isTrace();
        if (!context.getSession().isPlainTrace()) {
            subTrace = false;
        }
        Boolean persistentConfig = cmd.getPersistentConfig();
        if (!cmd.getIdsToInstall().isEmpty()) {
            if (persistentConfig == null) {
                if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                    persistentConfig = true;
                } else {
                    persistentConfig = false;
                }
            }
            boolean includeEnv = cmd.isEnv();
            for (String id : cmd.getIdsToInstall()) {
                NutsId nid = ws.id().parser().parse(id);
                if (nid == null) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = cmd.getLinkName();
            if (includeEnv) {
                linkNameCurrent = prepareLinkName(linkNameCurrent);
            }
            List<String> nutsIds = cmd.getIdsToInstall().stream().filter(x -> isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
            List<String> nonNutsIds = cmd.getIdsToInstall().stream().filter(x -> !isNutsBootId(ws.id().parser().parse(x))).collect(Collectors.toList());
            boolean bootAlreadyProcessed = false;
            for (String id : nutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    bootAlreadyProcessed = true;
                    if (!nid.getVersion().isBlank()) {
                        String verString = nid.getVersion().toString();
                        if (verString.equalsIgnoreCase("current")
                                || verString.equalsIgnoreCase("curr")) {
                            id = nid.builder().setVersion(ws.getApiId().getVersion()).build().toString();
                        }
                    }

                    result.addAll(
                            createNutsScript(
                                    new NdiScriptOptions().setId(id)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(cmd.isFetch())
                                            .setExecType(cmd.getExecType())
                                            .setExecutorOptions(cmd.getExecutorOptions())
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                                            .setCreateDesktop(cmd.isCreateDesktop())
                                            .setCreateMenu(cmd.isCreateMenu())
                                            .setAppArgs(cmd.getAppArgs())
                                            .setMenuPath(cmd.getMenuPath())
                                            .setIcon(cmd.getIcon())
                                            .setCwd(cmd.getCwd())
                            )
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                result.addAll(createBootScript(
                        null,
                        null,
                        false, false, includeEnv));
            }
            for (String id : nonNutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    if (nid == null) {
                        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                    }
                    result.addAll(
                            createNutsScript(
                                    new NdiScriptOptions().setId(id)
                                            .setAddNutsScript(false)
                                            .setSession(context.getSession().copy().setTrace(subTrace))
                                            .setForceBoot(context.getSession().isYes())
                                            .setFetch(cmd.isFetch())
                                            .setExecType(cmd.getExecType())
                                            .setExecutorOptions(cmd.getExecutorOptions())
                                            .setIncludeEnv(includeEnv)
                                            .setPreferredScriptName(linkNameCurrent)
                                            .setCreateDesktop(cmd.isCreateDesktop())
                                            .setCreateMenu(cmd.isCreateMenu())
                                            .setAppArgs(cmd.getAppArgs())
                                            .setMenuPath(cmd.getMenuPath())
                                            .setIcon(cmd.getIcon())
                                            .setCwd(cmd.getCwd())
                            )
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
            configurePath(persistentConfig);
        }
        return result;
    }

    public NutsWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        NutsWorkspaceBootConfig bootconfig = context.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
        if (bootconfig == null) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
        }
        return bootconfig;
    }

    private String prepareLinkName(String linkName) {
        if (linkName == null) {
            linkName = "%n-%v";
        } else if (Files.isDirectory(Paths.get(linkName))) {
            linkName = Paths.get(linkName).resolve("%n-%v").toString();
        } else if (linkName.endsWith("/") || linkName.endsWith("\\")) {
            linkName = Paths.get(linkName).resolve("%n-%v").toString();
        }
        return linkName;
    }

    protected abstract PathInfo[] persistConfig2(String switchWorkspaceLocation, NutsId nutsId, String rcPath);

    public abstract String toCommentLine(String line);

    public boolean saveFile(Path filePath, String content, boolean force) {
        try {
            String fileContent = "";
            if (Files.isRegularFile(filePath)) {
                fileContent = new String(Files.readAllBytes(filePath));
            }
            if (force || !content.trim().equals(fileContent.trim())) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, content.getBytes());
                return true;
            }
            return false;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public PathInfo addFileLine(NdiScriptInfo.Type type,Path filePath, ReplaceString commentLine, String goodLine, boolean force, ReplaceString header) {
//        Pattern commentLineConditionPattern = Pattern.compile(commentLineConditionRegexp);
        filePath = filePath.toAbsolutePath();
        List<String> goodLinesList = Arrays.asList(goodLine.split("\n"));
        boolean alreadyExists = Files.exists(filePath);
        boolean found = false;
        boolean updatedFile = false;
        List<String> lines = new ArrayList<>();
        if (Files.isRegularFile(filePath)) {
            String fileContent = null;
            try {
                fileContent = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            String[] fileRows = fileContent.split("\n");
            if (header != null) {
                if (fileRows.length == 0 || !header.matches(fileRows[0].trim())) {
                    lines.add(header.getReplacement());
                    updatedFile = true;
                }
            }
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (isComments(row.trim()) && commentLine.matches(trimComments(row.trim()))) {
                    String clta = toCommentLine(commentLine.getReplacement());
                    if (!clta.equals(row)) {
                        updatedFile = true;
                    }
                    lines.add(clta);
                    found = true;
                    i++;
                    List<String> old = new ArrayList<>();
                    while (i < fileRows.length) {
                        i++;
                        if (fileRows[i].trim().isEmpty()) {
                            break;
                        } else if (fileRows[i].trim().startsWith("#")) {
                            i--;
                            break;
                        } else {
                            old.add(fileRows[i].trim());
                        }
                    }
                    lines.addAll(goodLinesList);
                    lines.add("");
                    if (!old.equals(goodLinesList)) {
                        updatedFile = true;
                    }
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (!found) {
            if (header != null && lines.isEmpty()) {
                lines.add(header.getReplacement());
            }
            lines.add(toCommentLine(commentLine.getReplacement()));
            lines.addAll(goodLinesList);
            lines.add("");
            updatedFile = true;
        }
        if (force || updatedFile) {
            try {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        return new PathInfo(
                type,filePath,
                alreadyExists?force? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED
        );
    }

    public PathInfo removeFileCommented2Lines(NdiScriptInfo.Type type, Path filePath, String commentLine, boolean force) {
        filePath = filePath.toAbsolutePath();
        boolean alreadyExists = Files.exists(filePath);
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("\n");
                for (int i = 0; i < fileRows.length; i++) {
                    String row = fileRows[i];
                    if (row.trim().equals(toCommentLine(commentLine))) {
                        found = true;
                        i += 2;
                        for (; i < fileRows.length; i++) {
                            lines.add(fileRows[i]);
                        }
                    } else {
                        lines.add(row);
                    }
                }
            }
            if (found) {
                updatedFile = true;
            }
            if (force || updatedFile) {
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, (String.join("\n", lines) + "\n").getBytes());
            }
            return new PathInfo(type, filePath, updatedFile ? alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract String getCallScriptCommand(String path);

    protected abstract String getExecFileName(String name);

    protected abstract String getTemplateBodyName();

    protected abstract String getTemplateNutsName();

    public NdiScriptInfo createScript(NdiScriptInfo.Type type, String name, String preferredName, NutsId fnutsId, boolean trace, String desc, Function<String, String> mapper) {
        try {
            Path script = getScriptFile(name);
            if (preferredName != null && preferredName.length() > 0) {
                if (preferredName.contains("%v")) {
                    preferredName = preferredName.replace("%v", fnutsId.getVersion().toString());
                }
                if (preferredName.contains("%n")) {
                    preferredName = preferredName.replace("%n", fnutsId.getArtifactId());
                }
                if (preferredName.contains("%g")) {
                    preferredName = preferredName.replace("%g", fnutsId.getGroupId());
                }
                script = Paths.get(preferredName);
            }
            if (script.getParent() != null) {
                if (!Files.exists(script.getParent())) {
                    Files.createDirectories(script.getParent());
                }
            }
            boolean _override = Files.exists(script);
            try (BufferedWriter w = Files.newBufferedWriter(script)) {
                NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, mapper);
            }
            NdiUtils.setExecutable(script);
            return new NdiScriptInfo(type, name, fnutsId, script, _override);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected NAdminConfig loadNadminConfig(String nutsVersion) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        if (Files.isRegularFile(t)) {
            return context.getWorkspace().elem().setContentType(NutsContentType.JSON).parse(t, NAdminConfig.class);
        }
        return null;
    }

    protected void saveNadminConfig(NAdminConfig config) {
        Path t = Paths.get(context.getWorkspace().locations().getStoreLocation(context.getAppId(), NutsStoreLocation.CONFIG))
                .resolve("nadmin-config.json");
        context.getWorkspace().elem().setContentType(NutsContentType.JSON).setCompact(false).setValue(config)
                .print(t);
    }

    @NotNull
    protected abstract FreeDesktopEntryWriter createFreeDesktopEntryWriter();

    public PathInfo[] configurePathShortcut(
            AppShortcutTarget appShortcutTarget,
            NutsId appId,
            String apiVersion,
            String preferredName,
            String menuPath,
            String iconPath,
            String cwd,
            String[] extraArgs) {
        List<PathInfo> results = new ArrayList<>();

        NutsWorkspace ws = context.getWorkspace();
        if (apiVersion == null) {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("missing nuts-api version to link to"));
        }
        NutsId apiId = ws.getApiId().builder().setVersion(apiVersion).build();
        NutsDefinition apiDefinition = ws.search().addId(apiId).setFailFast(true).setLatest(true).setContent(true).getResultDefinitions().singleton();

//            Path apiConfigFolder
//                    = bootConfig != null ? Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS))
//                    : Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
//            Path startNutsFile = apiConfigFolder.resolve(getExecFileName("start-nuts"));

        FreeDesktopEntryWriter ww = createFreeDesktopEntryWriter();
        FreeDesktopEntry fdi = new FreeDesktopEntry();
        FreeDesktopEntry.Group sl = fdi.getOrCreateDesktopEntry();
        NutsDefinition appDef = context.getWorkspace().search().addId(appId).setLatest(true).setEffective(true).getResultDefinitions().singleton();
        StringBuilder execCmd = new StringBuilder();

        execCmd.append("'"+getNutsStartPath().toString()+"'").append(" -y '").append(appId).append("'");
        if (extraArgs != null) {
            for (String extraArg : extraArgs) {
                execCmd.append(" '").append(extraArg).append("'");
            }
        }
        sl.setStartNotify(true);
        sl.setExec(execCmd.toString());
        if (cwd == null) {
            //should it be id's var folder?
            cwd = System.getProperty("user.home");
        }
        sl.setPath(cwd);
        sl.setIcon(iconPath==null? getDefaultIconPath() :iconPath);
        if (preferredName != null) {
            preferredName = preferredName.replace("%v", apiVersion);
        }
        if (preferredName == null) {
            if (appDef.getDescriptor().getName() != null) {
                preferredName = appDef.getDescriptor().getName() + "-" + appDef.getDescriptor().getId().getVersion();
            }
            if (preferredName == null) {
                preferredName = appDef.getDescriptor().getId().getArtifactId() + "-" + appDef.getDescriptor().getId().getVersion();
            }
        }
        sl.setName(preferredName);
        sl.setGenericName(preferredName);
        sl.setComment(appDef.getDescriptor().getDescription());
        if (appShortcutTarget == AppShortcutTarget.DESKTOP) {
            results.addAll(Arrays.asList(ww.writeDesktop(fdi, true)));
        } else if (appShortcutTarget == AppShortcutTarget.MENU) {
            results.addAll(Arrays.asList(ww.writeMenu(fdi, menuPath, true)));
        } else {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unsupported"));
        }
        return results.toArray(new PathInfo[0]);
    }

    @NotNull
    private String getDefaultIconPath() {
        return "apper";
    }

}
