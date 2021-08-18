package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.*;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    public static final ReplaceString COMMENT_LINE_CONFIG_HEADER = new ReplaceString("net.thevpc.nuts configuration", "((net[.]thevpc[.]nuts)|(net[.]thevpc[.]nuts.toolbox[.]ndi)|(net[.]vpc[.]app[.]nuts)) configuration");

    private static final Logger LOG = Logger.getLogger(BaseSystemNdi.class.getName());
    private static final Set<String> ACCEPTED_ICON_EXTENSIONS = new HashSet<>(Arrays.asList("png", "gif", "jpg", "jpeg", "svg"));

    public BaseSystemNdi(NutsApplicationContext appContext) {
        super(appContext);
    }

    public abstract String getBashrcName();

    public NdiScriptInfo getSysRC(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                String bashrcName = getBashrcName();
                if (bashrcName == null) {
                    return null;
                }
                return Paths.get(System.getProperty("user.home")).resolve(bashrcName);
            }

            @Override
            public PathInfo create() {
                Path apiConfigFile = path();
                if (apiConfigFile == null) {
                    return null;
                }
                return addFileLine(NdiScriptInfoType.SYS_RC,
                        env.getNutsApiId(),
                        apiConfigFile, getCommentLineConfigHeader(),
                        getCallScriptCommand(getNutsInit(env).path().toString()),
                        getShebanSh());
            }
        };
    }

    public NdiScriptInfo getNutsInit(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-init"));
            }

            @Override
            public PathInfo create() {
                Path apiConfigFile = path();
                return addFileLine(NdiScriptInfoType.NUTS_INIT,
                        env.getNutsApiId(),
                        apiConfigFile, getCommentLineConfigHeader(),
                        getCallScriptCommand(getNutsEnv(env).path().toString()) + newlineString() +
                                createNutsEnvString(env, false, true),
                        getShebanSh());
            }
        };
    }

    public NdiScriptInfo getNutsTermInit(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-term-init"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-term-init", NdiScriptInfoType.NUTS_TERM, env.getNutsApiId(), env)
                        .setPath(path())
                        .build();
            }
        };
    }

    public FromTemplateScriptBuilder scriptBuilderTemplate(String templateName, NdiScriptInfoType type, NutsId anyId, NutsEnvInfo env) {
        return ScriptBuilder.fromTemplate(templateName, type, anyId, BaseSystemNdi.this, env);
    }

    public SimpleScriptBuilder scriptBuilderSimple(NdiScriptInfoType type, NutsId anyId, NutsEnvInfo env) {
        return ScriptBuilder.simple(type, anyId, BaseSystemNdi.this)/*,env*/;
    }

    public NdiScriptInfo getNutsTerm(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getBinFolder().resolve(getExecFileName("nuts-term"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("nuts-term", NdiScriptInfoType.NUTS_TERM, env.getNutsApiId(), env)
                        .setPath(path())
                        .build();
            }
        };
    }


    public NdiScriptInfo getNutsEnv(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getIncFolder().resolve(getExecFileName(".nuts-env"));
            }

            @Override
            public PathInfo create() {
                return scriptBuilderTemplate("body", NdiScriptInfoType.NUTS_ENV, env.getNutsApiId(), env)
                        .setPath(path())
                        .println(createNutsEnvString(env, true, false))
                        .build();
            }
        };
    }

    public NdiScriptInfo getNutsStart(NutsEnvInfo env) {
        return new NdiScriptInfo() {
            @Override
            public Path path() {
                return env.getBinFolder().resolve(getExecFileName("nuts"));
            }

            @Override
            public PathInfo create() {
                return null;
            }
        };
    }


    //ws.getApiId().getVersion()


    public Path getScriptFile(String name, NutsEnvInfo env) {
        if (name.indexOf('/') >= 0) {
            return Paths.get(name).toAbsolutePath();
        }
        return env.getBinFolder().resolve(getExecFileName(name)).toAbsolutePath();
//        Path bin =
//                Paths.get(context.getAppsFolder());
//        return bin.resolve(getExecFileName(name)).toAbsolutePath();
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

    protected abstract String createNutsScriptContent(NutsId fnutsId, NdiScriptOptions options);

    @Override
    public PathInfo[] createArtifactScript(NdiScriptOptions options) {
        NutsId nid = context.getWorkspace().id().parser().parse(options.getId());
        List<PathInfo> r = new ArrayList<>();
        if (isNutsBootId(nid)) {
            r.addAll(Arrays.asList(
                    createBootScripts(options.copy().setId(options.getEnv().getNutsApiId().toString()))));
        } else {
            if (options.isAddNutsScript()) {
                r.addAll(
                        Arrays.asList(createBootScripts(options.copy().setId(options.getEnv().getNutsApiId().toString())))
                );
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
            Path ff = getScriptFile(n, options.getEnv());
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
                r.add(scriptBuilderTemplate("body", NdiScriptInfoType.ARTIFACT, nid, options.getEnv())
                        .setPath(options.getScriptPath())
                        .println(createNutsScriptContent(nid, options))
                        .build());
            }
            if (matchCondition(options.getCreateDesktop(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.DESKTOP, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getCreateShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.SHORTCUT))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.SHORTCUT, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getCreateMenu(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                r.addAll(Arrays.asList(createShortcut(NutsDesktopIntegrationItem.MENU, options.copy().setId(nid.toString()))));
            }
        }
        return r.toArray(new PathInfo[0]);
    }

    public NutsActionSupport getDesktopIntegrationSupport(NutsDesktopIntegrationItem target){
        return context.getWorkspace().env().getDesktopIntegrationSupport(target);
    }

    @Override
    public void removeNutsScript(String id, NutsSession session, NutsEnvInfo env) {
        NutsId nid = context.getWorkspace().id().parser().parse(id);
        Path f = getScriptFile(nid.getArtifactId(), env);
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

//    @Override
//    public void addNutsWorkspaceScript(String preferredScriptName, NutsEnvInfo env) {
//        PathInfo[] v = persistConfig(preferredScriptName, env, createDesktop, createMenu);
//        if (context.getSession().isPlainTrace()) {
//            context.getSession().out().printf("added script %s to point to %s %s\n", env.getWorkspaceLocation(), env.getNutsApiVersion(),
//                    Arrays.stream(v).map(x -> x.getPath()).toArray()
//            );
//        }
//    }

    @Override
    public PathInfo[] switchWorkspace(NdiScriptOptions options) {
        options = options.copy();
        options.setPersistentConfig(true);
        PathInfo[] v = createBootScripts(options);

        if (context.getSession().isPlainTrace()) {
            context.getSession().out().printf("```sh nuts``` switched to workspace %s to point to %s\n",
                    options.getEnv().getWorkspaceLocation(),
                    options.getEnv().getNutsApiVersion()
            );
        }
        return v;
    }

    @Override
    public boolean isNutsBootId(NutsId nid) {
        if ("nuts".equals(nid.getShortName()) || "net.thevpc.nuts:nuts".equals(nid.getShortName())) {
            return true;
        }
        return false;
    }

    public PathInfo[] createBootScripts(NdiScriptOptions options
//                                        String preferredName,
//                                        NutsEnvInfo env,
//                                        boolean global,
//                                        boolean desktop,
//                                        boolean menu,
//                                        boolean shortcut,
//                                        String fileName
    ) {
        String preferredName = null;
        List<PathInfo> all = new ArrayList<>();
        // create $nuts-api-app/.nutsenv
        all.add(getNutsEnv(options.getEnv()).create());
        // create $nuts-api-app/.nutsrc
        all.add(getNutsInit(options.getEnv()).create());

//        NutsDefinition apiDef = context.getWorkspace().search()
//                .addId(apiId).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
        Path script = null;
        script = getScriptFile(NameBuilder.id(options.getEnv().getNutsApiId(), options.getScriptPath(), "%n",
                options.getEnv().getNutsApiDef().getDescriptor(), context.getSession()).buildName(), options.getEnv());
        boolean createPath = false;
        if (Files.exists(script)) {
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
                    scriptBuilderTemplate("nuts", NdiScriptInfoType.NUTS, options.getEnv().getNutsApiId(), options.getEnv())
                            .setPath(script)
                            .build()
            );
        }
//        if (currId && standardPath) {
//            Path ff2 = Paths.get(context.getWorkspace().locations().getWorkspaceLocation())
//                    .resolve("nuts");
//            boolean overridden = Files.exists(ff2);
//            boolean gen = true;
//
//            if (!force && Files.exists(ff2)) {
//                if (!context.getSession().getTerminal().ask()
//                        .resetLine()
//                        .setSession(context.getSession())
//                        .forBoolean("override existing script %s ?",
//                                context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path()))
//                        .setDefaultValue(false)
//                        .getBooleanValue()) {
//                    gen = false;
//                }
//            }
//            if (gen) {
//
//                if (trace && context.getSession().isPlainTrace()) {
//                    context.getSession().out().printf((Files.exists(ff2) ? "re-install" : "install")
//                                    + " script %s %n",
//                            context.getWorkspace().text().forStyled(NdiUtils.betterPath(ff2.toString()), NutsTextStyle.path())
//                    );
//                }
//                try {
//                    try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
//                        NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + getTemplateBodyName(), w, x -> {
//                            switch (x) {
//                                case "NUTS_ID":
//                                    return env.getNutsApiId().toString();
//                                case "GENERATOR":
//                                    return context.getAppId().toString();
//                                case "BODY": {
//                                    return getCallScriptCommand(NdiUtils.replaceFilePrefix(scriptString, ff2.toString(), ""));
//                                }
//                            }
//                            return null;
//                        });
//                    }
//                } catch (IOException ex) {
//                    throw new UncheckedIOException(ex);
//                }
//                NdiUtils.setExecutable(ff2);
//                all.add(new PathInfo(
//                                NdiScriptInfo.Type.NUTS
//                        , env.getNutsApiId(), ff2, overridden ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED));
//            }
//        }

        all.addAll(Arrays.asList(getNutsTermInit(options.getEnv()).create()));
        all.addAll(Arrays.asList(getNutsTerm(options.getEnv()).create()));

        if (options.getPersistentConfig() != null && options.getPersistentConfig()) {
            // create $home/.bashrc
            PathInfo sysRC = getSysRC(options.getEnv()).create();
            if (sysRC != null) {
                all.add(sysRC);
            }
            if (matchCondition(options.getCreateDesktop(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem.DESKTOP, options.getEnv())));
            }
            if (matchCondition(options.getCreateMenu(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem.MENU, options.getEnv())));
            }
        } else {
            if (matchCondition(options.getCreateDesktop(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.DESKTOP, options.getEnv(), preferredName, options.getScriptPath())));
            }
            if (matchCondition(options.getCreateMenu(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.MENU, options.getEnv(), preferredName, options.getScriptPath())));
            }
            if (matchCondition(options.getCreateShortcut(), getDesktopIntegrationSupport(NutsDesktopIntegrationItem.SHORTCUT))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NutsDesktopIntegrationItem.SHORTCUT, options.getEnv(), preferredName, options.getScriptPath())));
            }
        }

        if (options.getPersistentConfig() != null && options.getPersistentConfig()
                && all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            onPostGlobal(options.getEnv(), all.toArray(new PathInfo[0]));
        }
        return all.toArray(new PathInfo[0]);
    }

    protected boolean matchCondition(NutsActionSupportCondition createDesktop, NutsActionSupport desktopIntegrationSupport){
        if(desktopIntegrationSupport==null){
            desktopIntegrationSupport=NutsActionSupport.UNSUPPORTED;
        }
        return desktopIntegrationSupport.acceptCondition(createDesktop,context.getSession());
    }

    @Override
    public PathInfo[] addScript(
            CreateNutsScriptCommand cmd,
            NutsApplicationContext context) {
        NutsWorkspace ws = context.getWorkspace();
        Path workspaceLocation = Paths.get(ws.locations().getWorkspaceLocation());
        List<PathInfo> result = new ArrayList<>();
        boolean subTrace = context.getSession().isTrace();
        if (!context.getSession().isPlainTrace()) {
            subTrace = false;
        }
        cmd.getOptions().setEnv(new DefaultNutsEnvInfo(
                null, cmd.getOptions().getSwitchWorkspaceLocation(),
                context.getSession()
        ));
        Boolean persistentConfig = cmd.getOptions().getPersistentConfig();
        if (!cmd.getIdsToInstall().isEmpty()) {
            if (persistentConfig == null) {
                if (workspaceLocation.equals(Paths.get(System.getProperty("user.home")).resolve(".config/nuts/default-workspace"))) {
                    persistentConfig = true;
                } else {
                    persistentConfig = false;
                }
            }
            boolean includeEnv = cmd.getOptions().isIncludeEnv();
            for (String id : cmd.getIdsToInstall()) {
                NutsId nid = ws.id().parser().parse(id);
                if (nid == null) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = cmd.getOptions().getScriptPath();
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

                    result.addAll(Arrays.asList(
                            createArtifactScript(
                                    cmd.getOptions().copy()
                                            .setId(id)
                                            .setScriptPath(linkNameCurrent)
                                            .setPersistentConfig(persistentConfig != null && persistentConfig)
                            )
                    ));
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                result.addAll(Arrays.asList(createBootScripts(
                        cmd.getOptions().copy()
                                .setId(cmd.getOptions().getEnv().getNutsApiId().toString())
                                .setScriptPath(linkNameCurrent)
                                .setPersistentConfig(persistentConfig != null && persistentConfig)
                )));
            }
            for (String id : nonNutsIds) {
                try {
                    NutsId nid = ws.id().parser().parse(id);
                    if (nid == null) {
                        throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to create script for %s : invalid id", id), 100);
                    }
                    result.addAll(Arrays.asList(
                            createArtifactScript(
                                    cmd.getOptions().copy()
                                            .setId(id)
                                            .setScriptPath(linkNameCurrent)
                                            .setPersistentConfig(persistentConfig != null && persistentConfig)
                                            .setIncludeEnv(includeEnv)
                            ))
                    );
                } catch (UncheckedIOException e) {
                    throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to add script for %s : %s", id, e), e);
                }
            }
//            result.addAll(Arrays.asList(configurePath(
//                    env
//                    , cmd.isCreateDesktop(), cmd.isCreateMenu(), persistentConfig, cmd.isCreateShortcut(), linkNameCurrent)));
        }
        return result.toArray(new PathInfo[0]);
    }

    public void onPostGlobal(NutsEnvInfo env, PathInfo[] updatedPaths) {

    }

    protected String newlineString() {
        return "\n";
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

    public String dblQte(String line) {
        return "\"" + line + "\"";
    }

    public String smpQte(String line) {
        return "'" + line + "'";
    }

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
    public List<String> splitLines(String text){
        ArrayList<String> lines = new ArrayList<>();
        if(text==null){
            return lines;
        }
        try(BufferedReader br=new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }catch (IOException ex){
            throw new UncheckedIOException(ex);
        }
        return lines;
    }

    public PathInfo addFileLine(NdiScriptInfoType type,
                                NutsId id,
                                Path filePath,
                                ReplaceString commentLine,
                                String contentToAdd,
                                ReplaceString header) {
//        Pattern commentLineConditionPattern = Pattern.compile(commentLineConditionRegexp);
        filePath = filePath.toAbsolutePath();
        List<String> contentToAddRows = splitLines(contentToAdd);
        boolean found = false;
        List<String> newFileContentRows = new ArrayList<>();
        List<String> oldFileContentRows=null;
        if (Files.isRegularFile(filePath)) {
            String fileContentString = null;
            try {
                fileContentString = new String(Files.readAllBytes(filePath));
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            oldFileContentRows = splitLines(fileContentString);
            //trim lines
            while (!oldFileContentRows.isEmpty()) {
                if (oldFileContentRows.get(0).trim().isEmpty()) {
                    oldFileContentRows.remove(0);
                } else if (oldFileContentRows.get(oldFileContentRows.size() - 1).trim().isEmpty()) {
                    oldFileContentRows.remove(oldFileContentRows.size() - 1);
                } else {
                    break;
                }
            }
            for (int i = 0; i < oldFileContentRows.size(); i++) {
                String row = oldFileContentRows.get(i);
                if (isComments(row.trim()) && commentLine.matches(trimComments(row.trim()))) {
                    String clta = toCommentLine(commentLine.getReplacement());
                    if (!clta.equals(row)) {
//                        updatedFile = true;
                    }
                    if (newFileContentRows.size() > 0) {
                        if (newFileContentRows.get(newFileContentRows.size() - 1).trim().length() > 0) {
                            newFileContentRows.add("");
                        }
                    }
                    newFileContentRows.add(clta);
                    found = true;
                    i++;
                    List<String> old = new ArrayList<>();
                    while (i < oldFileContentRows.size()) {
                        String s = oldFileContentRows.get(i);
                        if (s.trim().isEmpty()) {
                            i++;
                            break;
                        } else if (s.trim().startsWith("#")) {
                            break;
                        } else {
                            i++;
                            old.add(s.trim());
                        }
                    }
                    newFileContentRows.addAll(contentToAddRows);
                    newFileContentRows.add("");
//                    if (!old.equals(contentToAddRows)) {
//                    }
                    for (; i < oldFileContentRows.size(); i++) {
                        newFileContentRows.add(oldFileContentRows.get(i));
                    }
                } else {
                    newFileContentRows.add(row);
                }
            }
        }
        if (header != null) {
            if (newFileContentRows.size() == 0 || !header.matches(newFileContentRows.get(0).trim())) {
                newFileContentRows.add(0, header.getReplacement());
            }
        }
        if (!found) {
            if (newFileContentRows.size() > 0 && !newFileContentRows.get(0).trim().isEmpty()) {
                newFileContentRows.add("");
            }
            newFileContentRows.add(toCommentLine(commentLine.getReplacement()));
            newFileContentRows.addAll(contentToAddRows);
            newFileContentRows.add("");
        } else {
//            if (lines.size() > 0) {
//                if (lines.get(lines.size() - 1).trim().length() > 0) {
//                    lines.add("");
//                }
//            }
        }
        byte[] oldContent = NdiUtils.loadFile(filePath);
        String oldContentString = oldContent == null ? "" : new String(oldContent);
        byte[] newContent = (String.join(newlineString(), newFileContentRows)).getBytes();
        String newContentString = new String(newContent);
        PathInfo.Status s = NdiUtils.tryWriteStatus(newContent, filePath);
        return new PathInfo(type, id, filePath, NdiUtils.tryWrite(newContent, filePath));
    }

    public PathInfo removeFileCommented2Lines(NdiScriptInfoType type, NutsId id, Path filePath, String commentLine, boolean force) {
        filePath = filePath.toAbsolutePath();
        boolean alreadyExists = Files.exists(filePath);
        boolean found = false;
        boolean updatedFile = false;
        try {
            List<String> lines = new ArrayList<>();
            if (Files.isRegularFile(filePath)) {
                String fileContent = new String(Files.readAllBytes(filePath));
                String[] fileRows = fileContent.split("[\n\r]");
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
                Files.write(filePath, (String.join(newlineString(), lines) + newlineString()).getBytes());
            }
            return new PathInfo(type, id, filePath, updatedFile ? alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected abstract String getSetVarCommand(String name, String value);

    protected abstract String getSetVarStaticCommand(String name, String value);

    protected abstract String getCallScriptCommand(String path, String... args);

    protected abstract String getExecFileName(String name);

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

    protected abstract FreeDesktopEntryWriter createFreeDesktopEntryWriter();

    public PathInfo[] createShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NutsId id, String path, FreeDesktopEntry.Group shortcut) {
        List<PathInfo> results = new ArrayList<>();
        FreeDesktopEntryWriter ww = createFreeDesktopEntryWriter();
        if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.DESKTOP) {
            results.addAll(Arrays.asList(ww.writeDesktop(shortcut, path, true, id)));
        } else if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.MENU) {
            results.addAll(Arrays.asList(ww.writeMenu(shortcut, path, true, id)));
        } else if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.SHORTCUT) {
            results.addAll(Arrays.asList(ww.writeShortcut(shortcut, path == null ? null : Paths.get(path), true, id)));
        } else {
            throw new NutsIllegalArgumentException(context.getSession(), NutsMessage.cstyle("unsupported"));
        }
        return results.toArray(new PathInfo[0]);
    }

    /**
     * bigger is better
     *
     * @param extension extension
     * @return extension support order (bigger is better, 0 or less is ignored)
     */
    protected int resolveIconExtensionPriority(String extension) {
        extension = extension.toLowerCase();
        switch (extension) {
            case "svg":
                return 10;
            case "png":
                return 8;
            case "jpg":
                return 6;
            case "jpeg":
                return 5;
            case "gif":
                return 4;
            case "ico":
                return 3;
        }
        return -1;
    }

    protected int compareIconExtensions(String a, String b) {
        int ai = resolveIconExtensionPriority(a);
        int bi = resolveIconExtensionPriority(b);
        //bigger is first
        return Integer.compare(bi, ai);
    }

    protected int compareIconPaths(String a, String b) {
        String n1 = context.getWorkspace().io().path(a).getLastExtension();
        String n2 = context.getWorkspace().io().path(b).getLastExtension();
        return compareIconExtensions(n1, n2);
    }

    protected String resolveBestIcon(String... iconPaths) {
        if (iconPaths != null) {
            List<String> all = Arrays.stream(iconPaths).map(x -> (x == null) ? "" : x.trim())
                    .filter(x -> !x.isEmpty())
                    .filter(x ->
                            resolveIconExtensionPriority(context.getWorkspace().io().path(x).getLastExtension()) >= 0
                    )
                    .sorted(this::compareIconPaths).collect(Collectors.toList());
            if (all.size() > 0) {
                return all.get(0);
            }
        }
        return null;
    }

    public String resolveIcon(String iconPath, NutsId appId) {
        if (iconPath != null && iconPath.length() > 0) {
            return iconPath;
        }
        NutsWorkspace ws = context.getWorkspace();
        NutsDefinition appDef = ws.search().addId(appId).setLatest(true).setEffective(true).getResultDefinitions().singleton();
        String descAppIcon = resolveBestIcon(appDef.getDescriptor().getIcons());
        if (descAppIcon == null) {
            if (isNutsBootId(appDef.getId())
                    || appDef.getId().getGroupId().startsWith("net.thevpc.nuts")
            ) {
                //get icon from nadmin
                descAppIcon =
                        resolveBestIcon(
                                "nuts-resource://" + context.getAppId().getLongName() + "/net/thevpc/nuts/runtime/nuts.svg",
                                "nuts-resource://" + context.getAppId().getLongName() + "/net/thevpc/nuts/runtime/nuts.png",
                                "nuts-resource://" + context.getAppId().getLongName() + "/net/thevpc/nuts/runtime/nuts.ico"
                        );
            }
        }
        if (descAppIcon != null) {
            NutsPath p0 = ws.io().path(descAppIcon);
            if (descAppIcon.startsWith("classpath://")) {
                descAppIcon = "nuts-resource://" + appDef.getId().getLongName() + "" + descAppIcon.substring("classpath://".length() - 1);
            }
            String bestName = "icon." + p0.getLastExtension();
            Path localIconPath = Paths.get(ws.locations().getStoreLocation(appDef.getId(), NutsStoreLocation.APPS))
                    .resolve(".nuts")
                    .resolve(bestName);
            if (Files.isRegularFile(localIconPath)) {
                iconPath = localIconPath.toString();
            } else {
                NutsPath p = ws.io().path(descAppIcon);
                if (p.exists()) {
                    ws.io().copy()
                            .from(p)
                            .to(localIconPath)
                            .run();
                    iconPath = localIconPath.toString();
                }
            }
        }
        if (iconPath == null) {
            iconPath = getDefaultIconPath();
        }
        return iconPath;
    }

    public Path getScriptPath(NdiScriptOptions options) {
        NutsDefinition appDef = options.getSession().getWorkspace().search()
                .addId(options.getId())
                .setLatest(true)
                .setEffective(true).getResultDefinitions().singleton();
        String path = NameBuilder.id(
                options.getSession().getWorkspace().id().parser().parse(options.getId()),
                options.getScriptPath(), "%n", appDef.getDescriptor(), options.getSession()).buildName();
        return Paths.get(path);
    }

    public PathInfo[] createShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NdiScriptOptions options) {
        String apiVersion = options.getEnv().getNutsApiVersion().toString();
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

//        FreeDesktopEntry fdi = new FreeDesktopEntry();
        NutsId appId = options.getSession().getWorkspace().id().parser().parse(options.getId());
        NutsDefinition appDef = context.getWorkspace().search().addId(appId).setLatest(true).setEffective(true).getResultDefinitions().singleton();
        List<String> cmd=new ArrayList<>();

        cmd.add(getNutsStart(options.getEnv()).path().toString());
        cmd.add("-y");
        cmd.add(appId.toString());
        if (options.getAppArgs() != null) {
            for (String extraArg : options.getAppArgs()) {
                cmd.add(extraArg);
            }
        }
        String cwd = options.getWorkingDirectory();
        if (cwd == null) {
            //should it be id's var folder?
            cwd = System.getProperty("user.home");
        }
        String iconPath = resolveIcon(options.getIcon(), appId);

        String shortcutName = options.getShortcutName();
        if (shortcutName == null) {
            if (nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.SHORTCUT) {
                shortcutName = options.getShortcutPath();
                if (shortcutName == null) {
                    shortcutName = options.getScriptPath();
                }
            }
        }
        shortcutName = NameBuilder.extractPathName(shortcutName);
        if (shortcutName.isEmpty() && nutsDesktopIntegrationItem == NutsDesktopIntegrationItem.DESKTOP) {
            shortcutName = "%N";
        }
        shortcutName += "%s%v%s%h";
        shortcutName = NameBuilder.label(appDef.getId(), shortcutName, null, appDef.getDescriptor(), context.getSession()).buildName();
        String execCmd=context.getSession().getWorkspace().commandLine().create(cmd.toArray(new String[0])).toString();
        FreeDesktopEntry.Group sl = FreeDesktopEntry.Group.desktopEntry(shortcutName, execCmd, cwd);
        sl.setStartNotify(true);
        sl.setIcon(iconPath);
        sl.setGenericName(apiDefinition.getDescriptor().getGenericName());
        sl.setComment(appDef.getDescriptor().getDescription());
        sl.setTerminal(options.isTerminalMode());
        if (options.getMenuCategory() != null) {
            sl.addCategory(options.getMenuCategory());
        }
        String preferredPath = NameBuilder.id(appDef.getId(), getScriptPath(options).toString(), null, appDef.getDescriptor(), context.getSession()).buildName();
        return createShortcut(nutsDesktopIntegrationItem, appId, preferredPath, sl);
    }

    protected String getDefaultIconPath() {
        return "apper";
    }


    public PathInfo[] createLaunchTermShortcutGlobal(NutsDesktopIntegrationItem nutsDesktopIntegrationItem, NutsEnvInfo env) {
        String fileName = env.getNutsApiId().getShortName().replace(':', '-');
        String name = "Nuts Terminal";
        return createLaunchTermShortcut(nutsDesktopIntegrationItem, env, name, fileName);
    }

    public PathInfo[] createLaunchTermShortcut(NutsDesktopIntegrationItem nutsDesktopIntegrationItem,
                                               NutsEnvInfo env,
                                               String name,
                                               String fileName
    ) {
        String cmd = getNutsTerm(env).path().toString();
        fileName = NameBuilder.id(env.getNutsApiId(), fileName, name, env.getNutsApiDef().getDescriptor(), context.getSession())
                .buildName();
        if (name == null) {
            name = NameBuilder.label(env.getNutsApiId(), "Nuts Terminal%s%v%s%h", null, env.getNutsApiDef().getDescriptor(), context.getSession())
                    .buildName();
        }
        String execCmd=context.getSession().getWorkspace().commandLine().create(cmd).toString();
        return createShortcut(nutsDesktopIntegrationItem,
                env.getNutsApiId(),
                fileName,
                FreeDesktopEntry.Group.desktopEntry(name, execCmd, System.getProperty("user.home"))
                        .setIcon(resolveIcon(null, env.getNutsApiId()))
                        .setStartNotify(true)
                        .addCategory("/Utilities/Nuts")
                        .setGenericName(env.getNutsApiDef().getDescriptor().getGenericName())
                        .setComment(env.getNutsApiDef().getDescriptor().getDescription())
                        .setTerminal(true)
        );
    }


    public String createNutsEnvString(NutsEnvInfo env, boolean updateEnv, boolean updatePATH) {
        final NutsWorkspace ws = context.getWorkspace();
        String NUTS_JAR_PATH = ws.search()
                .setSession(context.getSession().copy().setTrace(false))
                .addId(ws.getApiId()).getResultPaths().required();

        /**
         * "#!/bin/sh\n" +
         *                         "# This File is generated by nuts nadmin companion tool.\n" +
         *                         "# Do not edit it manually. All changes will be lost when nadmin runs again\n" +
         *                         "# This file aims to prepare bash environment against current nuts\n" +
         *                         "# workspace installation.\n" +
         *                         "#\n"
         */
        TreeSet<String> exports = new TreeSet<>();
        SimpleScriptBuilder tmp = scriptBuilderSimple(NdiScriptInfoType.NUTS_ENV, env.getNutsApiId(), env);
        if (updateEnv) {
            exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_JAR", "NUTS_WORKSPACE_BINDIR"));
            tmp.printSetStatic("NUTS_VERSION", ws.getApiVersion().toString());
            tmp.printSetStatic("NUTS_WORKSPACE", ws.locations().getWorkspaceLocation().toString());
            for (NutsStoreLocation value : NutsStoreLocation.values()) {
                tmp.printSetStatic("NUTS_WORKSPACE_" + value, ws.locations().getStoreLocation(value));
                exports.add("NUTS_WORKSPACE_" + value);
            }
            if (NUTS_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
                String pp = NUTS_JAR_PATH.substring(ws.locations().getStoreLocation(NutsStoreLocation.LIB).length());
                tmp.printSet("NUTS_JAR", varRef("NUTS_WORKSPACE_LIB") + pp);
            } else {
                tmp.printSetStatic("NUTS_JAR", NUTS_JAR_PATH);
            }
            String p0 = env.getBinFolder().toString().substring(
                    ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
            );
            tmp.printSet("NUTS_WORKSPACE_BINDIR", varRef("NUTS_WORKSPACE_APPS") + p0);
        }
        if (updatePATH) {
            exports.add("PATH");
            tmp.printSet("PATH", varRef("NUTS_WORKSPACE_BINDIR") + getPathVarSep() + varRef("PATH"));
        }
        tmp.println("export " + String.join(" ", exports));
        return tmp.buildString();
    }

    protected abstract ReplaceString getShebanSh();

    protected abstract ReplaceString getCommentLineConfigHeader();

    protected abstract String getTemplateName(String name);

    protected abstract String varRef(String v);

    public String getPathVarSep() {
        return System.getProperty("path.separator");
    }

}
