package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.*;

import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCmdLineUtils;
import net.thevpc.nuts.runtime.standalone.session.DefaultNSession;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.util.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NComponentScope(NScopeType.SESSION)
public class NAppImpl implements NApp, Cloneable, NCopiable {
    private Class appClass;
    private final NPath[] folders = new NPath[NStoreType.values().length];
    private final NPath[] sharedFolders = new NPath[NStoreType.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private NCmdLineAutoComplete autoComplete;
    private NId id;
    private String bundleName;
    private NClock startTime;
    private List<String> args;
    private NApplicationMode mode = NApplicationMode.RUN;
    private NAppStoreLocationResolver storeLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NVersion previousVersion;
    private List<String> modeArgs = new ArrayList<>();

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
            cloned.folders[i] = this.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            cloned.sharedFolders[i] = this.getSharedFolder(value);
        }
        cloned.autoComplete = this.getAutoComplete();
        cloned.startTime = this.getStartTime();
        cloned.args = this.getArguments() == null ? null : new ArrayList<>(this.getArguments());
        cloned.mode = this.getMode();
        cloned.storeLocationResolver = this.getStoreLocationResolver();
        cloned.previousVersion = this.previousVersion;
        cloned.modeArgs = this.getModeArguments() == null ? null : new ArrayList<>(this.getModeArguments());
        return cloned;
    }

    @Override
    public NApp copyFrom(NApp other) {
        //boolean withDefaults = false;
        this.id = other.getId().orNull();
        this.appClass = other.getAppClass();
        NStoreType[] values = NStoreType.values();
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.folders[i] = other.getFolder(value);
        }
        for (int i = 0; i < values.length; i++) {
            NStoreType value = values[i];
            this.sharedFolders[i] = other.getSharedFolder(value);
        }
        this.autoComplete = other.getAutoComplete();
        this.startTime = other.getStartTime();
        this.args = other.getArguments() == null ? null : new ArrayList<>(other.getArguments());
        this.mode = other.getMode();
        this.storeLocationResolver = other.getStoreLocationResolver();
        this.previousVersion = other.getPreviousVersion().orNull();
        this.modeArgs = other.getModeArguments() == null ? null : new ArrayList<>(other.getModeArguments());
        return this;
    }

    @Override
    public NOptional<NId> getId() {
        return NOptional.ofNamed(this.id, "app-id");
    }


    public void prepare(NAppInitInfo appInitInfo) {
        String[] args0 = appInitInfo.getArgs();
        Class<?> appClass = appInitInfo.getAppClass();
        NClock startTime = appInitInfo.getStartTime();
        this.storeLocationResolver = appInitInfo.getStoreLocationSupplier();
        List<String> args = new ArrayList<>();
        if (args0 != null) {
            for (String s : args0) {
                if (s == null) {
                    s = "";
                }
                args.add(s);
            }
        }
        this.startTime = startTime == null ? NClock.now() : startTime;
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCmdLine execModeCommand = NCmdLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get();
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get();
                switch (a.key()) {
                    case "auto-complete": {
                        this.mode = NApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get().intValue();
                        }
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        this.mode = NApplicationMode.INSTALL;
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        this.mode = NApplicationMode.UNINSTALL;
                        this.modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        this.mode = NApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            this.previousVersion = NVersion.get(execModeCommand.next().get().getImage()).get();
                        }
                        this.modeArgs = execModeCommand.toStringList();
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
            _appId = NId.getForClass(appClass).orNull();
        }
        if (_appId == null) {
            throw new NExecutionException(NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), NExecutionException.ERROR_255);
        }
        this.args = (args);
        this.id = (_appId);
        this.appClass = appClass == null ? null : JavaClassUtils.unwrapCGLib(appClass);
        NWorkspace workspace = NWorkspace.of();
        for (NStoreType folder : NStoreType.values()) {
            this.setFolder(folder, workspace.getStoreLocation(this.id, folder));
            this.setSharedFolder(folder, workspace.getStoreLocation(this.id.builder().setVersion("SHARED").build(), folder));
        }
        if (this.mode == NApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            this.autoComplete = new AppCmdLineAutoComplete(args, wordIndex);
        } else {
            this.autoComplete = null;
        }
        bundleName=resolveAppNameFromClass(this.appClass,_appId.getArtifactId());
    }

    public String getBundleName() {
        return bundleName;
    }

    private static String resolveAppNameFromClass(Class clazz, String defaultName) {
        String n = null;
        String baseFilePath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        NLog nLog = NLog.of(NAppImpl.class);
        nLog.debug(NMsg.ofC("resolveAppNameFromClass " + clazz + " (" + defaultName + ") " + baseFilePath));
        if (baseFilePath != null) {
            try {
                String jar = extractVar(baseFilePath, "x", "(?<x>.*)[.]jar[!]/BOOT-INF/classes[!]/?");
                if (jar != null) {
                    n = extractVar(new File(jar).getName(), "n", "(?<n>-[^.]+)[.]jar");
                } else {
                    File file = new File(baseFilePath);
                    File parentFile = file.getParentFile();
                    File parentFile2 = parentFile == null ? null : parentFile.getParentFile();
                    File parentFile3 = parentFile2 == null ? null : parentFile2.getParentFile();
                    if (
                            file.getName().toLowerCase().endsWith(".jar")
                                    && parentFile3 != null
                                    && parentFile.getName().equals("lib")
                                    && parentFile2.getName().equals("WEB-INF")
                    ) {
                        n = parentFile3.getName();
                        // /WEB-INF/lib/library.jar
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-/WEB-INF/lib/lib.jar] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    } else if (
                            file.getName().equals("classes")
                                    && parentFile2 != null
                                    && parentFile.getName().equals("WEB-INF")
                    ) {
                        n = parentFile2.getName();
                        // /WEB-INF/classes/
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-/WEB-INF/classes] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    } else if (parentFile2 != null) {
                        n = parentFile2.getName();
                        nLog.debug(NMsg.ofC("resolveAppNameFromClass [PARTIAL-OTHER] " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                    }
                }
            } catch (Exception ex) {
                //
            }
        }
        if (n != null) {
            n = n.trim();
            if (!n.isEmpty()) {
                try {
                    n = URLDecoder.decode(n, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                }
                if (n.contains("##")) {
                    n = n.split("##")[0];
                }
                nLog.debug(NMsg.ofC("resolveAppNameFromClass " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
                return n;
            }
        }
        nLog.debug(NMsg.ofC("resolveAppNameFromClass " + clazz + " (" + defaultName + ") " + baseFilePath + " ==> RESULT = " + n));
        return defaultName;
    }

    private static String extractVar(String str, String varName, String pattern) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        if (m.matches()) {
            return m.group(varName);
        }
        return null;
    }

    @Override
    public NApplicationMode getMode() {
        return this.mode;
    }

    @Override
    public List<String> getModeArguments() {
        return this.modeArgs;
    }

    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return this.autoComplete;
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
        if (h == null) {
            NOut.println(NMsg.ofC("Help is %s.", NMsg.ofStyledError("missing")));
        } else {
            NOut.println(h);
        }
        //need flush if the help is syntactically incorrect
        NOut.flush();
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
    public NPath getVersionFolder(NStoreType storeType, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getId().get().getVersion().getValue())) {
            return getFolder(storeType);
        }
        NId newId = getId().get().builder().setVersion(version).build();
        if (this.storeLocationResolver != null) {
            NPath r = this.storeLocationResolver.getStoreLocation(newId, storeType);
            if (r != null) {
                return r;
            }
        }
        return NWorkspace.of().getStoreLocation(newId, storeType);
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
        return this.sharedFolders[location.ordinal()];
    }

    @Override
    public NOptional<NVersion> getVersion() {
        return this.getId().map(NId::getVersion);
    }

    @Override
    public List<String> getArguments() {
        return this.args;
    }

    @Override
    public NClock getStartTime() {
        return this.startTime;
    }

    @Override
    public NOptional<NVersion> getPreviousVersion() {
        return NOptional.ofNamed(previousVersion, "previousVersion");
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
    public void runCmdLine(NCmdLineRunner commandLineRunner) {
        getCmdLine()
                .setSource(this)
                .run(commandLineRunner);
    }

    @Override
    public NPath getFolder(NStoreType location) {
        return this.folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NAppStoreLocationResolver getStoreLocationResolver() {
        return this.storeLocationResolver;
    }

    public NApp setVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NApp setMode(NApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NApp setModeArgs(List<String> modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    public NApp setFolder(NStoreType location, NPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NApp setSharedFolder(NStoreType location, NPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NApp setId(NId appId) {
        this.id = appId;
        return this;
    }

    //    @Override
    public NApp setArguments(List<String> args) {
        this.args = args;
        return this;
    }

    public NApp setArguments(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NApp setStartTime(NClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NApp setPreviousVersion(NVersion previousVersion) {
        this.previousVersion = previousVersion;
        return this;
    }

    public <T> T getOrComputeProperty(String name, NScopeType scope, Supplier<T> supplier) {
        NAssert.requireNonNull(supplier);
        if (scope == null) {
            scope = NScopeType.SHARED_SESSION;
        }
        switch (scope) {
            case SHARED_SESSION:
            case TRANSITIVE_SESSION:
            case SESSION: {
                return ((DefaultNSession) NSession.of()).getPropertiesHolder().getOrComputeProperty(name, supplier, scope);
            }
            case WORKSPACE: {
                return ((NWorkspaceExt.of())).getModel().properties.getOrComputeProperty(name, supplier, NScopeType.WORKSPACE);
            }
            case PROTOTYPE: {
                return supplier.get();
            }
            default: {
                throw new NUnsupportedEnumException(scope);
            }
        }
    }

    public <T> T setProperty(String name, NScopeType scope, T value) {
        if (scope == null) {
            scope = NScopeType.SHARED_SESSION;
        }
        switch (scope) {
            case SESSION:
            case SHARED_SESSION: {
                return (T) ((DefaultNSession) NSession.of()).getPropertiesHolder().setProperty(name, value, scope);
            }
            case TRANSITIVE_SESSION: {
                return (T) ((DefaultNSession) NSession.of()).getPropertiesHolder().setProperty(name, CoreNUtils.checkCopiableValue(value), scope);
            }
            case WORKSPACE: {
                NWorkspaceModel m = ((NWorkspaceExt.of())).getModel();
                return (T) m.properties.setProperty(name, value,NScopeType.WORKSPACE);
            }
            case PROTOTYPE:
            default: {
                throw new NUnsupportedEnumException(scope);
            }
        }
    }

    public <T> NOptional<T> getProperty(String name, NScopeType scope) {
        if (scope == null) {
            scope = NScopeType.SHARED_SESSION;
        }
        switch (scope) {
            case PROTOTYPE: {
                return NOptional.<T>ofNamedEmpty(name)
                        .withDefault(() -> this.<T>getProperty(name, NScopeType.SESSION).orDefault());
            }
            case SHARED_SESSION:
            case SESSION:
            case TRANSITIVE_SESSION:
            {
                return ((DefaultNSession) NSession.of()).getPropertiesHolder().<T>getOptional(name)
                        .withDefault(() -> this.<T>getProperty(name, NScopeType.WORKSPACE).orDefault())
                        ;
            }
            case WORKSPACE: {
                return (NWorkspaceExt.of()).getModel().properties.getOptional(name);
            }
            default: {
                return NOptional.<T>ofNamedEmpty(name);
            }
        }
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
            if (Objects.equals(v, d) || d == null) {
                NOut.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v)));
            } else {
                NOut.println(NMsg.ofC("%s", NConstants.Apps.AUTO_COMPLETE_CANDIDATE_PREFIX + NCmdLineUtils.escapeArgument(v) + " " + NCmdLineUtils.escapeArgument(d)));
            }
            return c;
        }
    }
}
