package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.shell.NShellHelper;
import net.thevpc.nuts.runtime.standalone.shell.ReplaceString;
import net.thevpc.nuts.runtime.standalone.shell.ScriptBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.*;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.FromTemplateScriptBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.SimpleScriptBuilder;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseSystemNdi extends AbstractSystemNdi {
    public static final ReplaceString COMMENT_LINE_CONFIG_HEADER = new ReplaceString("net.thevpc.nuts configuration",
            "((net[.]thevpc[.]nuts)" +
                    // TODO should remove this (was relevant in pre 0.8.0)
                    "|(net[.]thevpc[.]nuts.toolbox[.]ndi)" +
                    // TODO should remove this (was relevant in pre 0.8.0)
                    "|(net[.]vpc[.]app[.]nuts)) configuration"
    );

    public BaseSystemNdi() {
        super();
    }

    public NdiScriptInfo[] getSysRC(NdiScriptOptions options) {
        List<NdiScriptInfo> scriptInfos = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        for (NShellFamily sf : NWorkspace.of().getShellFamilies()) {
            String z = NShellHelper.of(sf).getSysRcName();
            if (!visited.contains(z)) {
                visited.add(z);
                NdiScriptInfo i = new RcNdiScriptInfo(z, options, sf);
                scriptInfos.add(i);
            }
        }
        return scriptInfos.toArray(new NdiScriptInfo[0]);
    }

    public NdiScriptInfo[] getIncludeNutsInit(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsInit(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public NdiScriptInfo getIncludeNutsInit(NdiScriptOptions options, NShellFamily shellFamily) {
        switch (shellFamily) {
            case SH:
            case BASH:
            case CSH:
            case KSH:
            case ZSH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.sh");
                    }

                    @Override
                    public PathInfo create() {
                        NPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NShellFamily.SH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(BaseSystemNdi.this);
                    }
                };
            }
            case FISH: {
                return new NdiScriptInfo() {
                    @Override
                    public NPath path() {
                        return options.resolveIncFolder().resolve(".nuts-init.fish");
                    }

                    @Override
                    public PathInfo create() {
                        NPath apiConfigFile = path();
                        return scriptBuilderTemplate("nuts-init", NShellFamily.FISH, "nuts-init", options.resolveNutsApiId(), options)
                                .setPath(apiConfigFile)
                                .buildAddLine(BaseSystemNdi.this);
                    }
                };
            }
        }
        return null;
    }

    public NdiScriptInfo[] getIncludeNutsTermInit(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsTermInit(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    protected abstract NShellFamily[] getShellGroups();

    public abstract NdiScriptInfo getIncludeNutsTermInit(NdiScriptOptions options, NShellFamily shellFamily);

    public FromTemplateScriptBuilder scriptBuilderTemplate(String templateName, NShellFamily shellFamily, String type, NId anyId, NdiScriptOptions options) {
        return ScriptBuilder.fromTemplate(templateName, shellFamily, type, anyId, BaseSystemNdi.this, options);
    }

    public SimpleScriptBuilder scriptBuilderSimple(NShellFamily shellFamily, String type, NId anyId, NdiScriptOptions options) {
        return ScriptBuilder.simple(shellFamily, type, anyId, BaseSystemNdi.this)/*,options*/;
    }

    public NdiScriptInfo[] getNutsTerm(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getNutsTerm(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public abstract NdiScriptInfo getNutsTerm(NdiScriptOptions options, NShellFamily shellFamily);


    public NdiScriptInfo[] getIncludeNutsEnv(NdiScriptOptions options) {
        return Arrays.stream(getShellGroups())
                .map(x -> getIncludeNutsEnv(options, x))
                .filter(Objects::nonNull)
                .toArray(NdiScriptInfo[]::new);
    }

    public abstract NdiScriptInfo getIncludeNutsEnv(NdiScriptOptions options, NShellFamily shellFamily);

    public NdiScriptInfo getNutsStart(NdiScriptOptions options) {
        return new NdiScriptInfo() {
            @Override
            public NPath path() {
                return options.resolveBinFolder().resolve(getExecFileName("nuts"));
            }

            @Override
            public PathInfo create() {
                return null;
            }
        };
    }


    //ws.getApiId().getVersion()


    public NPath getBinScriptFile(String name, NdiScriptOptions options) {
        NPath pp = NPath.of(name);
        if (!pp.isName()) {
            return pp.toAbsolute();
        }
        return options.resolveBinFolder().resolve(getExecFileName(name)).toAbsolute();
//        Path bin =
//                Paths.get(context.getAppsFolder());
//        return bin.resolve(getExecFileName(name)).toAbsolutePath();
    }

    protected abstract String createNutsScriptContent(NId fnutsId, NdiScriptOptions options, NShellFamily shellFamily);

    @Override
    public PathInfo[] createArtifactScript(NdiScriptOptions options) {
        NId nid = NId.get(options.getId()).get();
        List<PathInfo> r = new ArrayList<>();
        if (isNutsBootId(nid)) {
            r.addAll(Arrays.asList(
                    createBootScripts(options.copy().setId(options.resolveNutsApiId().toString()))));
        } else {
            if (options.isAddNutsScript()) {
                r.addAll(
                        Arrays.asList(createBootScripts(options.copy().setId(options.resolveNutsApiId().toString())))
                );
            }
            NDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = NSearchCmd.of()
                        .addId(options.getId()).setLatest(true).getResultDefinitions().findFirst().get();
                nid = fetched.getId().getShortId();
                //nutsId=fetched.getId().getLongNameId();
            }
            String n = nid.getArtifactId();
            NPath ff = getBinScriptFile(n, options);
            {
                String s = options.getLauncher().getCustomScriptPath();
                if (NBlankable.isBlank(s)) {
                    NDefinition appDef = loadIdDefinition(nid);
                    s = NameBuilder.id(appDef.getId(), "%n", null, appDef.getDescriptor()).buildName();
                    s = getBinScriptFile(s, options).toString();
                } else if (NPath.of(s).isName()) {
                    NDefinition appDef = loadIdDefinition(nid);
                    s = NameBuilder.id(appDef.getId(), s, null, appDef.getDescriptor()).buildName();
                    s = getBinScriptFile(s, options).toString();
                } else {
                    NDefinition appDef = loadIdDefinition(nid);
                    s = s + File.separator + NameBuilder.id(appDef.getId(), getExecFileName("%n"), null, appDef.getDescriptor()).buildName();
                }
                NShellFamily shellFamily = getShellGroups()[0];
                r.add(scriptBuilderTemplate("body", shellFamily, "artifact", nid, options)
                        .setPath(s)
                        .println(createNutsScriptContent(nid, options, shellFamily))
                        .build());
            }
            if (matchCondition(options.getLauncher().getCreateDesktopLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP))) {
                r.addAll(Arrays.asList(createShortcut(NDesktopIntegrationItem.DESKTOP, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getLauncher().getCreateUserLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.USER))) {
                r.addAll(Arrays.asList(createShortcut(NDesktopIntegrationItem.USER, options.copy().setId(nid.toString()))));
            }
            if (matchCondition(options.getLauncher().getCreateMenuLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU))) {
                r.addAll(Arrays.asList(createShortcut(NDesktopIntegrationItem.MENU, options.copy().setId(nid.toString()))));
            }
        }
        return r.toArray(new PathInfo[0]);
    }

    @Override
    public void removeNutsScript(String id, String switchWorkspaceLocation) {
        NdiScriptOptions options = new NdiScriptOptions();
        options.getLauncher().setSwitchWorkspaceLocation(switchWorkspaceLocation);
        NId nid = NId.get(id).get();
        NPath f = getBinScriptFile(nid.getArtifactId(), options);
        NTexts factory = NTexts.of();
        if (f.isRegularFile()) {
            if (NAsk.of()
                    .forBoolean(NMsg.ofC("tool %s will be removed. Confirm?",
                            factory.ofStyled(CoreIOUtils.betterPath(f.toString()), NTextStyle.path())
                    ))
                    .setDefaultValue(true)
                    .getBooleanValue()) {
                f.delete();
                NSession session = NSession.of();
                if (session.isPlainTrace()) {
                    NOut.println(NMsg.ofC("tool %s removed.", factory.ofStyled(CoreIOUtils.betterPath(f.toString()), NTextStyle.path())));
                }
            }
        }
    }

    @Override
    public PathInfo[] switchWorkspace(NdiScriptOptions options) {
        options = options.copy();
        options.getLauncher().setSwitchWorkspace(true);
        PathInfo[] v = createBootScripts(options);
        NSession session = NSession.of();
        if (session.isPlainTrace()) {
            NOut.println(NMsg.ofC("```sh nuts``` switched to workspace %s to point to %s",
                    options.getWorkspaceLocation(),
                    options.getNutsApiVersion()
            ));
        }
        return v;
    }

    @Override
    public boolean isNutsBootId(NId nid) {
        return
                NConstants.Ids.NUTS_API_ARTIFACT_ID.equals(nid.getShortName())
                ||
                NConstants.Ids.NUTS_APP_ARTIFACT_ID.equals(nid.getShortName())
                ||
                NConstants.Ids.NUTS_API.equals(nid.getShortName())
                ||
                NConstants.Ids.NUTS_APP.equals(nid.getShortName())
                ;
    }

    @Override
    public PathInfo[] addScript(NdiScriptOptions options, String[] all) {
        List<String> idsToInstall = Arrays.asList(all);
        NSession session = NSession.of();
        Path workspaceLocation = NWorkspace.of().getWorkspaceLocation().toPath().get();
        List<PathInfo> result = new ArrayList<>();
        Boolean systemWideConfig = options.getLauncher().getSwitchWorkspace();
        if (!idsToInstall.isEmpty()) {
            if (systemWideConfig == null) {
                systemWideConfig = workspaceLocation.toString().equals(NPlatformHome.of(NOsFamily.getCurrent()).getWorkspaceLocation(null));
            }
            boolean includeEnv = options.isIncludeEnv();
            for (String id : idsToInstall) {
                NId nid = NId.get(id).get();
                if (nid == null) {
                    throw new NExecutionException(NMsg.ofC("unable to create script for %s : invalid id", id), NExecutionException.ERROR_1);
                }
                if (!nid.getVersion().isBlank()) {
                    includeEnv = true;
                }
            }
            String linkNameCurrent = options.getLauncher().getCustomScriptPath();
//            if (includeEnv) {
//                linkNameCurrent = prepareLinkName(linkNameCurrent);
//            }
            List<String> nutsIds = idsToInstall.stream().filter(x -> isNutsBootId(NId.get(x).get())).collect(Collectors.toList());
            List<String> nonNutsIds = idsToInstall.stream().filter(x -> !isNutsBootId(NId.get(x).get())).collect(Collectors.toList());
            boolean bootAlreadyProcessed = false;
            for (String id : nutsIds) {
                try {
                    NId nid = NId.get(id).get();
                    bootAlreadyProcessed = true;
                    if (!nid.getVersion().isBlank()) {
                        String verString = nid.getVersion().toString();
                        if (verString.equalsIgnoreCase("current")
                                || verString.equalsIgnoreCase("curr")) {
                            id = nid.builder().setVersion(session.getWorkspace().getApiId().getVersion()).build().toString();
                        }
                    }

                    NdiScriptOptions oo = options.copy().setId(id);
                    oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                    oo.getLauncher().setSwitchWorkspace(systemWideConfig != null && systemWideConfig);

                    result.addAll(Arrays.asList(createArtifactScript(oo)));
                } catch (UncheckedIOException | NIOException e) {
                    throw new NExecutionException(NMsg.ofC("unable to add launcher for %s : %s", id, e), e);
                }
            }
            if (!bootAlreadyProcessed && !nonNutsIds.isEmpty()) {
                NdiScriptOptions oo = options.copy()
                        .setId(options.resolveNutsApiId().toString());
                oo.getLauncher().setCustomScriptPath(null);//reset script path!
                oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                oo.getLauncher().setSwitchWorkspace(systemWideConfig != null && systemWideConfig);
                result.addAll(Arrays.asList(createBootScripts(oo)));
            }
            for (String id : nonNutsIds) {
                try {
                    NId nid = NId.get(id).get();
                    if (nid == null) {
                        throw new NExecutionException(NMsg.ofC("unable to create script for %s : invalid id", id), NExecutionException.ERROR_1);
                    }
                    NdiScriptOptions oo = options.copy()
                            .setId(id);
                    oo.getLauncher().setCustomScriptPath(linkNameCurrent);
                    oo.getLauncher().setSwitchWorkspace(systemWideConfig != null && systemWideConfig);
                    oo.setIncludeEnv(includeEnv);
                    result.addAll(Arrays.asList(createArtifactScript(oo)));
                } catch (UncheckedIOException | NIOException e) {
                    throw new NExecutionException(NMsg.ofC("unable to add launcher for %s : %s", id, e), e);
                }
            }
//            result.addAll(Arrays.asList(configurePath(
//                    env
//                    , cmd.isCreateDesktop(), cmd.isCreateMenu(), systemWideConfig, cmd.isCreateShortcut(), linkNameCurrent)));
        }
        return result.toArray(new PathInfo[0]);
    }

    public PathInfo[] createBootScripts(NdiScriptOptions options) {
        String preferredName = options.getLauncher().getShortcutName();
        List<PathInfo> all = new ArrayList<>();

        // create $nuts-api-app/.nutsenv
        for (NdiScriptInfo i : getIncludeNutsEnv(options)) {
            all.add(i.create());
        }

        // create $nuts-api-app/.nutsrc
        for (NdiScriptInfo i : getIncludeNutsInit(options)) {
            all.add(i.create());
        }

        String scriptPath = options.getLauncher().getCustomScriptPath();
        all.add(scriptBuilderTemplate("nuts", getShellGroups()[0], "nuts", options.resolveNutsApiId(), options)
                .setPath(getBinScriptFile(NameBuilder.id(options.resolveNutsApiId(), scriptPath, "%n",
                        options.resolveNutsApiDef().getDescriptor()).buildName(), options))
                .build());
        for (NdiScriptInfo i : getIncludeNutsTermInit(options)) {
            all.add(i.create());
        }
        for (NdiScriptInfo i : getNutsTerm(options)) {
            all.add(i.create());
        }

        if (options.getLauncher().getSwitchWorkspace() != null && options.getLauncher().getSwitchWorkspace()) {
            // create $home/.bashrc
            //PathInfo sysRC = getSysRC(options).create();

            //  if (sysRC != null) {
            //    all.add(sysRC);
            //}
            for (NdiScriptInfo ndiScriptInfo : getSysRC(options)) {
                PathInfo sysRC = ndiScriptInfo.create();
                if (sysRC != null) {
                    all.add(sysRC);
                }

            }
            if (matchCondition(options.getLauncher().getCreateDesktopLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NDesktopIntegrationItem.DESKTOP, options)));
            }
            if (matchCondition(options.getLauncher().getCreateMenuLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcutGlobal(NDesktopIntegrationItem.MENU, options)));
            }
        } else {
            if (matchCondition(options.getLauncher().getCreateDesktopLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.DESKTOP))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NDesktopIntegrationItem.DESKTOP, options, scriptPath, preferredName)));
            }
            if (matchCondition(options.getLauncher().getCreateMenuLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.MENU))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NDesktopIntegrationItem.MENU, options, scriptPath, preferredName)));
            }
            if (matchCondition(options.getLauncher().getCreateUserLauncher(), getDesktopIntegrationSupport(NDesktopIntegrationItem.USER))) {
                all.addAll(Arrays.asList(createLaunchTermShortcut(NDesktopIntegrationItem.USER, options, scriptPath, preferredName)));
            }
        }

        if (options.getLauncher().getSwitchWorkspace() != null && options.getLauncher().getSwitchWorkspace()
                && all.stream().anyMatch(x -> x.getStatus() != PathInfo.Status.DISCARDED)) {
            onPostGlobal(options, all.toArray(new PathInfo[0]));
        }
        return all.toArray(new PathInfo[0]);
    }

    private NDefinition loadIdDefinition(NId nid) {
        return NSearchCmd.of().addId(nid).setLatest(true).setEffective(true).setDistinct(true).getResultDefinitions().findSingleton().get();
    }

    public NSupportMode getDesktopIntegrationSupport(NDesktopIntegrationItem target) {
        return NWorkspace.of().getDesktopIntegrationSupport(target);
    }

    protected boolean matchCondition(NSupportMode createDesktop, NSupportMode desktopIntegrationSupport) {
        if (desktopIntegrationSupport == null) {
            desktopIntegrationSupport = NSupportMode.NEVER;
        }
        return desktopIntegrationSupport.acceptCondition(createDesktop);
    }

    public void onPostGlobal(NdiScriptOptions options, PathInfo[] updatedPaths) {

    }

    public NWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        NWorkspaceBootConfig bootConfig = NWorkspace.of().loadBootConfig(switchWorkspaceLocation, false, true);
        if (bootConfig == null) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid workspace: %s", switchWorkspaceLocation));
        }
        return bootConfig;
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
            throw new NIOException(ex);
        }
    }

    public List<String> splitLines(String text) {
        ArrayList<String> lines = new ArrayList<>();
        if (text == null) {
            return lines;
        }
        try (BufferedReader br = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return lines;
    }

    public PathInfo addFileLine(String type,
                                NId id,
                                NPath filePath,
                                ReplaceString commentLine,
                                String contentToAdd,
                                ReplaceString header, NShellFamily shellFamily) {
//        Pattern commentLineConditionPattern = Pattern.compile(commentLineConditionRegexp);
        filePath = filePath.toAbsolute();
        List<String> contentToAddRows = splitLines(contentToAdd);
        boolean found = false;
        List<String> newFileContentRows = new ArrayList<>();
        List<String> oldFileContentRows = null;
        NShellHelper sh = NShellHelper.of(shellFamily);
        if (filePath.isRegularFile()) {
            String fileContentString = new String(filePath.readBytes());
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
                if (sh.isComments(row.trim()) && commentLine.matches(sh.trimComments(row.trim()))) {
                    String clta = sh.toCommentLine(commentLine.getReplacement());
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
            newFileContentRows.add(sh.toCommentLine(commentLine.getReplacement()));
            newFileContentRows.addAll(contentToAddRows);
            newFileContentRows.add("");
        } else {
//            if (lines.size() > 0) {
//                if (lines.get(lines.size() - 1).trim().length() > 0) {
//                    lines.add("");
//                }
//            }
        }
//        byte[] oldContent = NdiUtils.loadFile(filePath);
//        String oldContentString = oldContent == null ? "" : new String(oldContent);
        byte[] newContent = (String.join(sh.newlineString(), newFileContentRows)).getBytes();
//        String newContentString = new String(newContent);
//        PathInfo.Status s = NdiUtils.tryWriteStatus(newContent, filePath,session);
        return new PathInfo(type, id, filePath, CoreIOUtils.tryWrite(newContent, filePath, "UpdateScript"));
    }

    public PathInfo removeFileCommented2Lines(String type, NId id, NPath filePath, String commentLine, boolean force, NShellFamily shellFamily) {
        filePath = filePath.toAbsolute();
        boolean alreadyExists = filePath.exists();
        boolean found = false;
        boolean updatedFile = false;
        NShellHelper sh = NShellHelper.of(shellFamily);

        List<String> lines = new ArrayList<>();
        if (filePath.isRegularFile()) {
            String fileContent = new String(filePath.readBytes());
            String[] fileRows = fileContent.split("[\n\r]");
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals(sh.toCommentLine(commentLine))) {
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
            filePath.mkParentDirs();
            filePath.writeString((String.join(sh.newlineString(), lines) + sh.newlineString()));
        }
        return new PathInfo(type, id, filePath, updatedFile ? alreadyExists ? PathInfo.Status.OVERRIDDEN : PathInfo.Status.CREATED : PathInfo.Status.DISCARDED);
    }

    protected abstract String getExecFileName(String name);

    protected abstract FreeDesktopEntryWriter createFreeDesktopEntryWriter();

    public PathInfo[] createShortcut(NDesktopIntegrationItem nDesktopIntegrationItem, NId id, String path, FreeDesktopEntry.Group shortcut) {
        List<PathInfo> results = new ArrayList<>();
        FreeDesktopEntryWriter ww = createFreeDesktopEntryWriter();
        if (nDesktopIntegrationItem == NDesktopIntegrationItem.DESKTOP) {
            results.addAll(Arrays.asList(ww.writeDesktop(shortcut, path, true, id)));
        } else if (nDesktopIntegrationItem == NDesktopIntegrationItem.MENU) {
            results.addAll(Arrays.asList(ww.writeMenu(shortcut, path, true, id)));
        } else if (nDesktopIntegrationItem == NDesktopIntegrationItem.USER) {
            results.addAll(Arrays.asList(ww.writeShortcut(shortcut, path == null ? null : NPath.of(path), true, id)));
        } else {
            throw new NIllegalArgumentException(NMsg.ofPlain("unsupported"));
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
        String n1 = NPath.of(a).getNameParts(NPathExtensionType.SHORT).getExtension();
        String n2 = NPath.of(b).getNameParts(NPathExtensionType.SHORT).getExtension();
        return compareIconExtensions(n1, n2);
    }

    protected String resolveBestIcon(NId appId, List<String> iconPaths) {
        iconPaths=toAbsoluteIconPaths(appId,iconPaths);
        if (iconPaths != null) {
            List<String> all = iconPaths.stream().map(x -> (x == null) ? "" : x.trim())
                    .filter(x -> !x.isEmpty())
                    .filter(x ->
                            resolveIconExtensionPriority(NPath.of(x).getNameParts(NPathExtensionType.SHORT).getExtension()) >= 0
                    )
                    .sorted(this::compareIconPaths).collect(Collectors.toList());
            if (all.size() > 0) {
                return all.get(0);
            }
        }
        return null;
    }

    public String resolveIcon(String iconPath, NId appId) {
        if (!NBlankable.isBlank(iconPath)) {
            return iconPath;
        }
        return getPreferredIconPath(appId);
    }

    public List<String> toAbsoluteIconPaths(NId appId, List<String> iconPaths) {
        if(iconPaths==null){
            return null;
        }
        return iconPaths.stream().map(x->toAbsoluteIconPath(appId,x)).collect(Collectors.toList());
    }

    public String toAbsoluteIconPath(NId appId, String iconPath) {
        if (NBlankable.isBlank(iconPath)) {
            return iconPath;
        }
        if (iconPath.startsWith("classpath://")) {
            return "resource://" + appId.getLongName() + "" + iconPath.substring("classpath://".length() - 1);
        }
        return iconPath;
    }

    public String getPreferredIconPath(NId appId) {
        if (CoreNIdUtils.isApiId(appId)) {
            //apiId does not define any icon, will load icon from the runtime
            NId rt = CoreNIdUtils.findRuntimeForApi(appId.getVersion().getValue());
            if (rt == null) {
                rt = NWorkspace.of().getRuntimeId();
            }
            return getPreferredIconPath(rt);
        }
        NDefinition appDef = NSearchCmd.of().addId(appId).setLatest(true).setEffective(true).setDistinct(true).getResultDefinitions()
                .findSingleton().get();
        String descAppIcon = resolveBestIcon(appDef.getId(),appDef.getDescriptor().getIcons());
        if (descAppIcon == null) {
            if (isNutsBootId(appDef.getId())
                    || appDef.getId().getGroupId().equals("net.thevpc.nuts")
                    || appDef.getId().getGroupId().startsWith("net.thevpc.nuts.")
            ) {
                //get default icon
                NId rid = NWorkspace.of().getRuntimeId();
                descAppIcon =
                        resolveBestIcon(rid,
                                Arrays.asList(
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts.svg",
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts.png",
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts.ico"
                                )
                        );
            } else if (appDef.getId().getGroupId().startsWith("net.thevpc.nuts")) {
                //get default icon
                NId rid = NWorkspace.of().getRuntimeId();
                descAppIcon =
                        resolveBestIcon(rid,
                                Arrays.asList(
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts-app.svg",
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts-app.png",
                                "resource://" + rid.getLongName() + "/net/thevpc/nuts/runtime/nuts-app.ico"
                                )
                        );
            }
        }
        String iconPath = null;
        if (descAppIcon != null) {
            String descAppIcon0 = descAppIcon;
            String descAppIconDigest = NDigest.of().md5().setSource(new ByteArrayInputStream(descAppIcon0.getBytes())).computeString();
            NPath p0 = NPath.of(descAppIcon);
            descAppIcon=toAbsoluteIconPath(appId, descAppIcon);
            String bestName = descAppIconDigest + "." + p0.getNameParts(NPathExtensionType.SHORT).getExtension();
            NPath localIconPath = NWorkspace.of().getStoreLocation(appDef.getId(), NStoreType.CACHE)
                    .resolve("icons")
                    .resolve(bestName);
            if (localIconPath.isRegularFile()) {
                iconPath = localIconPath.toString();
            } else {
                NPath p = NPath.of(descAppIcon);
                if (p.exists()) {
                    NCp.of()
                            .from(p)
                            .to(localIconPath).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE)
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

    public Path getShortcutPath(NdiScriptOptions options) {
        NDefinition appDef = NSearchCmd.of()
                .addId(options.getId())
                .setLatest(true)
                .setEffective(true)
                .setDistinct(true)
                .getResultDefinitions()
                .findSingleton().get();

        String fileName = options.getLauncher().getCustomScriptPath();
        fileName = resolveShortcutFileName(appDef.getId(), appDef.getDescriptor(), fileName, null);
        return Paths.get(fileName);
    }

    public PathInfo[] createShortcut(NDesktopIntegrationItem nDesktopIntegrationItem, NdiScriptOptions options) {
        String apiVersion = options.getNutsApiVersion().toString();
        NAssert.requireNonBlank(apiVersion, "nuts-api version to link to");
        NId apiId = NWorkspace.of().getApiId().builder().setVersion(apiVersion).build();
        NDefinition apiDefinition = NSearchCmd.of().addId(apiId).failFast().latest()
                .content()
                .distinct()
                .getResultDefinitions()
                .findSingleton().get();

        NId appId = NId.get(options.getId()).get();
        NDefinition appDef = loadIdDefinition(appId);
        List<String> cmd = new ArrayList<>();

        cmd.add(getNutsStart(options).path().toString());
        cmd.add("-y");
        cmd.add(appId.toString());
        if (options.getLauncher().getArgs() != null) {
            cmd.addAll(options.getLauncher().getArgs());
        }
        String cwd = options.getLauncher().getWorkingDirectory();
        if (cwd == null) {
            //should it be id's var folder?
            cwd = System.getProperty("user.home");
        }
        String iconPath = resolveIcon(options.getLauncher().getIcon(), appId);

        String shortcutName = options.getLauncher().getShortcutName();
        if (shortcutName == null) {
            if (nDesktopIntegrationItem == NDesktopIntegrationItem.USER) {
                shortcutName = options.getLauncher().getCustomShortcutPath();
                if (shortcutName == null) {
                    shortcutName = options.getLauncher().getCustomScriptPath();
                }
            }
        }
        shortcutName = NameBuilder.extractPathName(shortcutName);
        if (shortcutName.isEmpty()) {
            shortcutName = "%N";
        }
        shortcutName += "%s%v%s%h";
        shortcutName = NameBuilder.label(appDef.getId(), shortcutName, null, appDef.getDescriptor()).buildName();

        String execCmd = NCmdLine.of(cmd).toString();
        FreeDesktopEntry.Group sl = FreeDesktopEntry.Group.desktopEntry(shortcutName, execCmd, cwd);
        sl.setStartNotify(true);
        sl.setIcon(iconPath);
        sl.setGenericName(apiDefinition.getDescriptor().getGenericName());
        sl.setComment(appDef.getDescriptor().getDescription());
        sl.setTerminal(options.getLauncher().isOpenTerminal());
        if (options.getLauncher().getMenuCategory() != null) {
            sl.addCategory(options.getLauncher().getMenuCategory());
        } else {
            sl.setCategories(appDef.getDescriptor().getCategories());
        }
        String preferredPath = getShortcutPath(options).toString();
        return createShortcut(nDesktopIntegrationItem, appId, preferredPath, sl);
    }

    protected String getDefaultIconPath() {
        return "apper";
    }


    public PathInfo[] createLaunchTermShortcutGlobal(NDesktopIntegrationItem nDesktopIntegrationItem, NdiScriptOptions options) {
        String fileName = options.resolveNutsApiId().getShortName().replace(':', '-');
        String name = "Nuts Terminal";
        return createLaunchTermShortcut(nDesktopIntegrationItem, options, fileName, name);
    }

    public abstract boolean isShortcutFileNameUserFriendly();

    public String resolveShortcutFileName(NId id, NDescriptor descriptor, String fileName, String name) {
        if (NBlankable.isBlank(fileName)) {
            if (isShortcutFileNameUserFriendly()) {
                fileName = name;
            }
            if (NBlankable.isBlank(fileName)) {
                if (isShortcutFileNameUserFriendly()) {
                    fileName = "%N%s%v%s%h";
                } else {
//                    fileName = "%g-%n-%v-%h";
                    fileName = "%g-%n-%v%s%h";
                }
            }
        }
        fileName =
                (isShortcutFileNameUserFriendly() ?
                        NameBuilder.label(id, fileName, null, descriptor)
                        : NameBuilder.id(id, fileName, null, descriptor)
                )
                        .buildName();
        return fileName;
    }

    public PathInfo[] createLaunchTermShortcut(NDesktopIntegrationItem nDesktopIntegrationItem,
                                               NdiScriptOptions options,
                                               String fileName, String name
    ) {
        String cmd = getNutsTerm(options)[0].path().toString();
        fileName = resolveShortcutFileName(options.resolveNutsApiId(), options.resolveNutsApiDef().getDescriptor(), fileName, name);
        if (name == null) {
            name = NameBuilder.label(options.resolveNutsApiId(), "Nuts Terminal%s%v%s%h", null, options.resolveNutsApiDef().getDescriptor())
                    .buildName();
        }
        String execCmd = NCmdLine.of(new String[]{cmd}).toString();
        return createShortcut(nDesktopIntegrationItem,
                options.resolveNutsApiId(),
                fileName,
                FreeDesktopEntry.Group.desktopEntry(name, execCmd, System.getProperty("user.home"))
                        .setIcon(resolveIcon(null, options.resolveNutsApiId()))
                        .setStartNotify(true)
                        .addCategory("/Utility/Nuts")
                        .setGenericName(options.resolveNutsApiDef().getDescriptor().getGenericName())
                        .setComment(options.resolveNutsApiDef().getDescriptor().getDescription())
                        .setTerminal(true)
        );
    }


//    public String createNutsEnvString(NdiScriptOptions options, boolean updateEnv, boolean updatePATH) {
//        final NutsWorkspace ws = session.getWorkspace();
//        String NUTS_APP_JAR_PATH = ws.search()
//                .setSession(session.copy())
//                .addId(ws.getApiId()).getResultPaths().required();
//
//        TreeSet<String> exports = new TreeSet<>();
//        SimpleScriptBuilder tmp = scriptBuilderSimple("nuts-env", options.resolveNutsApiId(), options);
//        if (updateEnv) {
//            exports.addAll(Arrays.asList("NUTS_VERSION", "NUTS_WORKSPACE", "NUTS_APP_JAR", "NUTS_WORKSPACE_BINDIR"));
//            tmp.printSetStatic("NUTS_VERSION", ws.getApiVersion().toString());
//            tmp.printSetStatic("NUTS_WORKSPACE", ws.locations().getWorkspaceLocation());
//            for (NutsStoreLocation value : NutsStoreLocation.values()) {
//                tmp.printSetStatic("NUTS_WORKSPACE_" + value, ws.locations().getStoreLocation(value));
//                exports.add("NUTS_WORKSPACE_" + value);
//            }
//            if (NUTS_APP_JAR_PATH.startsWith(ws.locations().getStoreLocation(NutsStoreLocation.LIB))) {
//                String pp = NUTS_APP_JAR_PATH.substring(ws.locations().getStoreLocation(NutsStoreLocation.LIB).length());
//                tmp.printSet("NUTS_APP_JAR", varRef("NUTS_WORKSPACE_LIB") + pp);
//            } else {
//                tmp.printSetStatic("NUTS_APP_JAR", NUTS_APP_JAR_PATH);
//            }
//            String p0 = options.resolveBinFolder().toString().substring(
//                    ws.locations().getStoreLocation(NutsStoreLocation.APPS).length()
//            );
//            tmp.printSet("NUTS_WORKSPACE_BINDIR", varRef("NUTS_WORKSPACE_BIN") + p0);
//        }
//        if (updatePATH) {
//            exports.add("PATH");
//            tmp.printSet("PATH", varRef("NUTS_WORKSPACE_BINDIR") + getPathVarSep() + varRef("PATH"));
//        }
//        String export = getExportCommand(exports.toArray(new String[0]));
//        if (!NutsBlankable.isBlank(export)) {
//            tmp.println(export);
//        }
//        return tmp.buildString();
//    }

    public ReplaceString getCommentLineConfigHeader() {
        return COMMENT_LINE_CONFIG_HEADER;
    }

    public abstract String getTemplateName(String name, NShellFamily shellFamily);

    private class RcNdiScriptInfo implements NdiScriptInfo {
        private final String bashrcName;
        private final NdiScriptOptions options;
        private final NShellFamily shellFamily;

        public RcNdiScriptInfo(String bashrcName, NdiScriptOptions options, NShellFamily shellFamily) {
            this.bashrcName = bashrcName;
            this.options = options;
            this.shellFamily = shellFamily;
        }

        @Override
        public NPath path() {
            if (bashrcName == null) {
                return null;
            }
            return NPath.of(System.getProperty("user.home")).resolve(bashrcName);
        }

        @Override
        public PathInfo create() {
            NPath apiConfigFile = path();
            if (apiConfigFile == null) {
                return null;
            }
            NShellHelper sh = NShellHelper.of(shellFamily);
            return addFileLine("sysrc",
                    options.resolveNutsApiId(),
                    apiConfigFile, getCommentLineConfigHeader(),
                    sh.getCallScriptCommand(getIncludeNutsInit(options, shellFamily).path().toString()),
                    sh.getShebanSh(), shellFamily);
        }
    }

}
