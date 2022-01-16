package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsClassLoaderNodeExt;
import net.thevpc.nuts.runtime.standalone.util.jclass.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.dependency.util.NutsClassLoaderUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private final boolean mainClassApp = false;
    private final String[] execArgs;
    private final List<String> jvmArgs = new ArrayList<String>();
    private final List<String> j9_addModules = new ArrayList<String>();
    private final List<String> j9_modulePath = new ArrayList<String>();
    private final List<String> j9_upgradeModulePath = new ArrayList<String>();
    private final List<String> prependArgs=new ArrayList<>();
    private final List<String> appArgs;
    private final List<String> appendArgs=new ArrayList<>();
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

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsSession session) {
        this.session = session;
        showCommand = getSession().boot().getBootCustomBoolArgument(false, false, false, "---show-command");
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
//            if (!CoreNutsUtils.isEffectiveId(id)) {
//                throw new NutsException(session, NutsMessage.cstyle("id should be effective : %s", id));
//            }
            id = descriptor.getId();
        } else {
            descriptor = NutsDescriptorUtils.getEffectiveDescriptor(def,session);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getFile();
        this.dir = dir;
        this.execArgs = executorOptions;
//        List<String> classPath0 = new ArrayList<>();
//        List<NutsClassLoaderNode> extraCp = new ArrayList<>();
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = NutsCommandLine.of(getExecArgs(), session).setExpandSimpleOptions(false);
        NutsArgument a;
        List<NutsClassLoaderNode> currentCP = new ArrayList<>();
        while (cmdLine.hasNext()) {
            a = cmdLine.peek();
            switch (a.getKey().getString()) {
                case "--java-version":
                case "-java-version": {
                    javaVersion = cmdLine.nextString().getValue().getString();
                    break;
                }
                case "--java-home":
                case "-java-home": {
                    javaCommand = cmdLine.nextString().getValue().getString();
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    String r = cmdLine.nextString().getValue().getString();
                    addCp(currentCP, r);
                    break;
                }

                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    addNp(currentCP, cmdLine.nextString().getValue().getString());
                    break;
                }
                case "--main-class":
                case "-main-class":
                case "--class":
                case "-class": {
                    this.mainClass = cmdLine.nextString().getValue().getString();
                    break;
                }
                case "--dir":
                case "-dir": {
                    this.dir = cmdLine.nextString().getValue().getString();
                    break;
                }
                case "--win":
                case "--javaw": {
                    this.javaw = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--jar":
                case "-jar": {
                    this.jar = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--show-command":
                case "-show-command": {
                    this.showCommand = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--exclude-base":
                case "-exclude-base": {
                    this.excludeBase = cmdLine.nextBoolean().getBooleanValue();
                    break;
                }
                case "--add-module": {
                    this.j9_addModules.add(cmdLine.nextString().getStringValue());
                    break;
                }
                case "-m":
                case "--module": {
                    //<module>/<mainclass>
                    this.j9_module = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--module-path": {
                    this.j9_modulePath.add(cmdLine.nextString().getStringValue());
                    break;
                }
                case "-splash": {
                    this.splash = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--upgrade-module-path": {
                    this.j9_upgradeModulePath.add(cmdLine.nextString().getStringValue());
                    break;
                }
                case "--prepend-arg": {
                    prependArgs.add(cmdLine.nextString().getStringValue());
                    break;
                }
                case "--append-arg": {
                    appendArgs.add(cmdLine.nextString().getStringValue());
                    break;
                }
                case "-s":{
                    NutsArgument s = cmdLine.next();
                    getJvmArgs().add("-Dswing.aatext=true");
                    getJvmArgs().add("-Dawt.useSystemAAFontSettings=on");
                    getJvmArgs().add("-Dapple.laf.useScreenMenuBar=true");
                    getJvmArgs().add("-Dapple.awt.graphics.UseQuartz=true");
//                    getJvmArgs().add("-Dsun.java2d.noddraw=true");
//                    getJvmArgs().add("-Dsun.java2d.dpiaware=true");
                    break;
                }
                default: {
                    getJvmArgs().add(cmdLine.next().getString());
                }
            }
        }
        this.appArgs = new ArrayList<>();
        appArgs.addAll(prependArgs);
        appArgs.addAll(Arrays.asList(args));
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
        if (se.getIds().length > 0) {
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
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("cannot exclude base with jar modifier"));
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = NutsExecutionEntries.of(session).parse(path);
                    NutsExecutionEntry[] primary = Arrays.stream(classes).filter(NutsExecutionEntry::isDefaultEntry).toArray(NutsExecutionEntry[]::new);
                    if (primary.length > 0) {
                        mainClass = Arrays.stream(primary).map(NutsExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    } else if (classes.length > 0) {
                        mainClass = Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                .collect(Collectors.joining(":"));
                    }
                }
            } else if (!mainClass.contains(".")) {
                NutsExecutionEntry[] classes = NutsExecutionEntries.of(session).parse(path);
                List<String> possibleClasses = Arrays.stream(classes).map(NutsExecutionEntry::getName)
                        .collect(Collectors.toList());
                String r = resolveMainClass(mainClass, possibleClasses);
                if (r != null) {
                    mainClass = r;
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing Main Class for %s", id));
            }
            if (path != null && javaVersion == null) {
                NutsVersion version = JavaJarUtils.parseJarClassVersion(
                        NutsPath.of(path, session), session
                );
                if (version == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to detect java version for %s as %s", id, path));
                }
                javaVersion = version.toString();
            }
            NutsPlatformLocation nutsPlatformLocation = NutsJavaSdkUtils.of(session).resolveJdkLocation(getJavaVersion(), session);
            if (nutsPlatformLocation == null) {
                throw new NutsExecutionException(session, NutsMessage.cstyle("no java version %s was found", NutsUtilStrings.trim(getJavaVersion())), 1);
            }
            javaEffVersion = nutsPlatformLocation.getVersion();
            javaCommand = NutsJavaSdkUtils.of(session).resolveJavaCommandByVersion(nutsPlatformLocation, javaw, session);
            if (javaCommand == null) {
                throw new NutsExecutionException(session, NutsMessage.cstyle("no java version %s was found", getJavaVersion()), 1);
            }
            java9 = NutsVersion.of(javaVersion, session).compareTo("1.8") > 0;
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                NutsClassLoaderNode nn = null;
                if (nutsDefinition.getFile() != null) {
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
                if (path == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing path for %s", id));
                }
                currentCP.add(0, NutsClassLoaderUtils.definitionToClassLoaderNode(def, session));
//                nutsPath.add(0, nutsIdFormat.value(id).format());
//                classPath.add(0, path.toString());
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing Main-Class in Manifest for %s", id));
                    case 1:
                        //
                        break;
                    default: {
                        if (!session.isPlainOut()
                                || session.isBot()
//                                    || !session.isAsk()
                        ) {
                            throw new NutsExecutionException(session, NutsMessage.cstyle("multiple runnable classes detected : %s" + possibleClasses), 102);
                        }
                        NutsTexts text = NutsTexts.of(session);
                        NutsTextBuilder msgString = text.builder();

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
                                .forString(msgString.toString())
                                .setValidator((value, question) -> {
                                    Integer anyInt = CoreNumberUtils.convertToInteger(value, null);
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
            Integer v = CoreNumberUtils.convertToInteger(name, null);
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("ambiguous main-class %s matches all of %s",
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("ambiguous main-class %s matches all of from %s",
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
    public String[] getExecArgs() {
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
