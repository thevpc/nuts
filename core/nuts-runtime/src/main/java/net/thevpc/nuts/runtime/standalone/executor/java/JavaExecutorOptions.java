package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsClassLoaderNode;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsClassLoaderNodeExt;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.dependency.util.NutsClassLoaderUtils;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private final boolean mainClassApp = false;
    private final List<String> execArgs;
    private final List<String> jvmArgs = new ArrayList<String>();
    private final List<String> j9_addModules = new ArrayList<String>();
    private final List<String> j9_modulePath = new ArrayList<String>();
    private final List<String> j9_upgradeModulePath = new ArrayList<String>();
    private final List<String> prependArgs = new ArrayList<>();
    private final List<String> appArgs;
    private final List<String> appendArgs = new ArrayList<>();
    //    private NutsDefinition nutsMainDef;
    private final NutsSession session;
    private final List<NutsClassLoaderNode> classPathNodes = new ArrayList<>();
    private final List<String> classPath = new ArrayList<>();
    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaEffVersion = null;
    private boolean java9;
    private String javaCommand = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private String dir = null;
    private boolean javaw = false;
    private boolean excludeBase = false;
    private boolean showCommand;
    private boolean jar = false;
    private String splash;
    private String j9_module;

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, List<String> args,
                               List<String> executorOptions, String dir, NutsSession session) {
        this.session = session;
        showCommand = CoreNutsUtils.isShowCommand(session);
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
//            if (!CoreNutsUtils.isEffectiveId(id)) {
//                throw new NutsException(session, NutsMessage.cstyle("id should be effective : %s", id));
//            }
            id = descriptor.getId();
        } else {
            descriptor = NutsDescriptorUtils.getEffectiveDescriptor(def, session);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getContent().map(NutsPath::toFile).orNull();
        this.dir = dir;
        this.execArgs = executorOptions;
//        List<String> classPath0 = new ArrayList<>();
//        List<NutsClassLoaderNode> extraCp = new ArrayList<>();
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = NutsCommandLine.of(getExecArgs()).setExpandSimpleOptions(false);
        NutsArgument a;
        List<NutsClassLoaderNode> currentCP = new ArrayList<>();
        while (cmdLine.hasNext()) {
            a = cmdLine.peek().get(session);
            switch (a.key()) {
                case "--java-version":
                case "-java-version": {
                    cmdLine.withNextString((v,r,s)-> javaVersion = v,session);
                    break;
                }
                case "--java-home":
                case "-java-home": {
                    cmdLine.withNextString((v,r,s)-> javaCommand = v,session);
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    cmdLine.withNextString((v,r,s)-> addCp(currentCP, v),session);
                    break;
                }

                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    cmdLine.withNextString((v,r,s)-> addNp(currentCP, v),session);
                    break;
                }
                case "--main-class":
                case "-main-class":
                case "--class":
                case "-class": {
                    cmdLine.withNextString((v,r,s)-> mainClass = v,session);
                    break;
                }
                case "--dir":
                case "-dir": {
                    cmdLine.withNextString((v,r,s)-> this.dir = v,session);
                    break;
                }
                case "--win":
                case "--javaw": {
                    cmdLine.withNextBoolean((v,r,s)-> javaw = v,session);
                    break;
                }
                case "--jar":
                case "-jar": {
                    cmdLine.withNextBoolean((v,r,s)-> jar = v,session);
                    break;
                }
                case "--show-command":
                case "-show-command": {
                    cmdLine.withNextBoolean((v,r,s)-> showCommand = v,session);
                    break;
                }
                case "--exclude-base":
                case "-exclude-base": {
                    cmdLine.withNextBoolean((v,r,s)-> excludeBase = v,session);
                    break;
                }
                case "--add-module": {
                    cmdLine.withNextString((v,r,s)-> this.j9_addModules.add(v),session);
                    break;
                }
                case "-m":
                case "--module": {
                    //<module>/<mainclass>
                    cmdLine.withNextString((v,r,s)-> this.j9_module=v,session);
                    break;
                }
                case "--module-path": {
                    cmdLine.withNextString((v,r,s)-> this.j9_modulePath.add(v),session);
                    break;
                }
                case "-splash": {
                    cmdLine.withNextString((v,r,s)-> splash = v,session);
                    break;
                }
                case "--upgrade-module-path": {
                    cmdLine.withNextString((v,r,s)-> this.j9_upgradeModulePath.add(v),session);
                    break;
                }
                case "--prepend-arg": {
                    cmdLine.withNextString((v,r,s)-> this.prependArgs.add(v),session);
                    break;
                }
                case "--append-arg": {
                    cmdLine.withNextString((v,r,s)-> this.appendArgs.add(v),session);
                    break;
                }
                case "-s": {
                    NutsArgument s = cmdLine.next().get(session);
                    getJvmArgs().add("-Dswing.aatext=true");
                    getJvmArgs().add("-Dawt.useSystemAAFontSettings=on");
                    getJvmArgs().add("-Dapple.laf.useScreenMenuBar=true");
                    getJvmArgs().add("-Dapple.awt.graphics.UseQuartz=true");
//                    getJvmArgs().add("-Dsun.java2d.noddraw=true");
//                    getJvmArgs().add("-Dsun.java2d.dpiaware=true");
                    break;
                }
                default: {
                    getJvmArgs().add(cmdLine.next().flatMap(NutsValue::asString).get(session));
                }
            }
        }
        this.appArgs = new ArrayList<>();
        appArgs.addAll(prependArgs);
        appArgs.addAll(args);
        appArgs.addAll(appendArgs);

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsSearchCommand se = session.search().setSession(session);
        if (tempId) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.toId());
            }
        } else {
            se.addId(id);
        }
        if (se.getIds().size() > 0) {
            nutsDefinitions.addAll(se
                    .setSession(se.getSession().copy().setTransitive(true))
                    .setDistinct(true)
                    .setContent(true)
                    .setDependencies(true)
                    .setLatest(true)
                    //
                    .setOptional(false)
                    .addScope(NutsDependencyScopePattern.RUN)
                    .setDependencyFilter(NutsDependencyFilters.of(session).byRunnable())
                    //
                    .getResultDefinitions().toList()
            );
        }
        if (path != null) {
            NutsVersion binJavaVersion = JavaJarUtils.parseJarClassVersion(
                    NutsPath.of(path, session), session
            );
            if (!NutsBlankable.isBlank(binJavaVersion) && (NutsBlankable.isBlank(javaVersion) || binJavaVersion.compareTo(javaVersion) > 0)) {
                javaVersion = binJavaVersion.toString();
            }
        }
        NutsVersion explicitJavaVersion = def.getDescriptor().getCondition().getPlatform().stream().map(x -> NutsId.of(x).get(session))
                .filter(x -> x.getShortName().equals("java"))
                .map(NutsId::getVersion)
                .min(Comparator.naturalOrder())
                .orElse(null);
        if (!NutsBlankable.isBlank(explicitJavaVersion) && (NutsBlankable.isBlank(javaVersion) || explicitJavaVersion.compareTo(javaVersion) > 0)) {
            javaVersion = explicitJavaVersion.toString();
        }
        NutsPlatformLocation nutsPlatformLocation = NutsJavaSdkUtils.of(session).resolveJdkLocation(getJavaVersion(), session);
        if (nutsPlatformLocation == null) {
            throw new NutsExecutionException(session, NutsMessage.ofCstyle("no java version %s was found", NutsStringUtils.trim(getJavaVersion())), 1);
        }
        javaEffVersion = nutsPlatformLocation.getVersion();
        javaCommand = NutsJavaSdkUtils.of(session).resolveJavaCommandByVersion(nutsPlatformLocation, javaw, session);
        if (javaCommand == null) {
            throw new NutsExecutionException(session, NutsMessage.ofCstyle("no java version %s was found", getJavaVersion()), 1);
        }
        java9 = NutsVersion.of(javaVersion).get(session).compareTo("1.8") > 0;
        if (this.jar) {
            if (this.mainClass != null) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("ignored main-class=%s. running jar!%n", getMainClass());
                }
            }
            if (!currentCP.isEmpty()) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("ignored class-path=%s. running jar!%n", currentCP
                            .stream()
                            .map(x -> x.getURL().toString()).collect(Collectors.joining(","))
                    );
                }
            }
            if (this.excludeBase) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("cannot exclude base with jar modifier"));
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    List<NutsExecutionEntry> classes = NutsExecutionEntries.of(session).parse(path);
                    NutsExecutionEntry[] primary = classes.stream().filter(NutsExecutionEntry::isDefaultEntry).toArray(NutsExecutionEntry[]::new);
                    if (primary.length > 0) {
                        mainClass = Arrays.stream(primary).map(NutsExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    } else if (classes.size() > 0) {
                        mainClass = classes.stream().map(NutsExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    }
                }
            } else if (!mainClass.contains(".")) {
                List<NutsExecutionEntry> classes = NutsExecutionEntries.of(session).parse(path);
                List<String> possibleClasses = classes.stream().map(NutsExecutionEntry::getName)
                        .collect(Collectors.toList());
                String r = resolveMainClass(mainClass, possibleClasses);
                if (r != null) {
                    mainClass = r;
                }
            }
            NutsId finalId = id;
            NutsUtils.requireNonNull(mainClass, () -> NutsMessage.ofCstyle("missing Main Class for %s", finalId), session);
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                NutsClassLoaderNode nn = null;
                if (nutsDefinition.getContent().isPresent()) {
                    if (id.getLongName().equals(nutsDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            nn = (NutsClassLoaderUtils.definitionToClassLoaderNode(nutsDefinition, session));
//                            classPath.add(nutsDefinition.getPath().toString());
//                            nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                        }
                    } else {
                        nn = (NutsClassLoaderUtils.definitionToClassLoaderNode(nutsDefinition, session));
//                        classPath.add(nutsDefinition.getPath().toString());
//                        nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                    }
                }
                if (nn != null) {
                    currentCP.add(nn);
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                NutsUtils.requireNonNull(path, () -> NutsMessage.ofCstyle("missing path %s", finalId), session);
                currentCP.add(0, NutsClassLoaderUtils.definitionToClassLoaderNode(def, session));
            }
            classPathNodes.addAll(currentCP);
            List<NutsClassLoaderNodeExt> ln =
                    NutsJavaSdkUtils.loadNutsClassLoaderNodeExts(
                            currentCP.toArray(new NutsClassLoaderNode[0]),
                            java9, session
                    );
            if (java9) {
                List<NutsClassLoaderNodeExt> ln_javaFx = new ArrayList<>();
                List<NutsClassLoaderNodeExt> ln_others = new ArrayList<>();
                for (NutsClassLoaderNodeExt n : ln) {
                    if (n.jfx) {
                        ln_javaFx.add(n);
                    } else {
                        ln_others.add(n);
                    }
                }
                ln_javaFx.sort(
                        (a1, a2) -> {
                            NutsId b1 = a1.id;
                            NutsId b2 = a2.id;
                            // give precedence to classifiers
                            String c1 = b1.getClassifier();
                            String c2 = b2.getClassifier();
                            if (b1.builder().setClassifier(null).build().getShortName().equals(b2.builder().setClassifier(null).build().getShortName())) {
                                if (NutsBlankable.isBlank(c1)) {
                                    return 1;
                                }
                                if (NutsBlankable.isBlank(c2)) {
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
            for (NutsClassLoaderNodeExt s : ln) {
                if (java9 && s.moduleName != null && s.jfx) {
                    if (!s.moduleName.endsWith("Empty")) {
                        j9_addModules.add(s.moduleName);
                    }
                    j9_modulePath.add(s.path.toFile().toString());
                    for (String requiredJfx : s.requiredJfx) {
                        if (!requiredJfx.endsWith("Empty")) {
                            j9_addModules.add(requiredJfx);
                        }
                    }
                } else {
                    classPath.add(s.path.toFile().toString());
                }
            }

            if (this.mainClass.contains(":")) {
                List<String> possibleClasses = StringTokenizerUtils.split(getMainClass(), ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("missing Main-Class in Manifest for %s", id));
                    case 1:
                        //
                        break;
                    default: {
                        if (!session.isPlainOut()
                                || session.isBot()
//                                    || !session.isAsk()
                        ) {
                            throw new NutsExecutionException(session, NutsMessage.ofCstyle("multiple runnable classes detected : %s", possibleClasses), 102);
                        }
                        NutsTexts text = NutsTexts.of(session);
                        NutsTextBuilder msgString = text.ofBuilder();

                        msgString.append("multiple runnable classes detected  - actually ")
                                .append(text.ofStyled("" + possibleClasses.size(), NutsTextStyle.primary5()))
                                .append(" . Select one :\n");
                        int x = ((int) Math.log(possibleClasses.size())) + 2;
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            StringBuilder clsIndex = new StringBuilder();
                            clsIndex.append((i + 1));
                            while (clsIndex.length() < x) {
                                clsIndex.append(' ');
                            }
                            msgString.append(clsIndex.toString(), NutsTextStyle.primary4());
                            msgString.append(possibleClasses.get(i), NutsTextStyle.primary4());
                            msgString.append("\n");
                        }
                        msgString.append("enter class ")
                                .append("#", NutsTextStyle.primary5()).append(" or ").append("name", NutsTextStyle.primary5())
                                .append(" to run it. type ").append("cancel!", NutsTextStyle.error())
                                .append(" to cancel : ");

                        mainClass = session.getTerminal()
                                .ask()
                                .resetLine()
                                .setSession(session)
                                .forString(NutsMessage.ofNtf(msgString))
                                .setValidator((value, question) -> {
                                    Integer anyInt = NutsValue.of(value).asInt().orNull();
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
                                    throw new NutsValidationException(session);
                                }).getValue();
                        break;
                    }
                }
            }
        }


    }

    private String resolveMainClass(String name, List<String> possibleClasses) {
        if (name != null) {
            Integer v = NutsValue.of(name).asInt().orNull();
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("ambiguous main-class %s matches all of %s",
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("ambiguous main-class %s matches all of from %s",
                                name, extraPossibilities.toString()
                        ));
                    }
                }
            }
        }
        return null;
    }

    private void addCp(List<NutsClassLoaderNode> classPath, String value) {
        if (value == null) {
            value = "";
        }
        boolean files = value.matches("([^:]+\\.jar[:;]?.*)");//|(.*[/\\\\].*)
        boolean nutsIds = value.matches("(.*[:#?].*)");
        if (nutsIds && !files) {
            addNp(classPath, value);
        } else {
            for (String n : StringTokenizerUtils.splitColon(value)) {
                if (!NutsBlankable.isBlank(n)) {
                    URL url = NutsPath.of(n, session).toURL();
                    classPath.add(new NutsClassLoaderNode("", url, true, true));
                }
            }
        }

    }

    private void addNp(List<NutsClassLoaderNode> classPath, String value) {
        NutsSession searchSession = this.session;
        NutsSearchCommand ns = session.search().setLatest(true)
                .setSession(searchSession);
        for (String n : StringTokenizerUtils.splitDefault(value)) {
            if (!NutsBlankable.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = session
                    .search().addId(nutsId).setSession(searchSession).setLatest(true).getResultDefinitions().required();
            classPath.add(NutsClassLoaderUtils.definitionToClassLoaderNode(f, session));
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

    public String getDir() {
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

    public NutsWorkspace getWorkspace() {
        return getSession().getWorkspace();
    }

    public List<String> getAppArgs() {
        return appArgs;
    }

    public NutsSession getSession() {
        return session;
    }

    public void fillStrings(NutsClassLoaderNode n, List<String> list) {
        URL f = n.getURL();
        list.add(NutsPath.of(f, getSession()).toFile().toString());
        for (NutsClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }


    public void fillNidStrings(NutsClassLoaderNode n, List<String> list) {
        if (n.getId() == null || n.getId().isEmpty()) {
            URL f = n.getURL();
            list.add(NutsPath.of(f, getSession()).toFile().toString());
        } else {
            list.add(n.getId());
        }
        for (NutsClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }

    public List<String> getClassPathNidStrings() {
        List<String> li = new ArrayList<>();
        for (NutsClassLoaderNode n : getClassPathNodes()) {
            fillNidStrings(n, li);
        }
        return li;
    }

    public List<NutsClassLoaderNode> getClassPathNodes() {
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

}
