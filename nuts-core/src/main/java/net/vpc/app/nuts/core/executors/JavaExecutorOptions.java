package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public class JavaExecutorOptions {

    private String javaVersion = null;//runnerProps.getProperty("java.version");
    private String javaHome = null;//runnerProps.getProperty("java.version");
    private String mainClass = null;
    private String dir = null;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreCommonUtils.getSystemBoolean("nuts.export.always-show-command", false);
    private boolean jar = false;
    private List<String> classPath = new ArrayList<>();
    private List<String> nutsPath = new ArrayList<>();
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private NutsWorkspace ws;
    private List<String> app;
//    private NutsDefinition nutsMainDef;
    private NutsSession session;

    public JavaExecutorOptions(NutsDefinition def, boolean tempId, String[] args, String[] executorOptions, String dir, NutsWorkspace ws, NutsSession session) {
        NutsId id = def.getId();
        NutsDescriptor descriptor = null;
        if (tempId) {
            descriptor = def.getDescriptor();
            if (!CoreNutsUtils.isEffectiveId(id)) {
                if (tempId) {
                    throw new NutsException(ws, "Id should be effective : " + id);
                }
                id = descriptor.getId();
            }
        } else {
            descriptor = def.getEffectiveDescriptor();
            if (!CoreNutsUtils.isEffectiveId(id)) {
                id = descriptor.getId();
            }
        }
        if (!CoreNutsUtils.isEffectiveId(id)) {
            if (tempId) {
                throw new NutsException(ws, "Id should be effective : " + id);
            }
            id = def.getEffectiveDescriptor().getId();
        }
        Path path = def.getPath();
        this.session = session;
//        this.nutsMainDef = nutsMainDef;
        this.ws = ws;
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
        List<String> classPath0 = new ArrayList<>();
        NutsIdFormat nutsIdFormat = ws.formatter().createIdFormat().setOmitNamespace(true);
        //will accept all -- and - based options!
        NutsCommand cmdLine = ws.parser().parseCommand(getExecArgs());
        NutsArgument a;
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
                    javaHome = cmdLine.nextString().getValue().getString();
                    break;
                }
                case "--class-path":
                case "-class-path":
                case "--classpath":
                case "-classpath":
                case "--cp":
                case "-cp": {
                    addToCp(classPath0, cmdLine.nextString().getValue().getString());
                    break;
                }
                case "--nuts-path":
                case "-nuts-path":
                case "--nutspath":
                case "-nutspath":
                case "--np":
                case "-np": {
                    npToCp(classPath0, cmdLine.nextString().getValue().getString());
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
                case "--jar":
                case "-jar": {
                    this.jar = cmdLine.nextBoolean().getValue().getBoolean();
                    break;
                }
                case "--show-command":
                case "-show-command": {
                    this.showCommand = cmdLine.nextBoolean().getValue().getBoolean();
                    break;
                }
                case "--exclude-base":
                case "-exclude-base": {
                    this.excludeBase = cmdLine.nextBoolean().getValue().getBoolean();
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
        NutsSearchCommand se = ws.search();
        if (tempId) {
            for (NutsDependency dependency : descriptor.getDependencies()) {
                se.addId(dependency.getId());
            }
        } else {
            se.id(id);
            nutsDefinitions.addAll(
                    se
                            .setSession(session)
                            .setTransitive(true)
                            .addScope(NutsDependencyScope.PROFILE_RUN)
                            .optional(false)
                            .duplicates(false)
                            .latest(false)
                            .inlineDependencies()
                            .getResultDefinitions().list()
            );
        }
        if (se.getIds().length > 0) {
            nutsDefinitions.addAll(
                    se
                            .setSession(session)
                            .setTransitive(true)
                            .addScope(NutsDependencyScope.PROFILE_RUN)
                            .optional(false)
                            .duplicates(false)
                            .latest(false)
                            .inlineDependencies()
                            .getResultDefinitions().list()
            );
        }
        if (this.jar) {
            if (this.mainClass != null) {
                session.getTerminal().ferr().printf("Ignored main-class=%s. running jar!%n", getMainClass());
            }
            if (!classPath0.isEmpty()) {
                session.getTerminal().ferr().printf("Ignored class-path=%s. running jar!%n", classPath0);
            }
            if (this.excludeBase) {
                throw new NutsIllegalArgumentException(ws, "Cannot exclude base with jar modifier");
            }
        } else {
            if (mainClass == null) {
                if (path != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = ws.parser().parseExecutionEntries(path);
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
            if (!isExcludeBase()) {
                if (path == null) {
                    throw new NutsIllegalArgumentException(ws, "Missing Path for " + id);
                }
                nutsPath.add(nutsIdFormat.toString(id));
                classPath.add(path.toString());
            }
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getPath() != null) {
                    classPath.add(nutsDefinition.getPath().toString());
                    nutsPath.add(nutsIdFormat.toString(nutsDefinition.getId()));
                }
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
                        while (true) {
                            PrintStream out = session.getTerminal().fout();
                            out.printf("Multiple runnable classes detected  - actually [[%s]] . Select one :%n", possibleClasses.size());
                            for (int i = 0; i < possibleClasses.size(); i++) {
                                out.printf("==[%s]== [[%s]]%n", (i + 1), possibleClasses.get(i));
                            }
                            String line = session.getTerminal().readLine("Enter class ==%s== or ==%s== to run it. Type @@%s@@ to cancel : ", "#", "name", "cancel");
                            if (line != null) {
                                if (line.equals("cancel")) {
                                    throw new NutsUserCancelException(ws);
                                }
                                Integer anyInt = CoreCommonUtils.convertToInteger(line, null);
                                if (anyInt != null) {
                                    int i = anyInt;
                                    if (i >= 1 && i <= possibleClasses.size()) {
                                        mainClass = possibleClasses.get(i - 1);
                                        break;
                                    }
                                } else {
                                    for (String possibleClass : possibleClasses) {
                                        if (possibleClass.equals(line)) {
                                            mainClass = possibleClass;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
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
        NutsSearchCommand ns = getWs().search().latest()
                .setSession(this.session);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = getWs()
                    .search().id(nutsId).setSession(this.session).installInformation().latest().getResultDefinitions().required();
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

    public NutsWorkspace getWs() {
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
