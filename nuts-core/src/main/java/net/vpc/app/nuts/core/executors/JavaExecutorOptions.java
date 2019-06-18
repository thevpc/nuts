package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public class JavaExecutorOptions {

    private String javaVersion = null;//runnerProps.getProperty("java.parseVersion");
    private String javaHome = null;//runnerProps.getProperty("java.parseVersion");
    private String mainClass = null;
    private String dir = null;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreCommonUtils.getSysBoolNutsProperty("show-command", false);
    private boolean jar = false;
    private List<String> classPath = new ArrayList<>();
    private List<String> nutsPath = new ArrayList<>();
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private NutsWorkspace ws;
    private List<String> app;
//    private NutsDefinition nutsMainDef;
    private NutsSession session;

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsSession session) {
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
            if (!CoreNutsUtils.isEffectiveId(id)) {
                throw new NutsException(ws, "Id should be effective : " + id);
            }
            id = descriptor.getId();
        } else {
            descriptor = NutsWorkspaceUtils.getEffectiveDescriptor(ws, def);
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        Path path = def.getPath();
        this.session = session;
//        this.nutsMainDef = nutsMainDef;
        this.ws = session.getWorkspace();
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
        List<String> classPath0 = new ArrayList<>();
        NutsIdFormat nutsIdFormat = ws.format().id().setOmitNamespace(true);
        //will accept all -- and - based options!
        NutsCommandLine cmdLine = ws.commandLine().setArgs(getExecArgs());
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
            if (!CoreStringUtils.isBlank(getJavaVersion())) {
                javaHome = "${java#" + getJavaVersion() + "}";
            } else {
                javaHome = "${java}";
            }
        } else {
            javaHome = CoreIOUtils.resolveJavaCommand(getJavaHome());
        }

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsSearchCommand se = ws.search().session(session.copy().trace(false));
        if (tempId) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.getId());
            }
        } else {
            se.id(id);
        }
        if (se.getIds().length > 0) {
            nutsDefinitions.addAll(
                    se
                            .transitive()
                            .scope(NutsDependencyScope.PROFILE_RUN)
                            .optional(false)
                            .duplicates(false)
                            .latest()
                            .inlineDependencies()
                            .getResultDefinitions().list()
            );
        }
        if (this.jar) {
            if (this.mainClass != null) {
                if (session.isPlainOut()) {
                    session.getTerminal().ferr().printf("Ignored main-class=%s. running jar!%n", getMainClass());
                }
            }
            if (!classPath0.isEmpty()) {
                if (session.isPlainOut()) {
                    session.getTerminal().ferr().printf("Ignored class-path=%s. running jar!%n", classPath0);
                }
            }
            if (this.excludeBase) {
                throw new NutsIllegalArgumentException(ws, "Cannot exclude base with jar modifier");
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = ws.io().parseExecutionEntries(path);
                    if (classes.length > 0) {
                        mainClass = CoreStringUtils.join(":",
                                Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                        .collect(Collectors.toList())
                        );
                    }
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException(ws, "Missing Main Class for " + id);
            }
            boolean baseDetected = false;
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getPath() != null) {
                    if (id.getLongName().equals(nutsDefinition.getId().getLongName())) {
                        baseDetected = true;
                        if (!isExcludeBase()) {
                            classPath.add(nutsDefinition.getPath().toString());
                            nutsPath.add(nutsIdFormat.set(nutsDefinition.getId()).format());
                        }
                    } else {
                        classPath.add(nutsDefinition.getPath().toString());
                        nutsPath.add(nutsIdFormat.set(nutsDefinition.getId()).format());
                    }
                }
            }
            if (!isExcludeBase() && !baseDetected) {
                if (path == null) {
                    throw new NutsIllegalArgumentException(ws, "Missing Path for " + id);
                }
                nutsPath.add(0, nutsIdFormat.set(id).format());
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
                        throw new NutsIllegalArgumentException(ws, "Missing Main-Class in Manifest for " + id);
                    case 1:
                        //
                        break;
                    default:
                        if (!session.isPlainOut()) {
                            throw new NutsExecutionException(ws, "Multiple runnable classes detected : " + possibleClasses, 102);
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
                                .setValidator(new NutsResponseValidator<String>() {
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
                                        throw new NutsValidationException(ws);
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
        NutsSession searchSession = this.session.copy().trace(false);
        NutsSearchCommand ns = getWorkspace().search().latest()
                .setSession(searchSession);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = getWorkspace()
                    .search().id(nutsId).setSession(searchSession).installInformation().latest().getResultDefinitions().required();
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
        return ws;
    }

    public List<String> getApp() {
        return app;
    }

//    public NutsDefinition getNutsMainDef() {
//        return nutsMainDef;
//    }
    public NutsSession getSession() {
        return session;
    }
}
