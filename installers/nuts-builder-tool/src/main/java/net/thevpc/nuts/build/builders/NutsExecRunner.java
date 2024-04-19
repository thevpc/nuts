/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.NEnvs;
import net.thevpc.nuts.NExecCmd;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NInstallCmd;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.build.util.BinPlatform;
import net.thevpc.nuts.build.util.Mvn;
import net.thevpc.nuts.build.util.MvnArtifactType;
import net.thevpc.nuts.build.util.NParams;
import net.thevpc.nuts.build.util.NamedStringParam;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.env.NArchFamily;
import static net.thevpc.nuts.env.NOsFamily.LINUX;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NBlankable;

/**
 *
 * @author vpc
 */
public class NutsExecRunner extends AbstractRunner {

    NamedStringParam NUTS_JAVA_HOME = NParams.ofString("NUTS_JAVA_HOME", "/usr/lib64/jvm/java-1.8.0-openjdk-1.8.0");
    NamedStringParam NUTS_INSTALLER_BUILD_JAVA_HOME = NParams.ofString("NUTS_INSTALLER_BUILD_JAVA_HOME", "/usr/lib64/jvm/java-17-openjdk");
    NamedStringParam NUTS_GRAALVM_DIR = NParams.ofString("NUTS_GRAALVM_DIR", "/home/vpc/Programs/Dev/graalvm-jdk-22+36.1/");
    NamedStringParam NUTS_INSTALLER_TARGET = NParams.ofString("NUTS_INSTALLER_TARGET", "linux-x64");

    NamedStringParam INSTALLER_JRE8_LINUX64 = NParams.ofString("INSTALLER_JRE8_LINUX64", "/home/vpc/Programs/Dev/JRE/openlogic-openjdk-jre-8u402-b06-linux-x64.tar.gz");
    NamedStringParam INSTALLER_JRE8_LINUX32 = NParams.ofString("INSTALLER_JRE8_LINUX32", "/home/vpc/Programs/Dev/JRE/openlogic-openjdk-jre-8u402-b06-linux-x32.tar.gz");
    NamedStringParam INSTALLER_JRE8_WINDOWS64 = NParams.ofString("INSTALLER_JRE8_WINDOWS64", "/home/vpc/Programs/Dev/JRE/openlogic-openjdk-jre-8u402-b06-windows-x64.zip");
    NamedStringParam INSTALLER_JRE8_WINDOWS32 = NParams.ofString("INSTALLER_JRE8_WINDOWS32", "/home/vpc/Programs/Dev/JRE/openlogic-openjdk-jre-8u402-b06-windows-x32.zip");
    NamedStringParam INSTALLER_JRE8_MAC64 = NParams.ofString("INSTALLER_JRE8_MAC64", "/home/vpc/Programs/Dev/JRE/openlogic-openjdk-jre-8u402-b06-mac-x64.zip");
    boolean NUTS_FLAG_NATIVE = true;
    boolean NUTS_FLAG_REPO_STATS = true;
    boolean NUTS_FLAG_IMPLICIT_ALL = true;
    String NUTS_DEBUG_ARG = null;
    boolean NUTS_FLAG_SITE = false;

    String JAVA_CMD() {
        return NUTS_JAVA_HOME.getValue() + "/bin/java";
    }

    @Override
    public void buildConfiguration() {
        context().NUTS_WEBSITE_BASE = context().NUTS_ROOT_BASE.resolve("documentation/website");
        NUTS_JAVA_HOME.update(context()).ensureDirectory(session);
        NUTS_INSTALLER_BUILD_JAVA_HOME.update(context()).ensureDirectory(session);
        NUTS_GRAALVM_DIR.update(context()).ensureDirectory(session);
        NUTS_INSTALLER_TARGET.update(context()).ensureNonBlank(session);
        INSTALLER_JRE8_LINUX64.update(context()).ensureRegularFile(session);
        INSTALLER_JRE8_LINUX32.update(context()).ensureRegularFile(session);
        INSTALLER_JRE8_WINDOWS64.update(context()).ensureRegularFile(session);
        INSTALLER_JRE8_WINDOWS32.update(context()).ensureRegularFile(session);
        INSTALLER_JRE8_MAC64.update(context()).ensureRegularFile(session);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--debug": {
                cmdLine.withNextFlag((v, a, s)
                        -> NUTS_DEBUG_ARG = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
                );
                return true;
            }
            case "--trace": {
                cmdLine.withNextFlag((v, a, s)
                        -> context().options.add("--trace=" + v)
                );
                return true;
            }
            case "--verbose": {
                cmdLine.withNextFlag((v, a, s)
                        -> context().options.add("--verbose")
                );
                return true;
            }
            case "--all": {
                cmdLine.withNextFlag((v, a, s)
                        -> {
                    NUTS_FLAG_IMPLICIT_ALL = false;
                    NUTS_FLAG_NATIVE = true;
                    NUTS_FLAG_REPO_STATS = true;
                    NUTS_FLAG_SITE = true;
                }
                );
                return true;
            }
            case "--native": {
                cmdLine.withNextFlag((v, a, s)
                        -> {
                    NUTS_FLAG_IMPLICIT_ALL = false;
                    NUTS_FLAG_NATIVE = v;
                    NUTS_FLAG_REPO_STATS = false;
                    NUTS_FLAG_SITE = false;
                }
                );
                return true;
            }
            case "--repo-stats": {
                cmdLine.withNextFlag((v, a, s)
                        -> {
                    NUTS_FLAG_IMPLICIT_ALL = false;
                    NUTS_FLAG_NATIVE = false;
                    NUTS_FLAG_REPO_STATS = v;
                    NUTS_FLAG_SITE = false;
                }
                );
                return true;
            }
            case "--site": {
                cmdLine.withNextFlag((v, a, s)
                        -> {
                    NUTS_FLAG_IMPLICIT_ALL = false;
                    NUTS_FLAG_NATIVE = false;
                    NUTS_FLAG_REPO_STATS = false;
                    NUTS_FLAG_SITE = v;
                }
                );
                return true;
            }
        }
        return false;
    }

    public NutsExecRunner(NSession session) {
        super(session);
    }

    @Override
    public void configureDefaults() {
        super.configureDefaults();
    }

    @Override
    public void run() {
        if (NUTS_FLAG_IMPLICIT_ALL) {
            NUTS_FLAG_IMPLICIT_ALL = false;
            NUTS_FLAG_NATIVE = true;
            NUTS_FLAG_REPO_STATS = true;
            NUTS_FLAG_SITE = true;
        }
        if (NUTS_FLAG_SITE) {
            runSite();
        }
        if (NUTS_FLAG_REPO_STATS) {
            runRepoStats();
        }
        if (NUTS_FLAG_NATIVE) {
            runNativeGraalVM();
            runNativeJPackage();
        }
    }

    private NPath evalInstallerRoot() {
        return context().NUTS_ROOT_BASE.resolve("installers/nuts-installer");
    }

    private NPath evalInstallerSrcDist() {
        NPath installerRoot = context().NUTS_ROOT_BASE.resolve("installers/nuts-installer");
        return installerRoot.resolve("src/dist");
    }

    private NPath evalInstallerJar() {
        String installerJarName = evalInstallerName(null, null, ".jar");
        NPath installerSrcDist = evalInstallerSrcDist();
        return installerSrcDist.resolve(installerJarName);
    }

    private String evalInstallerName(BinPlatform platform, String discriminator, String suffix) {
        String n = "nuts-installer";
        if (platform != null) {
            n += ("-" + platform.id());
        }
        if (!NBlankable.isBlank(discriminator)) {
            n += ("-" + suffix);
        }
        n += ("-" + context().NUTS_INSTALLER_VERSION);
        if (!NBlankable.isBlank(suffix)) {
            n += ("-" + suffix);
        }
        return n;
    }

    private void runNativeGraalVM() {
        NPath installerRoot = evalInstallerRoot();
        NPath installerSrcDist = evalInstallerSrcDist();
        NPath srcDistMetaInfNativeImage = installerSrcDist.resolve("META-INF/native-image");
        srcDistMetaInfNativeImage.mkdirs();
        installerRoot.resolve("dist/portable-jar").mkdirs();
        NPath NUTS_INSTALLER_JAR = evalInstallerJar();
        String installerJarName = NUTS_INSTALLER_JAR.getName();
        NPath installerJarPath = context().NUTS_ROOT_BASE.resolve("installers/nuts-installer/target").resolve(installerJarName);
        installerJarPath.copyTo(installerRoot.resolve("dist/portable-jar").resolve(installerJarName));
        installerJarPath.copyTo(NUTS_INSTALLER_JAR);

//  mkdir -p $NUTS_ROOT_BASE/installers/nuts-installer/dist/linux64-bin
//  cd $NUTS_ROOT_BASE/installers/nuts-installer/src/dist
        NExecCmd.of(session).system()
                .setDirectory(installerSrcDist)
                .addCommand(NUTS_GRAALVM_DIR.getValue() + "/bin/java")
                .addCommand("-agentlib:native-image-agent=config-output-dir=" + srcDistMetaInfNativeImage)
                .addCommand("-jar")
                .addCommand(NUTS_INSTALLER_JAR)
                .failFast()
                .run();

        NPath installerRootDistLinux64Bin = installerRoot.resolve("dist/linux64-bin");
        installerRootDistLinux64Bin.mkdirs();
        NExecCmd.of(session).system()
                .setDirectory(installerSrcDist)
                .addCommand(NUTS_GRAALVM_DIR.getValue() + "/bin/native-image")
                .addCommand("--enable-http")
                .addCommand("--enable-https")
                .addCommand("--enable-https")
                .addCommand("--no-fallback")
                .addCommand("-H:ConfigurationFileDirectories=" + srcDistMetaInfNativeImage)
                .addCommand("-Djava.awt.headless=false")
                .addCommand("-jar")
                .addCommand(NUTS_INSTALLER_JAR)
                .addCommand(installerRootDistLinux64Bin.resolve("nuts-installer-" + NUTS_INSTALLER_TARGET + "-" + context().NUTS_INSTALLER_VERSION))
                .failFast()
                .run();
    }

    private BinPlatform evalCurrentBinPlatform() {
        NEnvs z = NEnvs.of(session);
        switch (z.getOsFamily()) {
            case LINUX: {
                switch (z.getArchFamily()) {
                    case X86_32:
                        return BinPlatform.LINUX32;
                    case X86_64:
                        return BinPlatform.LINUX64;
                }
                break;
            }
            case WINDOWS: {
                switch (z.getArchFamily()) {
                    case X86_32:
                        return BinPlatform.WINDOWS32;
                    case X86_64:
                        return BinPlatform.WINDOWS64;
                }
                break;
            }
            case MACOS: {
                switch (z.getArchFamily()) {
                    case X86_64:
                        return BinPlatform.MAC64;
                }
                break;
            }
        }

        throw new AssertionError("Not supported " + z.getOsFamily() + " " + z.getArchFamily());
    }

    private void runNativeJPackage() {
        runNativeJPackageRPM();
        runNativeJPackageWithJava(BinPlatform.LINUX64);
        runNativeJPackageWithJava(BinPlatform.WINDOWS32);
        runNativeJPackageWithJava(BinPlatform.WINDOWS64);
        runNativeJPackageWithJava(BinPlatform.MAC64);
        runNativeJAR2APP(BinPlatform.MAC64);
    }

    private void runNativeJPackageRPM() {
        NPath installerRoot = evalInstallerRoot();
        NExecCmd.of(session).system()
                .addCommand(NUTS_INSTALLER_BUILD_JAVA_HOME.getValue() + "/bin/jpackage")
                .addCommand("--name")
                .addCommand("nuts-installer")
                .addCommand("--description")
                .addCommand("Nuts Installer")
                .addCommand("--vendor")
                .addCommand("thevpc")
                .addCommand("--app-version")
                .addCommand(context().NUTS_INSTALLER_VERSION)
                .addCommand("--input")
                .addCommand(evalInstallerSrcDist())
                .addCommand("--main")
                .addCommand("-jar")
                .addCommand(evalInstallerName(evalCurrentBinPlatform(), null, ".jar"))
                .addCommand("--dest")
                .addCommand(installerRoot.resolve("dist/" + evalCurrentBinPlatform().id() + "-rpm"))
                .failFast()
                .run();
    }

    private void runNativeJAR2APP(BinPlatform splatform) {
        NPath target = context().NUTS_ROOT_BASE.resolve("installers/nuts-installer/dist/" + splatform.id() + "-bin");
        target.deleteTree();
        NExecCmd.of(session).system()
                .addCommand("jar2app")
                .addCommand("--copyright=(c) 2024 thevpc")
                .addCommand("--short-version=" + Nuts.getVersion())
                .addCommand("--version=" + context().NUTS_INSTALLER_VERSION)
                .addCommand("--icon=" + context().NUTS_ROOT_BASE.resolve("documentation/media/nuts-icon.icns"))
                .addCommand("--bundle-identifier=net.thevpc.nut.nuts-installer")
                .addCommand("--display-name=Nuts Installer")
                .addCommand("--name=nuts-installer-$NUTS_INSTALLER_TARGET-$NUTS_INSTALLER_VERSION" + this.evalInstallerJar())
                .addCommand(evalInstallerJar())
                .addCommand(target)
                .failFast()
                .run();
    }

    private void runNativeJPackageWithJava(BinPlatform splatform) {
        NPath f = context().NUTS_ROOT_BASE.resolve("installers/nuts-installer/dist/" + splatform.id() + "-with-java");
        f.deleteTree();
        NPath jre = null;
        switch (splatform) {
            case LINUX64: {
                jre = NPath.of(INSTALLER_JRE8_LINUX64.getValue(), session);
                break;
            }
            case WINDOWS64: {
                jre = NPath.of(INSTALLER_JRE8_WINDOWS64.getValue(), session);
                break;
            }
            case WINDOWS32: {
                jre = NPath.of(INSTALLER_JRE8_WINDOWS32.getValue(), session);
                break;
            }
            case MAC64: {
                jre = NPath.of(INSTALLER_JRE8_MAC64.getValue(), session);
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported " + splatform);
            }
        }
        NPath packrbin = NPath.ofUserHome(session).resolve("packr-all-4.0.0.jar");
        if (!packrbin.exists()) {
            NPath.of("https://github.com/libgdx/packr/releases/download/4.0.0/packr-all-4.0.0.jar", session)
                    .copyTo(packrbin);
        }
        NExecCmd.of(session).system()
                .addCommand("java")
                .addCommand("-jar")
                .addCommand(packrbin)
                .addCommand("--platform")
                .addCommand(splatform.id())
                .addCommand("--jdk")
                .addCommand(jre)
                .addCommand("--useZgcIfSupportedOs")
                .addCommand("--executable")
                .addCommand("nuts-installer-" + NUTS_INSTALLER_TARGET.getValue() + "-" + context().NUTS_INSTALLER_VERSION)
                .addCommand("--classpath")
                .addCommand(evalInstallerJar())
                .addCommand("--mainclass")
                .addCommand("net.thevpc.nuts.installer.NutsInstaller")
                .addCommand("--vmargs")
                .addCommand("Xmx1G")
                .addCommand("--output")
                .addCommand(f)
                .failFast()
                .run();
    }

    private void runRepoStats() {
        NExecCmd.of(session)
                .addCommand("settings",
                        "update",
                        "stats"
                )
                .addCommand(context().NUTS_ROOT_BASE.resolve("../nuts-preview"))
                .failFast()
                .run();
        NExecCmd.of(session)
                .addCommand("settings",
                        "update",
                        "stats"
                ).addCommand(context().NUTS_ROOT_BASE.resolve("../nuts-public"))
                .failFast()
                .run();
    }

    private void runSite() {
        NInstallCmd.of(session).addIds("ntemplate", "ndocusaurus");
        NExecCmd.of(session).embedded()
                .addCommand(
                        "ntemplate",
                        "-p",
                        context().NUTS_ROOT_BASE.resolve(".dir-template").toString(),
                        "-t",
                        context().NUTS_ROOT_BASE.toString()
                ).failFast()
                .run();

        NPath.of(Mvn.localMaven() + "/" + Mvn.file(Nuts.getApiId(), MvnArtifactType.JAR), session)
                .copyTo(context().NUTS_WEBSITE_BASE.resolve("static/nuts-preview.jar")
                );

        NExecCmd.of(session).embedded()
                .addCommand(
                        "ndocusaurus",
                        "-d",
                        context().NUTS_WEBSITE_BASE.toString(),
                        "pdf",
                        "build"
                ).failFast()
                .run();
    }
}
