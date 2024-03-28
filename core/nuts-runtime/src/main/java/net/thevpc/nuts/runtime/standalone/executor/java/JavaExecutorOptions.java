package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.boot.NWorkspaceCmdLineParser;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NClassLoaderNodeExt;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.dependency.util.NClassLoaderUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private final boolean mainClassApp = false;
    private final List<String> execArgs;
    private final List<String> jvmArgs = new ArrayList<String>();
    private final List<String> extraExecutorOptions = new ArrayList<String>();
    private final List<String> extraNutsOptions = new ArrayList<String>();
    private final List<String> j9_addModules = new ArrayList<String>();
    private final List<String> j9_modulePath = new ArrayList<String>();
    private final List<String> j9_upgradeModulePath = new ArrayList<String>();
    private final List<String> prependArgs = new ArrayList<>();
    private final List<String> appArgs;
    private final List<String> appendArgs = new ArrayList<>();
    //    private NutsDefinition nutsMainDef;
    private final NSession session;
    private final List<NClassLoaderNode> classPathNodes = new ArrayList<>();
    private final List<String> classPath = new ArrayList<>();
    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaEffVersion = null;
    private boolean java9;
    private String javaCommand = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private NPath dir = null;
    private boolean javaw = false;
    private boolean excludeBase = false;
    private boolean showCommand;
    private boolean jar = false;
    private String splash;
    private String j9_module;

    public JavaExecutorOptions(NDefinition def, boolean tempId, List<String> args,
                               List<String> executorOptions, NPath dir, NSession session) {
        this.session = session;
        showCommand = CoreNUtils.isShowCommand(session);
        NId id = def.getId();
        NDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
//            if (!CoreNutsUtils.isEffectiveId(id)) {
//                throw new NutsException(session, NMsg.ofC("id should be effective : %s", id));
//            }
            id = descriptor.getId();
        } else {
            descriptor = NDescriptorUtils.getEffectiveDescriptor(def, session);
            if (!CoreNUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getContent().flatMap(NPath::toPath).orNull();
        this.dir = dir;
        this.execArgs = executorOptions;
//        List<String> classPath0 = new ArrayList<>();
//        List<NutsClassLoaderNode> extraCp = new ArrayList<>();
        //will accept all -- and - based options!
        NCmdLine cmdLine = NCmdLine.of(getExecArgs()).setExpandSimpleOptions(false);
        NArg a;
        List<NClassLoaderNode> currentCP = new ArrayList<>();
        List<NArg> extraMayBeJvmOptions = new ArrayList<>();

        while (cmdLine.hasNext()) {
            a = cmdLine.peek().get(session);
            switch (a.key()) {
                case "--java-version":
                case "-java-version": {
                    cmdLine.withNextEntry((v, r, s) -> javaVersion = v);
                    break;
                }
                case "--java-home":
                case "-java-home": {
                    cmdLine.withNextEntry((v, r, s) -> javaCommand = v);
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    cmdLine.withNextEntry((v, r, s) -> addCp(currentCP, v));
                    break;
                }

                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    cmdLine.withNextEntry((v, r, s) -> addNp(currentCP, v));
                    break;
                }
                case "--main-class":
                case "-main-class":
                case "--class":
                case "-class": {
                    cmdLine.withNextEntry((v, r, s) -> mainClass = v);
                    break;
                }
                case "--dir":
                case "-dir": {
                    cmdLine.withNextEntry((v, r, s) -> this.dir = NPath.of(v, session));
                    break;
                }
                case "--win":
                case "--javaw": {
                    cmdLine.withNextFlag((v, r, s) -> javaw = v);
                    break;
                }
                case "--jar":
                case "-jar": {
                    cmdLine.withNextFlag((v, r, s) -> jar = v);
                    break;
                }
                case "--show-command":
                case "-show-command": {
                    cmdLine.withNextFlag((v, r, s) -> showCommand = v);
                    break;
                }
                case "--exclude-base":
                case "-exclude-base": {
                    cmdLine.withNextFlag((v, r, s) -> excludeBase = v);
                    break;
                }
                case "--add-module": {
                    cmdLine.withNextEntry((v, r, s) -> this.j9_addModules.add(v));
                    break;
                }
                case "-m":
                case "--module": {
                    //<module>/<mainclass>
                    cmdLine.withNextEntry((v, r, s) -> this.j9_module = v);
                    break;
                }
                case "--module-path": {
                    cmdLine.withNextEntry((v, r, s) -> this.j9_modulePath.add(v));
                    break;
                }
                case "-splash": {
                    cmdLine.withNextEntry((v, r, s) -> splash = v);
                    break;
                }
                case "--upgrade-module-path": {
                    cmdLine.withNextEntry((v, r, s) -> this.j9_upgradeModulePath.add(v));
                    break;
                }
                case "--prepend-arg": {
                    cmdLine.withNextEntry((v, r, s) -> this.prependArgs.add(v));
                    break;
                }
                case "--append-arg": {
                    cmdLine.withNextEntry((v, r, s) -> this.appendArgs.add(v));
                    break;
                }
                case "-s": {
                    NArg s = cmdLine.next().get(session);
                    getJvmArgs().add("-Dswing.aatext=true");
                    getJvmArgs().add("-Dawt.useSystemAAFontSettings=on");
                    getJvmArgs().add("-Dapple.laf.useScreenMenuBar=true");
                    getJvmArgs().add("-Dapple.awt.graphics.UseQuartz=true");
//                    getJvmArgs().add("-Dsun.java2d.noddraw=true");
//                    getJvmArgs().add("-Dsun.java2d.dpiaware=true");
                    break;
                }
                default: {
                    if (a.isOption()) {
                        List<NArg> nArgs = NWorkspaceCmdLineParser.nextNutsArgument(cmdLine, null, session).orNull();
                        if (nArgs != null) {
                            for (NArg nArg : nArgs) {
                                extraNutsOptions.add(nArg.toString());
                            }
                        } else if (a.toString().startsWith("--jvm-")) {
                            getJvmArgs().add(cmdLine.next().get().toString().substring("--jvm".length()));
                        } else if (a.toString().startsWith("--nuts-")) {
                            extraNutsOptions.add(cmdLine.next().get().toString().substring("--nuts".length()));
                        } else {
                            extraMayBeJvmOptions.add(cmdLine.next().get());
                        }
                    }
                }
            }
        }
        this.appArgs = new ArrayList<>();
        appArgs.addAll(prependArgs);
        appArgs.addAll(args);
        appArgs.addAll(appendArgs);

        List<NDefinition> nDefinitions = new ArrayList<>();
        NSearchCmd se = NSearchCmd.of(session);
        if (tempId) {
            for (NDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.toId());
            }
        } else {
            se.addId(id);
        }
        if (se.getIds().size() > 0) {
            nDefinitions.addAll(se
                    .setSession(se.getSession().copy().setTransitive(true))
                    .setDistinct(true)
                    .setContent(true)
                    .setDependencies(true)
                    .setLatest(true)
                    //
                    .setOptional(false)
                    .addScope(NDependencyScopePattern.RUN)
                    .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                    //
                    .getResultDefinitions().toList()
            );
        }
        if (path != null) {
            NVersion binJavaVersion = JavaJarUtils.parseJarClassVersion(
                    NPath.of(path, session), session
            );
            if (!NBlankable.isBlank(binJavaVersion) && (NBlankable.isBlank(javaVersion) || binJavaVersion.compareTo(javaVersion) > 0)) {
                javaVersion = binJavaVersion.toString();
            }
        }
        NVersion explicitJavaVersion = def.getDescriptor().getCondition().getPlatform().stream().map(x -> NId.of(x).get(session))
                .filter(x -> x.getShortName().equals("java"))
                .map(NId::getVersion)
                .min(Comparator.naturalOrder())
                .orElse(null);
        if (!NBlankable.isBlank(explicitJavaVersion) && (NBlankable.isBlank(javaVersion) || explicitJavaVersion.compareTo(javaVersion) > 0)) {
            javaVersion = explicitJavaVersion.toString();
        }
        NPlatformLocation nutsPlatformLocation = NJavaSdkUtils.of(session).resolveJdkLocation(getJavaVersion(), session);
        if (nutsPlatformLocation == null) {
            throw new NExecutionException(session, NMsg.ofC("no java version %s was found", NStringUtils.trim(getJavaVersion())), NExecutionException.ERROR_1);
        }
        javaEffVersion = nutsPlatformLocation.getVersion();
        javaCommand = NJavaSdkUtils.of(session).resolveJavaCommandByVersion(nutsPlatformLocation, javaw, session);
        if (javaCommand == null) {
            throw new NExecutionException(session, NMsg.ofC("no java version %s was found", getJavaVersion()), NExecutionException.ERROR_1);
        }
        for (NArg varg : extraMayBeJvmOptions) {
            if (isJvmOption(varg, explicitJavaVersion)) {
                getJvmArgs().add(varg.toString());
            } else {
                extraExecutorOptions.add(varg.toString());
            }
        }
        java9 = NVersion.of(javaVersion).get(session).compareTo("9") >= 0;
        for (NArg extraMayBeJvmOption : extraMayBeJvmOptions) {
            if (extraMayBeJvmOption.toString().startsWith("--jvm-")) {
                getJvmArgs().add(extraMayBeJvmOption.toString().substring("--jvm".length()));
            } else if (isJvmOption(extraMayBeJvmOption, NVersion.of(javaVersion).get(session))) {
                getJvmArgs().add(extraMayBeJvmOption.toString());
            }
        }
        if (this.jar) {
            if (this.mainClass != null) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().println((NMsg.ofC("ignored main-class=%s. running jar!", getMainClass())));
                }
            }
            if (!currentCP.isEmpty()) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().println(NMsg.ofC("ignored class-path=%s. running jar!", currentCP
                            .stream()
                            .map(x -> x.getURL().toString()).collect(Collectors.joining(","))
                    ));
                }
            }
            if (this.excludeBase) {
                throw new NIllegalArgumentException(session, NMsg.ofPlain("cannot exclude base with jar modifier"));
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    List<NExecutionEntry> classes = NLibPaths.of(session).parseExecutionEntries(path);
                    NExecutionEntry[] primary = classes.stream().filter(NExecutionEntry::isDefaultEntry).toArray(NExecutionEntry[]::new);
                    if (primary.length > 0) {
                        mainClass = Arrays.stream(primary).map(NExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    } else if (classes.size() > 0) {
                        mainClass = classes.stream().map(NExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    }
                }
            } else if (!mainClass.contains(".")) {
                List<NExecutionEntry> classes = NLibPaths.of(session).parseExecutionEntries(path);
                List<String> possibleClasses = classes.stream().map(NExecutionEntry::getName)
                        .collect(Collectors.toList());
                String r = resolveMainClass(mainClass, possibleClasses);
                if (r != null) {
                    mainClass = r;
                }
            }
            NId finalId = id;
            NAssert.requireNonNull(mainClass, () -> NMsg.ofC("missing Main Class for %s", finalId), session);
            boolean baseDetected = false;
            NRepositoryFilters nRepositoryFilters = NRepositoryFilters.of(session);
            for (NDefinition nDefinition : nDefinitions) {
                NClassLoaderNode nn = null;
                if (nDefinition.getContent().isPresent()) {
                    if (id.getLongName().equals(nDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            nn = (NClassLoaderUtils.definitionToClassLoaderNode(nDefinition,
                                    nRepositoryFilters.installedRepo(),
                                    session));
//                            classPath.add(nutsDefinition.getPath().toString());
//                            nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                        }
                    } else {
                        nn = (NClassLoaderUtils.definitionToClassLoaderNode(nDefinition,
                                nRepositoryFilters.installedRepo(),
                                session));
//                        classPath.add(nutsDefinition.getPath().toString());
//                        nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                    }
                }
                if (nn != null) {
                    currentCP.add(nn);
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                NAssert.requireNonNull(path, () -> NMsg.ofC("missing path %s", finalId), session);
                currentCP.add(0, NClassLoaderUtils.definitionToClassLoaderNode(def,nRepositoryFilters.installedRepo(), session));
            }
            classPathNodes.addAll(currentCP);
            List<NClassLoaderNodeExt> ln =
                    NJavaSdkUtils.loadNutsClassLoaderNodeExts(
                            currentCP.toArray(new NClassLoaderNode[0]),
                            java9, session
                    );
            if (java9) {
                List<NClassLoaderNodeExt> ln_javaFx = new ArrayList<>();
                List<NClassLoaderNodeExt> ln_others = new ArrayList<>();
                for (NClassLoaderNodeExt n : ln) {
                    if (n.jfx) {
                        ln_javaFx.add(n);
                    } else {
                        ln_others.add(n);
                    }
                }
                ln_javaFx.sort(
                        (a1, a2) -> {
                            NId b1 = a1.id;
                            NId b2 = a2.id;
                            // give precedence to classifiers
                            String c1 = b1.getClassifier();
                            String c2 = b2.getClassifier();
                            if (b1.builder().setClassifier(null).build().getShortName().equals(b2.builder().setClassifier(null).build().getShortName())) {
                                if (NBlankable.isBlank(c1)) {
                                    return 1;
                                }
                                if (NBlankable.isBlank(c2)) {
                                    return -1;
                                }
                                return b1.compareTo(b2);
                            }
                            return b1.compareTo(b2);
                        }
                );
                ln.clear();
                ln.addAll(ln_javaFx);
                ln.addAll(ln_others);
            }
            for (NClassLoaderNodeExt s : ln) {
                if (java9 && s.moduleName != null && s.jfx) {
                    if (!s.moduleName.endsWith("Empty")) {
                        j9_addModules.add(s.moduleName);
                    }
                    j9_modulePath.add(s.path.toPath().get().toString());
                    for (String requiredJfx : s.requiredJfx) {
                        if (!requiredJfx.endsWith("Empty")) {
                            j9_addModules.add(requiredJfx);
                        }
                    }
                } else {
                    classPath.add(s.path.toPath().get().toString());
                }
            }

            if (this.mainClass.contains(":")) {
                List<String> possibleClasses = StringTokenizerUtils.split(getMainClass(), ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NIllegalArgumentException(session, NMsg.ofC("missing Main-Class in Manifest for %s", id));
                    case 1:
                        //
                        break;
                    default: {
                        if (!session.isPlainOut()
                                || session.isBot()
//                                    || !session.isAsk()
                        ) {
                            throw new NExecutionException(session, NMsg.ofC("multiple runnable classes detected : %s", possibleClasses), NExecutionException.ERROR_1);
                        }
                        NTexts text = NTexts.of(session);
                        NTextBuilder msgString = text.ofBuilder();

                        msgString.append("multiple runnable classes detected  - actually ")
                                .append(text.ofStyled("" + possibleClasses.size(), NTextStyle.primary5()))
                                .append(" . Select one :\n");
                        int x = ((int) Math.log(possibleClasses.size())) + 2;
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            StringBuilder clsIndex = new StringBuilder();
                            clsIndex.append((i + 1));
                            while (clsIndex.length() < x) {
                                clsIndex.append(' ');
                            }
                            msgString.append(clsIndex.toString(), NTextStyle.primary4());
                            msgString.append(possibleClasses.get(i), NTextStyle.primary4());
                            msgString.append("\n");
                        }
                        msgString.append("enter class ")
                                .append("#", NTextStyle.primary5()).append(" or ").append("name", NTextStyle.primary5())
                                .append(" to run it. type ").append("cancel!", NTextStyle.error())
                                .append(" to cancel : ");

                        mainClass = session.getTerminal()
                                .ask()
                                .resetLine()
                                .setSession(session)
                                .forString(NMsg.ofNtf(msgString))
                                .setValidator((value, question) -> {
                                    Integer anyInt = NLiteral.of(value).asInt().orNull();
                                    if (anyInt != null) {
                                        int i = anyInt;
                                        if (i >= 1 && i <= possibleClasses.size()) {
                                            return possibleClasses.get(i - 1);
                                        }
                                    } else {
                                        for (String possibleClass : possibleClasses) {
                                            if (possibleClass.equals(value)) {
                                                return possibleClass;
                                            }
                                        }
                                    }
                                    throw new NValidationException(session);
                                }).getValue();
                        break;
                    }
                }
            }
        }


    }

    private boolean isJvmOption(NArg extraMayBeJvmOption, NVersion nVersion) {
        String s = extraMayBeJvmOption.toString();
        if (s.startsWith("-d32")) {
            return true;
        }
        if (s.startsWith("-d64")) {
            return true;
        }
        if (s.startsWith("-server")) {
            return true;
        }
        if (s.startsWith("-cp")) {
            return true;
        }
        if (s.startsWith("-classpath")) {
            return true;
        }
        if (s.startsWith("-D")) {
            return true;
        }
        if (s.startsWith("-verbose:")) {
            return true;
        }
        if (s.startsWith("-ea:")) {
            return true;
        }
        if (s.startsWith("-enableassertions:")) {
            return true;
        }
        if (s.startsWith("-da:")) {
            return true;
        }
        if (s.startsWith("-disableassertions:")) {
            return true;
        }
        if (s.startsWith("-esa")) {
            return true;
        }
        if (s.startsWith("-dsa")) {
            return true;
        }
        if (s.startsWith("-enablesystemassertions")) {
            return true;
        }
        if (s.startsWith("-disablesystemassertions")) {
            return true;
        }
        if (s.startsWith("-splash:")) {
            return true;
        }
        if (s.startsWith("-agentlib:")) {
            return true;
        }
        if (s.startsWith("-javaagent:")) {
            return true;
        }
        return false;
    }

    private String resolveMainClass(String name, List<String> possibleClasses) {
        if (name != null) {
            Integer v = NLiteral.of(name).asInt().orNull();
            if (v != null) {
                if (v >= 1 && v <= possibleClasses.size()) {
                    return possibleClasses.get(v - 1);
                } else if (v < 0) {
                    int i = possibleClasses.size() + v;
                    if (i >= 0 && i < possibleClasses.size()) {
                        return possibleClasses.get(i);
                    }
                }
            } else {
                if (possibleClasses.contains(name)) {
                    return name;
                } else {
                    List<String> extraPossibilities = new ArrayList<>();
                    for (String possibleClass : possibleClasses) {
                        int x = possibleClass.lastIndexOf('.');
                        if (x > 0) {
                            if (possibleClass.substring(x + 1).equals(name)) {
                                extraPossibilities.add(possibleClass);
                            }
                        }
                    }
                    if (extraPossibilities.size() == 1) {
                        return extraPossibilities.get(0);
                    }
                    if (extraPossibilities.size() > 1) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("ambiguous main-class %s matches all of %s",
                                name, extraPossibilities.toString()
                        ));
                    }
                    for (String possibleClass : possibleClasses) {
                        int x = possibleClass.lastIndexOf('.');
                        if (x > 0) {
                            if (possibleClass.substring(x + 1).equalsIgnoreCase(name)) {
                                extraPossibilities.add(possibleClass);
                            }
                        }
                    }
                    if (extraPossibilities.size() == 1) {
                        return extraPossibilities.get(0);
                    }
                    if (extraPossibilities.size() > 1) {
                        throw new NIllegalArgumentException(session, NMsg.ofC("ambiguous main-class %s matches all of from %s",
                                name, extraPossibilities.toString()
                        ));
                    }
                }
            }
        }
        return null;
    }

    private void addCp(List<NClassLoaderNode> classPath, String value) {
        if (value == null) {
            value = "";
        }
        boolean files = value.matches("([^:]+\\.jar[:;]?.*)");//|(.*[/\\\\].*)
        boolean nutsIds = value.matches("(.*[:#?].*)");
        if (nutsIds && !files) {
            addNp(classPath, value);
        } else {
            for (String n : StringTokenizerUtils.splitColon(value)) {
                if (!NBlankable.isBlank(n)) {
                    URL url = NPath.of(n, session).toURL().get();
                    classPath.add(new NClassLoaderNode("", url, true, true));
                }
            }
        }

    }

    private void addNp(List<NClassLoaderNode> classPath, String value) {
        NSession searchSession = this.session;
        NSearchCmd ns = NSearchCmd.of(searchSession).setLatest(true);
        NRepositoryFilters nRepositoryFilters = NRepositoryFilters.of(session);
        for (String n : StringTokenizerUtils.splitDefault(value)) {
            if (!NBlankable.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NId nutsId : ns.getResultIds()) {
            NDefinition f = NSearchCmd.of(searchSession).addId(nutsId)
                    .setLatest(true).getResultDefinitions().findFirst().get();
            classPath.add(NClassLoaderUtils.definitionToClassLoaderNode(f, nRepositoryFilters.installedRepo(),session));
        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public String getMainClass() {
        return mainClass;
    }

    public NPath getDir() {
        return dir;
    }

    public boolean isMainClassApp() {
        return mainClassApp;
    }

    public boolean isExcludeBase() {
        return excludeBase;
    }

    public boolean isShowCommand() {
        return showCommand;
    }

    public boolean isJar() {
        return jar;
    }

    //    public List<String> getClassPath() {
//        return classPath;
//    }
//
//    public List<String> getNutsPath() {
//        return nutsPath;
//    }
    public List<String> getExecArgs() {
        return execArgs;
    }

    public List<String> getJvmArgs() {
        return jvmArgs;
    }

    public NWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    public List<String> getAppArgs() {
        return appArgs;
    }

    public NSession getSession() {
        return session;
    }

    public void fillStrings(NClassLoaderNode n, List<String> list) {
        URL f = n.getURL();
        list.add(NPath.of(f, getSession()).toPath().get().toString());
        for (NClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }


    public void fillNidStrings(NClassLoaderNode n, List<String> list) {
        if (n.getId() == null || n.getId().isEmpty()) {
            URL f = n.getURL();
            list.add(NPath.of(f, getSession()).toPath().get().toString());
        } else {
            list.add(n.getId());
        }
        for (NClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }

    public List<String> getClassPathNidStrings() {
        List<String> li = new ArrayList<>();
        for (NClassLoaderNode n : getClassPathNodes()) {
            fillNidStrings(n, li);
        }
        return li;
    }

    public List<NClassLoaderNode> getClassPathNodes() {
        return classPathNodes;
    }

    public String getJavaEffVersion() {
        return javaEffVersion;
    }

    public boolean isJava9() {
        return java9;
    }

    public boolean isJavaw() {
        return javaw;
    }

    public String getSplash() {
        return splash;
    }

    public List<String> getJ9_addModules() {
        return j9_addModules;
    }

    public List<String> getJ9_modulePath() {
        return j9_modulePath;
    }

    public String getJ9_module() {
        return j9_module;
    }

    public List<String> getJ9_upgradeModulePath() {
        return j9_upgradeModulePath;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getExtraNutsOptions() {
        return extraNutsOptions;
    }

    public List<String> getExtraExecutorOptions() {
        return extraExecutorOptions;
    }
}
