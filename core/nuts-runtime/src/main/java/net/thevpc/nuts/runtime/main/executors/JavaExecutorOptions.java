package net.thevpc.nuts.runtime.main.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.NutsJavaSdkUtils;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class JavaExecutorOptions {

    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaHome = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private String dir = null;
    private boolean javaw = false;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreCommonUtils.getSysBoolNutsProperty("show-command", false);
    private boolean jar = false;
    private List<String> classPath = new ArrayList<>();
    private List<String> nutsPath = new ArrayList<>();
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private List<String> app;
//    private NutsDefinition nutsMainDef;
    private NutsSession session;

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsSession session) {
        this.session = session;
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
            if (!CoreNutsUtils.isEffectiveId(id)) {
                throw new NutsException(getWorkspace(), "Id should be effective : " + id);
            }
            id = descriptor.getId();
        } else {
            descriptor = NutsWorkspaceUtils.of(getWorkspace()).getEffectiveDescriptor( def);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getPath();
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
        List<String> classPath0 = new ArrayList<>();
        NutsIdFormat nutsIdFormat = getWorkspace().id().formatter().omitNamespace();
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = getWorkspace().commandLine().create(getExecArgs()).setExpandSimpleOptions(false);
        NutsArgument a;
        while (cmdLine.hasNext()) {
            a = cmdLine.peek();
            switch (a.getStringKey()) {
                case "--java-version":
                case "-java-version": {
                    javaVersion = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--java-home":
                case "-java-home": {
                    javaHome = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    addToCp(classPath0, cmdLine.nextString().getStringValue());
                    break;
                }
                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    npToCp(classPath0, cmdLine.nextString().getStringValue());
                    break;
                }
                case "--main-class":
                case "-main-class":
                case "--class":
                case "-class": {
                    this.mainClass = cmdLine.nextString().getStringValue();
                    break;
                }
                case "--dir":
                case "-dir": {
                    this.dir = cmdLine.nextString().getStringValue();
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
        if (getJavaHome() == null) {
            if(javaw){
                if (!CoreStringUtils.isBlank(getJavaVersion())) {
                    javaHome = "${javaw#" + getJavaVersion() + "}";
                } else {
                    javaHome = "${javaw}";
                }
            }else {
                if (!CoreStringUtils.isBlank(getJavaVersion())) {
                    javaHome = "${java#" + getJavaVersion() + "}";
                } else {
                    javaHome = "${java}";
                }
            }
        } else {
            javaHome = NutsJavaSdkUtils.of(session.getWorkspace()).resolveJavaCommandByHome(getJavaHome());
        }

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsSearchCommand se = getWorkspace().search().setSession(CoreNutsUtils.silent(session));
        if (tempId) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.toId());
            }
        } else {
            se.addId(id);
        }
        if (se.getIds().length > 0) {
            nutsDefinitions.addAll(
                    se
                            .setTransitive(true)
                            .addScope(NutsDependencyScopePattern.RUN)
                            .setOptional(false)
                            .setDistinct(true)
                            .setContent(true)
                            .setDependencies(true)
                            .setLatest(true)
                            .setInlineDependencies(true)
                            .getResultDefinitions().list()
            );
        }
        if (this.jar) {
            if (this.mainClass != null) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("Ignored main-class=%s. running jar!%n", getMainClass());
                }
            }
            if (!classPath0.isEmpty()) {
                if (session.isPlainOut()) {
                    session.getTerminal().err().printf("Ignored class-path=%s. running jar!%n", classPath0);
                }
            }
            if (this.excludeBase) {
                throw new NutsIllegalArgumentException(getWorkspace(), "Cannot exclude base with jar modifier");
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = getWorkspace().apps().execEntries().parse(path);
                    if (classes.length > 0) {
                        mainClass = String.join(":",
                                Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                        .collect(Collectors.toList())
                        );
                    }
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException(getWorkspace(), "Missing Main Class for " + id);
            }
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getPath() != null) {
                    if (id.getLongName().equals(nutsDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            classPath.add(nutsDefinition.getPath().toString());
                            nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                        }
                    } else {
                        classPath.add(nutsDefinition.getPath().toString());
                        nutsPath.add(nutsIdFormat.value(nutsDefinition.getId()).format());
                    }
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                if (path == null) {
                    throw new NutsIllegalArgumentException(getWorkspace(), "Missing Path for " + id);
                }
                nutsPath.add(0, nutsIdFormat.value(id).format());
                classPath.add(0, path.toString());
            }
            for (String cp : classPath0) {
                classPath.add(cp);
                nutsPath.add(cp);
            }
            if (this.mainClass.contains(":")) {
                List<String> possibleClasses = CoreStringUtils.split(getMainClass(), ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NutsIllegalArgumentException(getWorkspace(), "Missing Main-Class in Manifest for " + id);
                    case 1:
                        //
                        break;
                    default:
                        if (!session.isPlainOut()) {
                            throw new NutsExecutionException(getWorkspace(), "Multiple runnable classes detected : " + possibleClasses, 102);
                        }
                        StringBuilder msgString = new StringBuilder();
                        List<Object> msgParams = new ArrayList<>();

                        msgString.append("Multiple runnable classes detected  - actually [[%s]] . Select one :%n");
                        msgParams.add(possibleClasses.size());
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            msgString.append("==[%s]== [[%s]]%n");
                            msgParams.add((i + 1));
                            msgParams.add(possibleClasses.get(i));
                        }
                        msgString.append("Enter class ==%s== or ==%s== to run it. Type @@%s@@ to cancel : ");
                        msgParams.add("#");
                        msgParams.add("name");
                        msgParams.add("cancel!");

                        mainClass = session.getTerminal()
                                .ask().forString(msgString.toString(), msgParams.toArray())
                                .setValidator(new NutsQuestionValidator<String>() {
                                    @Override
                                    public String validate(String value, NutsQuestion<String> question) throws NutsValidationException {
                                        Integer anyInt = CoreCommonUtils.convertToInteger(value, null);
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
                                        throw new NutsValidationException(getWorkspace());
                                    }
                                }).getValue();
                        break;
                }
            }
        }

    }

    private void addToCp(List<String> classPath, String value) {
        for (String n : CoreStringUtils.split(value, ":;, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                classPath.add(n);
            }
        }
    }

    private void npToCp(List<String> classPath, String value) {
        NutsSession searchSession = CoreNutsUtils.silent(this.session);
        NutsSearchCommand ns = getWorkspace().search().setLatest(true)
                .setSession(searchSession);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = getWorkspace()
                    .search().addId(nutsId).setSession(searchSession).setLatest(true).getResultDefinitions().required();
            classPath.add(f.getPath().toString());
        }
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaHome() {
        return javaHome;
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

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getNutsPath() {
        return nutsPath;
    }

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
}
