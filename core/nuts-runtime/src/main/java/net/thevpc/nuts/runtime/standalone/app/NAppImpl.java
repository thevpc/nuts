package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@NComponentScope(NScopeType.SESSION)
public class NAppImpl implements NApp, Cloneable {
    private Class appClass;
    private final NPath[] appFolders = new NPath[NStoreType.values().length];
    private final NPath[] appSharedFolders = new NPath[NStoreType.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private NCmdLineAutoComplete appAutoComplete;
    private NId appId;
    private NClock appStartTime;
    private List<String> appArgs;
    private NApplicationMode appMode = NApplicationMode.RUN;
    private NAppStoreLocationResolver appStoreLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NVersion appPreviousVersion;
    private List<String> appModeArgs = new ArrayList<>();

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NApp copy() {
        NAppImpl cloned = null;
        try {
            cloned = (NAppImpl) this.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        cloned.appClass = this.getAppClass();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.appFolders[i] = this.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.appSharedFolders[i] = this.getSharedFolder(value);
        }
        cloned.appAutoComplete = this.getAutoComplete();
        cloned.appStartTime = this.getStartTime();
        cloned.appArgs = this.getArguments() == null ? null : new ArrayList<>(this.getArguments());
        cloned.appMode = this.getMode();
        cloned.appStoreLocationResolver = this.getStoreLocationResolver();
        cloned.appPreviousVersion = this.getPreviousVersion();
        cloned.appModeArgs = this.getModeArguments() == null ? null : new ArrayList<>(this.getModeArguments());
        return cloned;
    }

    @Override
    public NApp copyFrom(NApp other) {
        //boolean withDefaults = false;
        this.appId = other.getId().orNull();
        this.appClass = other.getAppClass();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.appFolders[i] = other.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.appSharedFolders[i] = other.getSharedFolder(value);
        }
        this.appAutoComplete = other.getAutoComplete();
        this.appStartTime = other.getStartTime();
        this.appArgs = other.getArguments() == null ? null : new ArrayList<>(other.getArguments());
        this.appMode = other.getMode();
        this.appStoreLocationResolver = other.getStoreLocationResolver();
        this.appPreviousVersion = other.getPreviousVersion();
        this.appModeArgs = other.getModeArguments() == null ? null : new ArrayList<>(other.getModeArguments());
        return this;
    }

    @Override
    public NOptional<NId> getId() {
        return NOptional.ofNamed(this.appId,"app-id");
    }


    public void prepare(NAppInitInfo appInitInfo) {
        String[] args0=appInitInfo.getArgs();
        Class<?> appClass=appInitInfo.getAppClass();
        String storeId=appInitInfo.getStoreId();
        NClock startTime=appInitInfo.getStartTime();
        this.appStoreLocationResolver=appInitInfo.getStoreLocationSupplier();
        List<String> args = new ArrayList<>();
        if (args0 != null) {
            for (String s : args0) {
                if (s == null) {
                    s = "";
                }
                args.add(s);
            }
        }
        this.appStartTime = startTime == null ? NClock.now() : startTime;
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCmdLine execModeCommand = NCmdLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get();
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get();
                switch (a.key()) {
                    case "auto-complete": {
                        this.appMode = NApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get().asInt().get();
                        }
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        this.appMode = NApplicationMode.INSTALL;
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        this.appMode = NApplicationMode.UNINSTALL;
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        this.appMode = NApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            this.appPreviousVersion = NVersion.of(execModeCommand.next().flatMap(NLiteral::asString).get()).get();
                        }
                        this.appModeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NExecutionException(NMsg.ofC("Unsupported nuts-exec-mode : %s", args.get(0)), NExecutionException.ERROR_255);
                    }
                }
            }
            args = args.subList(1, args.size());
        }
        NId _appId = (NId) NApplications.getSharedMap().get("nuts.embedded.application.id");
        if (_appId != null) {
            //("=== Inherited "+_appId);
        } else {
            _appId = NId.ofClass(appClass).orNull();
        }
        if (_appId == null) {
            throw new NExecutionException(NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), NExecutionException.ERROR_255);
        }
        this.appArgs = (args);
        this.appId = (_appId);
        this.appClass = appClass == null ? null : JavaClassUtils.unwrapCGLib(appClass);
        NLocations locations = NLocations.of();
        for (NStoreType folder : NStoreType.values()) {
            this.setFolder(folder, locations.getStoreLocation(this.appId, folder));
            this.setSharedFolder(folder, locations.getStoreLocation(this.appId.builder().setVersion("SHARED").build(), folder));
        }
        if (this.appMode == NApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            this.appAutoComplete = new AppCmdLineAutoComplete(args, wordIndex);
        } else {
            this.appAutoComplete = null;
        }

    }

    @Override
    public NApplicationMode getMode() {
        return this.appMode;
    }

    @Override
    public List<String> getModeArguments() {
        return this.appModeArgs;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return this.appAutoComplete;
    }

    @Override
    public NOptional<NText> getHelpText() {
        NText h = null;
        try {
            h = NWorkspaceExt.of().resolveDefaultHelp(getAppClass());
        } catch (Exception ex) {
            //
        }
        if (h != null) {
            try {
                h = NTexts.of().transform(h, new NTextTransformConfig()
                        .setProcessTitleNumbers(true)
                        .setNormalize(true)
                        .setFlatten(true)
                );
            } catch (Exception ex) {
                //
                return NOptional.ofNamedError("application help", ex);
            }
        }
        return NOptional.ofNamed(h, "application help");
    }

    @Override
    public void printHelp() {
        NText h = NWorkspaceExt.of().resolveDefaultHelp(getAppClass());
        h = NTexts.of().transform(h, new NTextTransformConfig()
                .setProcessTitleNumbers(true)
                .setNormalize(true)
                .setFlatten(true)
        );
        NPrintStream out = NSession.get().out();
        if (h == null) {
            out.println(NMsg.ofC("Help is %s.", NMsg.ofStyled("missing", NTextStyle.error())));
        } else {
            out.println(h);
        }
        //need flush if the help is syntactically incorrect
        out.flush();
    }

    @Override
    public Class<?> getAppClass() {
        return this.appClass;
    }

    @Override
    public NPath getBinFolder() {
        return getFolder(NStoreType.BIN);
    }

    @Override
    public NPath getConfFolder() {
        return getFolder(NStoreType.CONF);
    }

    @Override
    public NPath getLogFolder() {
        return getFolder(NStoreType.LOG);
    }

    @Override
    public NPath getTempFolder() {
        return getFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getVarFolder() {
        return getFolder(NStoreType.VAR);
    }

    @Override
    public NPath getLibFolder() {
        return getFolder(NStoreType.LIB);
    }

    @Override
    public NPath getRunFolder() {
        return getFolder(NStoreType.RUN);
    }

    @Override
    public NPath getCacheFolder() {
        return getFolder(NStoreType.CACHE);
    }

    @Override
    public NPath getVersionFolder(NStoreType location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getId().get().getVersion().getValue())) {
            return getFolder(location);
        }
        NId newId = getId().get().builder().setVersion(version).build();
        if (this.appStoreLocationResolver != null) {
            NPath r = this.appStoreLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return NLocations.of().getStoreLocation(newId, location);
    }

    @Override
    public NPath getSharedAppsFolder() {
        return getSharedFolder(NStoreType.BIN);
    }

    @Override
    public NPath getSharedConfFolder() {
        return getSharedFolder(NStoreType.CONF);
    }

    @Override
    public NPath getSharedLogFolder() {
        return getSharedFolder(NStoreType.LOG);
    }

    @Override
    public NPath getSharedTempFolder() {
        return getSharedFolder(NStoreType.TEMP);
    }

    @Override
    public NPath getSharedVarFolder() {
        return getSharedFolder(NStoreType.VAR);
    }

    @Override
    public NPath getSharedLibFolder() {
        return getSharedFolder(NStoreType.LIB);
    }

    @Override
    public NPath getSharedRunFolder() {
        return getSharedFolder(NStoreType.RUN);
    }

    @Override
    public NPath getSharedFolder(NStoreType location) {
        return this.appSharedFolders[location.ordinal()];
    }

    @Override
    public NVersion getVersion() {
        return this.appId == null ? null : this.appId.getVersion();
    }

    @Override
    public List<String> getArguments() {
        return this.appArgs;
    }

    @Override
    public NClock getStartTime() {
        return this.appStartTime;
    }

    @Override
    public NVersion getPreviousVersion() {
        return this.appPreviousVersion;
    }

    @Override
    public NCmdLine getCmdLine() {
        NId appId = getId().orNull();
        if (appId == null) {
            return null;
        }
        List<String> appArguments = getArguments();
        if (appArguments == null) {
            return null;
        }
        return NCmdLine.of(appArguments)
                .setCommandName(appId.getArtifactId())
                .setAutoComplete(getAutoComplete())
                ;
    }

    @Override
    public void processCmdLine(NCmdLineRunner commandLineRunner) {
        getCmdLine().forEachPeek(commandLineRunner, new DefaultNCmdLineContext(this));
    }

    @Override
    public NPath getFolder(NStoreType location) {
        return this.appFolders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NAppStoreLocationResolver getStoreLocationResolver() {
        return this.appStoreLocationResolver;
    }

    public NApp setVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.appStoreLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NApp setMode(NApplicationMode mode) {
        this.appMode = mode;
        return this;
    }

    public NApp setModeArgs(List<String> modeArgs) {
        this.appModeArgs = modeArgs;
        return this;
    }

    public NApp setFolder(NStoreType location, NPath folder) {
        this.appFolders[location.ordinal()] = folder;
        return this;
    }

    public NApp setSharedFolder(NStoreType location, NPath folder) {
        this.appSharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NApp setId(NId appId) {
        this.appId = appId;
        return this;
    }

    //    @Override
    public NApp setArguments(List<String> args) {
        this.appArgs = args;
        return this;
    }

    public NApp setArguments(String[] args) {
        this.appArgs = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NApp setStartTime(NClock startTime) {
        this.appStartTime = startTime;
        return this;
    }

    public NApp setPreviousVersion(NVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    private static class AppCmdLineAutoComplete extends NCmdLineAutoCompleteBase {

        private final ArrayList<String> words;
        private final int wordIndex;

        public AppCmdLineAutoComplete(List<String> args, int wordIndex) {
            words = new ArrayList<>(args);
            this.wordIndex = wordIndex;
        }

        @Override
        public String getLine() {
            return NCmdLine.of(getWords()).toString();
        }

        @Override
        public List<String> getWords() {
            return words;
        }

        @Override
        public int getCurrentWordIndex() {
            return wordIndex;
        }

        @Override
        protected NArgCandidate addCandidatesImpl(NArgCandidate value) {
            NArgCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NExecutionException(NMsg.ofPlain("candidate cannot be null"), NExecutionException.ERROR_2);
            }
            String d = value.getDisplay();
            NPrintStream out = NSession.get().out();
            if (Objects.equals(v, d) || d == null) {
                out.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v)));
            } else {
                out.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v) + " " + NCmdLineUtils.escapeArgument(d)));
            }
            return c;
        }
    }
}
