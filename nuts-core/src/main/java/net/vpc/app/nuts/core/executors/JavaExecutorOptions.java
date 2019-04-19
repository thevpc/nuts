package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public class JavaExecutorOptions {
    private String javaVersion = null;//runnerProps.getProperty("java.version");
    private String javaHome = null;//runnerProps.getProperty("java.version");
    private String mainClass = null;
    private String dir = null;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreCommonUtils.getSystemBoolean("nuts.export.always-show-command",false);
    private boolean jar = false;
    private List<String> classPath = new ArrayList<>();
    private List<String> nutsPath = new ArrayList<>();
    private String[] execArgs;
    private List<String> jvmArgs = new ArrayList<String>();
    private NutsWorkspace ws;
    private List<String> app;
    private NutsDefinition nutsMainDef;
    private NutsSession session;

    public JavaExecutorOptions(NutsDefinition nutsMainDef, String[] args, String[] executorOptions, String dir, NutsWorkspace ws, NutsSession session) {
        this.session = session;
        this.nutsMainDef = nutsMainDef;
        this.ws = ws;
        this.app = new ArrayList<>(Arrays.asList(args));
        this.dir = dir;
        this.execArgs = executorOptions;
        List<String> classPath0 = new ArrayList<>();
        NutsIdFormat nutsIdFormat = ws.formatter().createIdFormat().setOmitNamespace(true);
        //will accept all -- and - based options!
        for (int i = 0; i < getExecArgs().length; i++) {
            String arg = getExecArgs()[i];
            if (arg.equals("--java-version") || arg.equals("-java-version")) {
                i++;
                javaVersion = getExecArgs()[i];
            } else if (arg.startsWith("--java-version=") || arg.startsWith("-java-version=")) {
                javaVersion = getExecArgs()[i].substring(arg.indexOf('=') + 1);

            } else if (arg.equals("--java-home") || arg.equals("-java-home")) {
                i++;
                javaHome = getExecArgs()[i];
            } else if (
                    arg.startsWith("--java-home=")
                            || arg.startsWith("-java-home=")
            ) {
                javaHome = getExecArgs()[i].substring(arg.indexOf('=') + 1);

            } else if (
                    arg.equals("--class-path") || arg.equals("--classpath") || arg.equals("--cp")
                            || arg.equals("-class-path") || arg.equals("-classpath") || arg.equals("-cp")
            ) {
                i++;
                addToCp(classPath0, getExecArgs()[i]);
            } else if (
                    arg.startsWith("--class-path=") || arg.startsWith("--classpath=") || arg.startsWith("--cp=")
                            || arg.startsWith("-class-path=") || arg.startsWith("-classpath=") || arg.startsWith("-cp=")
            ) {
                addToCp(classPath0, getExecArgs()[i].substring(arg.indexOf('=') + 1));

            } else if (arg.equals("--nuts-path") || arg.equals("--nutspath") || arg.equals("--np")) {
                i++;
                npToCp(classPath0, getExecArgs()[i]);
            } else if (arg.startsWith("--nuts-path=") || arg.startsWith("--nutspath=") || arg.startsWith("--np=")) {
                npToCp(classPath0, getExecArgs()[i].substring(arg.indexOf('=') + 1));
            } else if (arg.equals("--jar") || arg.equals("-jar")) {
                this.jar = true;
            } else if (arg.equals("--main-class") || arg.equals("-main-class") || arg.equals("--class") || arg.equals("-class")) {
                i++;
                this.mainClass = getExecArgs()[i];
            } else if (arg.startsWith("--main-class=") || arg.startsWith("-main-class=") || arg.startsWith("--class=")
                            || arg.startsWith("-class=")
            ) {
                this.mainClass = getExecArgs()[i].substring(arg.indexOf('=') + 1);
            } else if (arg.equals("--show-command") || arg.equals("-show-command")) {
                this.showCommand = true;
            } else if (arg.equals("--dir") || arg.equals("-dir")) {
                i++;
                this.dir = getExecArgs()[i];
            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
                this.dir = getExecArgs()[i].substring(arg.indexOf('=') + 1);
            } else if (arg.startsWith("--exclude-base")) {
                this.excludeBase = true;
            } else {
                getJvmArgs().add(arg);
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
        NutsDescriptor descriptor = nutsMainDef.getEffectiveDescriptor();
        nutsDefinitions.addAll(
                ws
                        .find().addId(descriptor.getId())
                        .setSession(session)
                        .setTransitive(true)
                        .addScope(NutsDependencyScope.PROFILE_RUN)
                        .setIncludeOptional(false)
                        .mainAndDependencies()
                        .getResultDefinitions().list()

        );

        if (this.jar) {
            if (this.mainClass != null) {
                session.getTerminal().ferr().printf("Ignored main-class=%s. running jar!\n", getMainClass());
            }
            if (!classPath0.isEmpty()) {
                session.getTerminal().ferr().printf("Ignored class-path=%s. running jar!\n", classPath0);
            }
            if(this.excludeBase) {
                throw new NutsIllegalArgumentException("Cannot exclude base with jar modifier");
            }
        }else{
            Path contentFile = nutsMainDef.getPath();
            if (mainClass == null) {
                if (contentFile != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = ws.parser().parseExecutionEntries(contentFile);
                    if (classes.length > 0) {
                        mainClass = CoreStringUtils.join(":", 
                                Arrays.stream(classes).map(NutsExecutionEntry::getName)
                                .collect(Collectors.toList())
                        );
                    }
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException("Missing Main Class for " + nutsMainDef.getId());
            }
            if(!isExcludeBase()) {
                nutsPath.add(nutsIdFormat.toString(nutsMainDef.getId()));
                classPath.add(contentFile.toString());
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
                        throw new NutsIllegalArgumentException("Missing Main-Class in Manifest for " + nutsMainDef.getId());
                    case 1:
                        //
                        break;
                    default:
                        while (true) {
                            PrintStream out = session.getTerminal().fout();
                            out.printf("Multiple runnable classes detected  - actually [[%s]] . Select one :\n", possibleClasses.size());
                            for (int i = 0; i < possibleClasses.size(); i++) {
                                out.printf("==[%s]== [[%s]]\n", (i + 1), possibleClasses.get(i));
                            }
                            String line = session.getTerminal().readLine("Enter class ==%s== or ==%s== to run it. Type @@%s@@ to cancel : ", "#", "name", "cancel");
                            if (line != null) {
                                if (line.equals("cancel")) {
                                    throw new NutsUserCancelException();
                                }
                                Integer anyInt = CoreCommonUtils.convertToInteger(line, null);
                                if (anyInt!=null) {
                                    int i = anyInt;
                                    if (i >= 1 && i <= possibleClasses.size()) {
                                        mainClass=possibleClasses.get(i - 1);
                                        break;
                                    }
                                } else {
                                    for (String possibleClass : possibleClasses) {
                                        if (possibleClass.equals(line)) {
                                            mainClass=possibleClass;
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
        NutsFindCommand ns = getWs().find().latestVersions()
                .setSession(this.session);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!CoreStringUtils.isBlank(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.getResultIds()) {
            NutsDefinition f = getWs()
                    .find().id(nutsId).setSession(this.session).setIncludeInstallInformation(true).latestVersions().getResultDefinitions().required();
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

    public NutsDefinition getNutsMainDef() {
        return nutsMainDef;
    }

    public NutsSession getSession() {
        return session;
    }
}
