package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Convert;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaExecutorOptions {
    private String javaVersion = null;//runnerProps.getProperty("java.version");
    private String javaHome = null;//runnerProps.getProperty("java.version");
    private String mainClass = null;
    private String dir = null;
    private boolean mainClassApp = false;
    private boolean excludeBase = false;
    private boolean showCommand = CoreNutsUtils.getSystemBoolean("nuts.export.always-show-command",false);
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
        NutsIdFormat nutsIdFormat = ws.getFormatManager().createIdFormat().setOmitNamespace(true);
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
            if (!StringUtils.isEmpty(getJavaVersion())) {
                javaHome = "${java#" + getJavaVersion() + "}";
            } else {
                javaHome = "${java}";
            }
        } else {
            javaHome = CoreNutsUtils.resolveJavaCommand(getJavaHome());
        }

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsDescriptor descriptor = nutsMainDef.getDescriptor();
        descriptor = ws.resolveEffectiveDescriptor(descriptor, session);
        nutsDefinitions.addAll(
                ws
                        .createQuery().addId(descriptor.getId())
                        .setSession(session.copy().setTransitive(true))
                        .addScope(NutsDependencyScope.PROFILE_RUN)
                        .setIncludeOptional(false)
                        .mainAndDependencies()
                        .fetch()

        );

        if (this.jar) {
            if (this.mainClass != null) {
                session.getTerminal().getFormattedErr().printf("Ignored main-class=%s. running jar!\n", getMainClass());
            }
            if (!classPath0.isEmpty()) {
                session.getTerminal().getFormattedErr().printf("Ignored class-path=%s. running jar!\n", classPath0);
            }
            if(this.excludeBase) {
                throw new NutsIllegalArgumentException("Cannot exclude base with jar modifier");
            }
        }else{
            String contentFile = nutsMainDef.getContent().getFile();
            if (mainClass == null) {
                File file = CoreIOUtils.fileByPath(contentFile);
                if (file != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = ws.getParseManager().parseExecutionEntries(file);
                    if (classes.length > 0) {
                        mainClass = StringUtils.join(":", classes, NutsExecutionEntry::getName);
                    }
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException("Missing Main Class for " + nutsMainDef.getId());
            }
            if(!isExcludeBase()) {
                nutsPath.add(nutsIdFormat.format(nutsMainDef.getId()));
                classPath.add(contentFile);
            }
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getContent().getFile() != null) {
                    classPath.add(nutsDefinition.getContent().getFile());
                    nutsPath.add(nutsIdFormat.format(nutsDefinition.getId()));
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
                            PrintStream out = session.getTerminal().getFormattedOut();
                            out.printf("Multiple runnable classes detected  - actually [[%s]] . Select one :\n", possibleClasses.size());
                            for (int i = 0; i < possibleClasses.size(); i++) {
                                out.printf("==[%s]== [[%s]]\n", (i + 1), possibleClasses.get(i));
                            }
                            String line = session.getTerminal().readLine("Enter class ==%s== or ==%s== to run it. Type @@%s@@ to cancel : ", "#", "name", "cancel");
                            if (line != null) {
                                if (line.equals("cancel")) {
                                    throw new NutsUserCancelException();
                                }
                                Integer anyInt = Convert.toInt(line, CoreNutsUtils.INTEGER_LENIENT_NULL);
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
            if (!StringUtils.isEmpty(n)) {
                classPath.add(n);
            }
        }
    }

    private void npToCp(List<String> classPath, String value) {
        NutsQuery ns = getWs().createQuery().setLatestVersions(true)
                .setSession(this.session);
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!StringUtils.isEmpty(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.find()) {
            NutsDefinition f = getWs()
                    .fetch(nutsId).setSession(this.session).setIncludeInstallInformation(true).fetchDefinition();
            classPath.add(f.getContent().getFile());
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
