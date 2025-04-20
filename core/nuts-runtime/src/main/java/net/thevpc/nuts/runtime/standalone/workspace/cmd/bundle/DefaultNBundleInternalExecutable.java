/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell.NShellWriter;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.util.*;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNBundleInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNBundleInternalExecutable(String[] args, NExecCmd execCommand) {
        super("bundle", args, execCommand);
    }

    private NOptional<String> ensureValidFileName(String any) {
        if (any != null) {
            StringBuilder sb = new StringBuilder();
            for (char c : any.toCharArray()) {
                if (
                        (c >= 'a' && c <= 'z')
                                || (c >= 'A' && c <= 'Z')
                                || (c >= '0' && c <= '9')
                                || (c == '_')
                                || (c == '-')
                                || (c == '+')
                                || (c == '.')
                ) {
                    sb.append(c);
                } else {
                    sb.append("_");
                }
            }
            if (sb.length() > 0) {
                return NOptional.of(sb.toString());
            }
        }
        return NOptional.ofNamedEmpty("file name");
    }

    @Override
    public int execute() {
        NChronometer allChrono = NChronometer.startNow();
        NSession session = NSession.of();
        if (session.isDry()) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NTrace.println(NMsg.ofC("nuts %s v%s",
                NMsg.ofStyledPrimary1("bundle builder"),
                NWorkspace.of().getRuntimeId().getVersion()));
        BOptions boptions = new BOptions();
        NCmdLine cmdLine = NCmdLine.of(args);
        new BOptionsParser().parseBOptions(boptions, cmdLine);
        NutsBundleFilesConfig nuts_bundle_files_config = new NutsBundleFilesConfig();
        NutsBundleInfoConfig nuts_bundle_info_config = new NutsBundleInfoConfig();
        String repo = "${target}/.nuts-bundle/lib";
        NPath rootFolder = null;
        NPath bundleFolder = null;
        boolean tempBundleFolder = false;
        BundleType format = boptions.format;
        if (format == null) {
            format = BundleType.JAR;
        }
        boolean includeConfigFiles = true;

        ResultingIds resultingIds = new ResultingIds();

        NTrace.println(NMsg.ofC(NI18n.of("computing dependencies for %s"), boptions.ids));
        NChronometer chrono = NChronometer.startNow();
        resultingIds
                .addAllId(boptions.ids.toArray(new String[0]))
                .addAllLibs(boptions.lib.toArray(new String[0]))
                .build();


        cmdLine.throwUnexpectedArgument(NMsg.ofC("%s", boptions.ids));
        chrono.stop();
        NTrace.println(NMsg.ofC(NI18n.of("found %s deps and %s apps in %s..."), resultingIds.classPath.size(), resultingIds.executableAppIds.size(), chrono.getDuration()));
        String defaultName = "nuts-bundle";
        String defaultVersion = "1.0";
        if (resultingIds.executableAppIds.size() == 1) {
            for (NId executableAppId : resultingIds.executableAppIds) {
                defaultName = preferredAppName(executableAppId);
                defaultVersion = executableAppId.getVersion().toString();
                break;
            }
        } else if (resultingIds.executableAppIds.size() > 1) {
            NId[] explicitIds = resultingIds.executableAppIds.stream().filter(x -> {
                for (String id : boptions.ids) {
                    if (Objects.equals(NId.of(id).getShortName(), x.getShortName())) {
                        return true;
                    }
                }
                for (String id : boptions.ids) {
                    if (Objects.equals(id, x.getArtifactId())) {
                        return true;
                    }
                }
                return false;
            }).toArray(NId[]::new);
            if (explicitIds.length == 1) {
                defaultName = preferredAppName(explicitIds[0]);
                defaultVersion = explicitIds[0].getVersion().toString();
            } else {
                for (NId executableAppId : resultingIds.executableAppIds) {
                    defaultName = preferredAppName(executableAppId) + "-and-all";
                    defaultVersion = executableAppId.getVersion().toString();
                    break;
                }
            }
        }
        nuts_bundle_info_config.appName = NStringUtils.firstNonBlank(boptions.appName, boptions.appTitle, defaultName);
        nuts_bundle_info_config.appVersion = NStringUtils.firstNonBlank(boptions.appVersion, defaultVersion, "1.0");
        nuts_bundle_info_config.appTitle = NStringUtils.firstNonBlank(boptions.appTitle, nuts_bundle_info_config.appName);
        nuts_bundle_info_config.appDesc = NStringUtils.firstNonBlank(boptions.appDesc, nuts_bundle_info_config.appTitle);
        String fullAppFileName = ensureValidFileName(nuts_bundle_info_config.appName).orElse("app") + "-" + ensureValidFileName(nuts_bundle_info_config.appVersion).orElse("1.0");
        nuts_bundle_info_config.target = "${user.dir}/" + fullAppFileName;

        switch (format) {
            case JAR:
            case ZIP: {
                rootFolder = NPath.ofTempFolder("bundle");
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
                break;
            }
            case EXPLODED: {
                rootFolder = NBlankable.isBlank(boptions.withTarget) ?
                        NPath.ofUserDirectory().resolve(fullAppFileName + "-bundle")
                        : NPath.of(boptions.withTarget)
                ;
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
                if (boptions.clean) {
                    if (bundleFolder.isDirectory()) {
                        for (NPath nPath : bundleFolder.list()) {
                            nPath.deleteTree();
                        }
                    }
                    for (String s : new String[]{
                            "META-INF/nuts-bundle-info.config",
                            "META-INF/nuts-bundle-files.config"
                    }) {
                        NPath p = rootFolder.resolve(s);
                        if (p.isRegularFile()) {
                            p.delete();
                        }
                    }
                }
                break;
            }
            case DIR: {
                rootFolder = NBlankable.isBlank(boptions.withTarget) ?
                        NPath.ofUserDirectory().resolve(fullAppFileName + "-bundle")
                        : NPath.of(boptions.withTarget)
                ;
                bundleFolder = rootFolder;
                if (boptions.clean) {
                    if (rootFolder.isDirectory()) {
                        for (NPath nPath : rootFolder.list()) {
                            nPath.deleteTree();
                        }
                    }
                }
                includeConfigFiles = false;
                break;
            }
            default: {
                cmdLine.throwUnexpectedArgument(NMsg.ofC("invalid format %s", format));
            }
        }
        bundleFolder.mkdirs();

        NCp cp = NCp.of();
        if ("jar".equals(format)) {
            cp
                    .from(getClass().getResource("/META-INF/bundle/NutsBundleRunner.class.template"))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("net/thevpc/nuts/runtime/standalone/installer/NutsBundleRunner.class"))
                    .run();
            cp
                    .from(getClass().getResource("/META-INF/bundle/MANIFEST-COPY.MF"))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("META-INF/MANIFEST.MF"))
                    .run();
        }

        for (NDefinition d : resultingIds.classPath.values()) {
            NId id = d.getId();
            String fullPath = id.getMavenPath("jar");
            if (d.getContent().isPresent()) {
                cp.from(d.getContent().get())
                        .to(bundleFolder.resolve(fullPath))
                        .run();
                if (includeConfigFiles) {
                    nuts_bundle_files_config.install(
                            "/" + fullPath
                            , repo + "/" + fullPath
                    );
                    NTrace.println(NMsg.ofC(NI18n.of("adding to classpath %s"), id));
                }
            }

            fullPath = id
                    //descriptor is not classifier aware
                    .builder().setClassifier(null).build()
                    .getMavenPath(NConstants.Files.DESCRIPTOR_FILE_EXTENSION_SIMPLE);
            cp.from(NDescriptorFormat.of().setValue(d.getDescriptor()).setNtf(false).toString().getBytes())
                    .to(bundleFolder.resolve(fullPath))
                    .run();
            if (includeConfigFiles) {
                nuts_bundle_files_config.install("/" + fullPath
                        , repo + "/" + fullPath
                );
            }
        }
        NTrace.println(NMsg.ofC(NI18n.of("resolving workspace runner as %s"), resultingIds.findNutsAppId()));
        bundleFolder.resolve(".nuts-repository").writeString("{}");
        if (includeConfigFiles) {
            NTrace.println(NMsg.ofC(NI18n.of("building repository")));
            nuts_bundle_files_config.install("/.nuts-repository", repo + "/.nuts-repository");
            for (NId executableAppId : resultingIds.executableAppIds) {
                NDefinition d = resultingIds.classPath.get(executableAppId);
                int minJava = 8;
                boolean gui = d.getDescriptor().getFlags().contains(NDescriptorFlag.GUI);
                for (String s : d.getDescriptor().getCondition().getPlatform()) {
                    NId id = NId.get(s).orNull();
                    if (NJavaSdkUtils.isJava(id)) {
                        minJava = NJavaSdkUtils.normalizeJavaVersionAsInt(id.getVersion());
                    }
                }
                NTrace.println(NMsg.ofC(NI18n.of("building executable script for %s"), executableAppId));
                createAppScripts(executableAppId, resultingIds.findNutsAppId(), bundleFolder, nuts_bundle_files_config, boptions, minJava, gui);
            }
            rootFolder.resolve("META-INF/nuts-bundle-files.config").writeString(nuts_bundle_files_config.toString());
        }


        if (includeConfigFiles) {
            rootFolder.resolve("META-INF/nuts-bundle-info.config").writeString(nuts_bundle_info_config.toString());
        }

        NSession nSession = NSession.of();
        switch (format) {
            case JAR: {
                NCompress zip = NCompress.of().setPackaging("zip");
                NPath target = NPath.of(NStringUtils.firstNonBlank(boptions.withTarget,
                        fullAppFileName
                                + "-bundle"
                                + ".jar")).toAbsolute();
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(
                                target
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            case ZIP: {
                NCompress zip = NCompress.of().setPackaging("zip");
                NPath target = NPath.of(NStringUtils.firstNonBlank(boptions.withTarget,
                        fullAppFileName
                                + "-bundle"
                                + ".zip")).toAbsolute();
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(target
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            case DIR:
            case EXPLODED: {
                NPath target = rootFolder.toAbsolute();
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            default: {
                cmdLine.throwError(NMsg.ofC("invalid format %s", format));
            }
        }
        return NExecutionException.SUCCESS;
    }


    private String preferredAppName(NId mainIdStr) {
        if (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName())) {
            return "nuts";
        }
        return mainIdStr.getArtifactId();
    }

    private void createAppScripts(NId mainIdStr, NId nutsId, NPath bundleFolder,
                                  NutsBundleFilesConfig nuts_bundle_files_config, BOptions options,
                                  int minJavaVersion,
                                  boolean javaw
    ) {
        if (mainIdStr == null) {
            return;
        }
        String appName = preferredAppName(mainIdStr);
        for (NOsFamily osFamily : new NOsFamily[]{NOsFamily.UNIX, NOsFamily.LINUX, NOsFamily.MACOS, NOsFamily.WINDOWS}) {
            NShellFamily shellFamily = NShellFamily.SH;
            switch (osFamily) {
                case WINDOWS:
                    shellFamily = NShellFamily.WIN_CMD;
                    break;
                case LINUX:
                    shellFamily = NShellFamily.BASH;
                    break;
                case MACOS:
                    shellFamily = NShellFamily.ZSH;
                    break;
                case UNIX:
                    shellFamily = NShellFamily.SH;
                    break;
            }
            NShellWriter out = NShellWriter.of(shellFamily).get();
            String dotExe = osFamily == NOsFamily.WINDOWS ? ".exe" : "";
            String dotBatOrSh = osFamily == NOsFamily.WINDOWS ? ".bat" : ".sh";
            String dotBatOrNothing = osFamily == NOsFamily.WINDOWS ? ".bat" : "";
            out
                    .printlnComment("-------------------------------------")
                    .printlnComment(" Nuts Bundle Launcher Script " + NWorkspace.of().getRuntimeId().getVersion())
                    .printlnComment(" This bundle was created for " + mainIdStr.getShortName())
                    .printlnComment(" (c) 2025 thevpc")
                    .printlnComment("-------------------------------------")
                    .println()
                    .echoOff()
                    .printlnComment("resolve current script path")
                    .printlnSetVarScriptPath("NS_SCRIPT_PATH")
                    .printlnSetVarFolderPath("NS_SCRIPT_DIR", "NS_SCRIPT_PATH")
                    .printlnSetVar("NS_WS_JAR", "$NS_SCRIPT_DIR/.nuts-bundle/lib/" + nutsId.getMavenPath("jar"))
                    .printlnSetVar("NS_JAVA_OPTIONS", "")
                    .printlnPrepareJavaCommand("NS_JAVA", "NS_JAVA_HOME", minJavaVersion, javaw)
                    .println()
                    .printlnComment("resolve workspace options")
                    .printlnSetVar("NS_WS_OPTIONS", "--repo==$NS_SCRIPT_DIR/.nuts-bundle/lib -w=$NS_SCRIPT_DIR/.nuts-bundle/ws")
                    .printlnComment("add workspace isolation options")
                    .printlnSetVar("NS_WS_OPTIONS", "$NS_WS_OPTIONS ---m2=false --desktop-launcher=unsupported --menu-launcher=unsupported --user-launcher=unsupported --!switch --!init-platforms --!init-scripts --!init-launchers --!install-companions")
                    .printlnComment("add other options like --verbose")
                    .printlnComment("--verbose : for more logging")
                    .printlnComment("-Zy      : to reset the whole workspace")
                    .setDisableCommands(!options.verbose).printlnSetAppendVar("NS_WS_OPTIONS", " --verbose")
                    .setDisableCommands(!options.yes).printlnSetAppendVar("NS_WS_OPTIONS", " --yes")
                    .setDisableCommands(!options.reset).printlnSetAppendVar("NS_WS_OPTIONS", " --reset")
                    .setDisableCommands(!options.embedded).printlnSetAppendVar("NS_WS_OPTIONS", " -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
                    .setDisableCommands(false)
                    .printlnComment("")
                    .printlnComment("add other JVM options like for debug mode")
                    .setDisableCommands(true).printlnSetAppendVar("NS_JAVA_OPTIONS", " --embedded")
                    .setDisableCommands(false)
                    .println()
                    .printlnCommand("$NS_JAVA" + dotExe + " $NS_JAVA_OPTIONS -jar \"$NS_WS_JAR\" $NS_WS_OPTIONS " +
                            (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                            + " ${*}")
            ;

            String scriptInternalPath = appName + "-" + osFamily.id() + "-runner" + dotBatOrSh;
            bundleFolder.resolve(scriptInternalPath).writeString(out.build());
            nuts_bundle_files_config.installExecutable(osFamily, "/" + scriptInternalPath, "${target}/" + appName + dotBatOrNothing);
        }
    }


}
