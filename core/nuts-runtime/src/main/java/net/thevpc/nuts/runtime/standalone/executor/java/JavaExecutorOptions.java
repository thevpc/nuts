package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.*;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.extension.NutsClassLoaderUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaCommand = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private String dir = null;
    private boolean javaw = false;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand;
    private boolean jar = false;
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private List<String> app;
    //    private NutsDefinition nutsMainDef;
    private NutsSession session;
    private List<NutsClassLoaderNode> classPath = new ArrayList<>();

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsSession session) {
        this.session = session;
        showCommand=getSession().boot().getBootCustomArgument("---show-command").getBooleanValue(false);
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
//            if (!CoreNutsUtils.isEffectiveId(id)) {
//                throw new NutsException(session, NutsMessage.cstyle("id should be effective : %s", id));
//            }
            id = descriptor.getId();
        } else {
            descriptor = NutsWorkspaceUtils.of(getSession()).getEffectiveDescriptor(def);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getFile();
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
//        List<String> classPath0 = new ArrayList<>();
//        List<NutsClassLoaderNode> extraCp = new ArrayList<>();
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = NutsCommandLine.of(getExecArgs(),session).setExpandSimpleOptions(false);
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
                default: {
                    getJvmArgs().add(cmdLine.next().getString());
                }
            }
        }
        if (getJavaCommand() == null) {
            if (javaw) {
                if (!NutsBlankable.isBlank(getJavaVersion())) {
                    javaCommand = "${javaw#" + getJavaVersion() + "}";
                } else {
                    javaCommand = "${javaw}";
                }
            } else {
                if (!NutsBlankable.isBlank(getJavaVersion())) {
                    javaCommand = "${java#" + getJavaVersion() + "}";
                } else {
                    javaCommand = "${java}";
                }
            }
        } else {
            javaCommand = NutsJavaSdkUtils.of(session).resolveJavaCommandByHome(getJavaCommand(), session);
        }

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
                    .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(session))
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
                    if (classes.length > 0) {
                        mainClass = String.join(":",
                                Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                        .collect(Collectors.toList())
                        );
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
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getFile() != null) {
                    if (id.getLongName().equals(nutsDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            currentCP.add(NutsClassLoaderUtils.definitionToClassLoaderNode(nutsDefinition, session));
//                            classPath.add(nutsDefinition.getPath().toString());
//                            nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                        }
                    } else {
                        currentCP.add(NutsClassLoaderUtils.definitionToClassLoaderNode(nutsDefinition, session));
//                        classPath.add(nutsDefinition.getPath().toString());
//                        nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                    }
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                if (path == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing path for %s",id));
                }
                currentCP.add(0, NutsClassLoaderUtils.definitionToClassLoaderNode(def, session));
//                nutsPath.add(0, nutsIdFormat.value(id).format());
//                classPath.add(0, path.toString());
            }
            classPath.addAll(currentCP);
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
                }else if(v<0){
                    int i=possibleClasses.size()+v;
                    if (i >= 0 && i < possibleClasses.size()) {
                        return possibleClasses.get(i);
                    }
                }
            } else {
                if (possibleClasses.contains(name)) {
                    return name;
                } else {
                    List<String> extraPossibilities=new ArrayList<>();
                    for (String possibleClass : possibleClasses) {
                        int x = possibleClass.lastIndexOf('.');
                        if (x > 0) {
                            if (possibleClass.substring(x + 1).equals(name)) {
                                extraPossibilities.add(possibleClass);
                            }
                        }
                    }
                    if(extraPossibilities.size()==1){
                        return extraPossibilities.get(0);
                    }
                    if(extraPossibilities.size()>1){
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("ambiguous main-class %s matches all of %s",
                                name,extraPossibilities.toString()
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
                    if(extraPossibilities.size()==1){
                        return extraPossibilities.get(0);
                    }
                    if(extraPossibilities.size()>1){
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("ambiguous main-class %s matches all of from %s",
                                name,extraPossibilities.toString()
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
            for (String n : StringTokenizerUtils.split(value, ":")) {
                if (!NutsBlankable.isBlank(n)) {
                    File f = CoreIOUtils.toFile(n);
                    if (f == null) {
                        throw new IllegalArgumentException("invalid path " + n);
                    }
                    URL url = null;
                    try {
                        url = f.toURI().toURL();
                    } catch (MalformedURLException ex) {
                        throw new IllegalArgumentException("invalid url " + url);
                    }
                    classPath.add(new NutsClassLoaderNode("", url, true,true));
                }
            }
        }

    }

    private void addNp(List<NutsClassLoaderNode> classPath, String value) {
        NutsSession searchSession = this.session;
        NutsSearchCommand ns = session.search().setLatest(true)
                .setSession(searchSession);
        for (String n : StringTokenizerUtils.split(value, ";, ")) {
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

    public List<String> getApp() {
        return app;
    }

    public NutsSession getSession() {
        return session;
    }

    public void fillStrings(NutsClassLoaderNode n, List<String> list) {
        URL f = n.getURL();
        list.add(CoreIOUtils.toFile(f).getPath());
        for (NutsClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }

    public List<String> getClassPathStrings() {
        List<String> li = new ArrayList<>();
        for (NutsClassLoaderNode n : getClassPath()) {
            fillStrings(n, li);
        }
        return li;
    }

    public void fillNidStrings(NutsClassLoaderNode n, List<String> list) {
        if (n.getId() == null || n.getId().isEmpty()) {
            URL f = n.getURL();
            list.add(CoreIOUtils.toFile(f).getPath());
        } else {
            list.add(n.getId());
        }
        for (NutsClassLoaderNode d : n.getDependencies()) {
            fillStrings(d, list);
        }
    }

    public List<String> getClassPathNidStrings() {
        List<String> li = new ArrayList<>();
        for (NutsClassLoaderNode n : getClassPath()) {
            fillNidStrings(n, li);
        }
        return li;
    }

    public List<NutsClassLoaderNode> getClassPath() {
        return classPath;
    }

}
