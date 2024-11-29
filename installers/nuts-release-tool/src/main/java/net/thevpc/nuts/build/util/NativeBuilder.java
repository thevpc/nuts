package net.thevpc.nuts.build.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.env.NEnvs;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;

public class NativeBuilder {
    private String jpackageHome;
    private String graalvmHome;

    private String jre8Linux64;
    private String jre8Linux32;
    private String jre8Windows64;
    private String jre8Windows32;
    private String jre8Mac64;
    private NPath dist;
    private NPath projectFolder;
    private NId projectId;
    private NPath jarPath;
    private List<NPath> icons;
    private List<NPath> generatedFiles;
    private String appName;
    private String displayName;
    private String vendor;
    private String appId;
    private NVersion version;
    private String mainClass;
    private String copyright;
    private String[] profilingArgs;
    private Set<PackageType> supported = new LinkedHashSet<>();

    public Set<PackageType> getSupported() {
        return supported;
    }

    public NativeBuilder setSupported(PackageType... supported) {
        if (supported == null) {
            this.supported = new LinkedHashSet<>();
        }
        return setSupported(new HashSet<>(Arrays.asList(supported)));
    }

    public NativeBuilder addSupported(PackageType... supported) {
        if (this.supported == null) {
            this.supported = new LinkedHashSet<>();
        }
        if (supported != null) {
            for (PackageType p : supported) {
                if (p != null) {
                    this.supported.add(p);
                }
            }
        }
        return this;
    }

    public NativeBuilder setSupported(Set<PackageType> supported) {
        this.supported = supported == null ? null : supported.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        return this;
    }

    public String[] getProfilingArgs() {
        return profilingArgs;
    }

    public NativeBuilder setProfilingArgs(String[] profilingArgs) {
        this.profilingArgs = profilingArgs;
        return this;
    }

    public NativeBuilder() {
    }


    private NPath evalSrcDist() {
        return projectFolder.resolve("src/dist");
    }

    private NPath evalSrcDistJar() {
        String jarName = evalName(null, null, ".jar");
        return evalSrcDist().resolve(jarName);
    }

    private String evalNameNoVersion(BinPlatform platform, String discriminator, String suffix) {
        String n = appName;
        if (platform != null) {
            n += ("-" + platform.id());
        }
        if (!NBlankable.isBlank(discriminator)) {
            n += ("-" + discriminator);
        }
        //n += ("-" + appVersion);
        if (!NBlankable.isBlank(suffix)) {
            n += suffix;
        }
        return n;
    }

    public String evalName(BinPlatform platform, String discriminator, String suffix) {
        String n = appName;
        if (platform != null) {
            n += ("-" + platform.id());
        }
        if (!NBlankable.isBlank(discriminator)) {
            n += ("-" + discriminator);
        }
        n += ("-" + version);
        if (!NBlankable.isBlank(suffix)) {
            n += suffix;
        }
        return n;
    }

    public void echo(String message, Map<String, ?> vars) {
        NSession.get().out().println(NMsg.ofV(message, vars));
    }


    private List<NPath> createDistPortableJar() {
        echo("**** [$id] create $v...", NMaps.of("id",appId,"v", NMsg.ofStyled("jar", NTextStyle.keyword())));
        NPath targetFolder = dist.resolve(evalNameNoVersion(null, "jar", null));
        if (targetFolder.isDirectory()) {
            targetFolder.deleteTree();
        }
        targetFolder.mkdirs();
        NPath distJar = evalSrcDistJar();
        NPath installerJarPath = projectFolder.resolve("target").resolve(projectId.getArtifactId()+"-"+projectId.getVersion()+".jar");
        NPath f = targetFolder.resolve(distJar.getName());
        installerJarPath.copyTo(f);
        installerJarPath.copyTo(distJar);
        return Arrays.asList(f);
    }

    private List<NPath> createDistNativeGraalVMBin() {
        echo("**** [$id] create $v (GraalVM)...", NMaps.of("id",appId,"v", NMsg.ofStyled("native-image", NTextStyle.keyword())));
        List<NPath> ret = new ArrayList<>();

        BinPlatform platform = currentPlatform();
        NPath rootDistLinux64Bin = dist.resolve(evalNameNoVersion(platform, "bin", null));
        if (rootDistLinux64Bin.isDirectory()) {
            rootDistLinux64Bin.deleteTree();
        }
        rootDistLinux64Bin.mkdirs();


        NPath srcDistMetaInfNativeImage = evalSrcDist().resolve("META-INF/native-image");
        srcDistMetaInfNativeImage.mkdirs();
        NPath newJarPath = evalSrcDistJar();
        jarPath.copyTo(newJarPath);

//  mkdir -p $NUTS_ROOT_BASE/installers/nuts-installer/dist/linux64-bin
//  cd $NUTS_ROOT_BASE/installers/nuts-installer/src/dist
        ensureRegularFile(graalvmHome + "/bin/java", "graalvmHome");
        ensureRegularFile(graalvmHome + "/bin/native-image", "graalvmHome");
        NExecCmd.of().system()
                .setEnv("JAVA_HOME",graalvmHome)
                .setDirectory(evalSrcDist())
                .addCommand(graalvmHome + "/bin/java")
                .addCommand("-agentlib:native-image-agent=config-output-dir=" + srcDistMetaInfNativeImage)
                .addCommand("-DEnableGraalVM=true")
                .addCommand("-jar")
                .addCommand(newJarPath)
                .addCommand(profilingArgs)
                .failFast()
                .run();

        NPath f = rootDistLinux64Bin.resolve(evalName(platform, null, null));
        NExecCmd.of().system()
                .setEnv("JAVA_HOME",graalvmHome)
                .setDirectory(evalSrcDist())
                .addCommand(graalvmHome + "/bin/native-image")
                .addCommand("--enable-http")
                .addCommand("--enable-https")
                .addCommand("--enable-https")
                .addCommand("--no-fallback")
                .addCommand("-H:+UnlockExperimentalVMOptions")
                .addCommand("-H:ConfigurationFileDirectories=" + srcDistMetaInfNativeImage)
                .addCommand(
                        (srcDistMetaInfNativeImage.resolve("my-reflect-config.json")).isRegularFile()?
                        "-H:ReflectionConfigurationFiles=" + srcDistMetaInfNativeImage.resolve("my-reflect-config.json")
                                :null
                )
                .addCommand("-Djava.awt.headless=false")
                .addCommand("-DEnableGraalVM=true")
                .addCommand("-jar")
                .addCommand(newJarPath)
                .addCommand(f)
                .failFast()
                .run();
        ret.add(zipFolder(rootDistLinux64Bin, platform, "bin"));
        return ret;
    }

    private NPath zipFolder(NPath folder, BinPlatform platform, String discriminator) {
        NPath fzip = folder.resolveSibling(evalName(platform, discriminator, ".zip"));
        NExecCmd.of().system()
                .setDirectory(folder.getParent())
                .addCommand("zip")
                .addCommand("-r")
                .addCommand(fzip)
                .addCommand(folder.getName())
                .failFast()
                .run();
        return fzip;
    }

    private BinPlatform currentPlatform() {
        NEnvs e = NEnvs.of();
        switch (e.getOsFamily()) {
            case UNIX:
            case LINUX: {
                if (e.getArchFamily().name().endsWith("_64")) {
                    return BinPlatform.LINUX64;
                }
                if (e.getArchFamily().name().endsWith("_32")) {
                    return BinPlatform.LINUX32;
                }
                break;
            }
            case MACOS: {
                if (e.getArchFamily().name().endsWith("_64")) {
                    return BinPlatform.MAC64;
                }
                break;
            }

            case WINDOWS: {
                if (e.getArchFamily().name().endsWith("_64")) {
                    return BinPlatform.WINDOWS64;
                }
                if (e.getArchFamily().name().endsWith("_32")) {
                    return BinPlatform.WINDOWS32;
                }
                break;
            }
        }
        throw new IllegalArgumentException("unsupported platform " + e.getOs() + " " + e.getArch());
    }

    private BinPlatform evalCurrentBinPlatform() {
        NEnvs z = NEnvs.of();
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

    private List<NPath> createDistJPackageRPM() {
        echo("**** [$id] create $v (JPackage)...", NMaps.of("id",appId,"v", NMsg.ofStyled("rpm", NTextStyle.keyword())));
        NPath targetFolder = dist.resolve(evalNameNoVersion(evalCurrentBinPlatform(), "rpm", null)
        );
        BinPlatform platform = currentPlatform();
        if (targetFolder.isDirectory()) {
            targetFolder.deleteTree();
        }
        NExecCmd.of().system()
                .addCommand(jpackageHome + "/bin/jpackage")
                .addCommand("--name")
                .addCommand(appName)
                .addCommand("--description")
                .addCommand(displayName)
                .addCommand("--vendor")
                .addCommand(vendor)
                .addCommand("--app-version")
                .addCommand(version.toString())
                .addCommand("--input")
                .addCommand(evalSrcDist())
                .addCommand("--main-jar")
                .addCommand(evalName(null, null, ".jar"))
                .addCommand("--dest")
                .addCommand(targetFolder)
                .failFast()
                .run();
        NPath rpmFile = targetFolder.list().stream().filter(x -> x.getName().startsWith(evalName(null, null, null)))
                .findFirst().get();
        return Arrays.asList(rpmFile);
    }

    private List<NPath> createDistNativeJar2appAllBin() {
        List<NPath> ret = new ArrayList<>();
        ret.addAll(createDistNativeJar2app(BinPlatform.MAC64));
        return ret;
    }

    private NPath installJar2App() {
        NPath jar2appBase = NPath.ofTempFolder("jar2app");
        NExecCmd.of().system()
                .setDirectory(jar2appBase)
                .addCommand("git", "clone")
                .addCommand("https://github.com/Jorl17/jar2app.git")
                .failFast()
                .run();
        NPath jar2appFolderSrc = jar2appBase.resolve("jar2app");
//        NPath jar2appFolderBin=jar2appBase.resolve("jar2app-bin");
//        jar2appFolderSrc.resolve("jar2app_basefiles").copyTo(
//                jar2appFolderBin.resolve("jar2app_basefiles")
//        );
//        NExecCmd.of(session).system().setDirectory(jar2appFolderSrc).addCommand("chmod","-R", "a+rw",jar2appFolderBin.resolve("jar2app_basefiles").toString()).run();
//        jar2appFolderSrc.resolve("jar2app").copyTo(
//                jar2appFolderBin
//        );
//        jar2appFolderBin=jar2appFolderBin.setUserTemporary(true);
        NExecCmd.of().system().setDirectory(jar2appFolderSrc).failFast().addCommand("chmod", "-R", "a+rw", jar2appFolderSrc.resolve("jar2app_basefiles").toString()).run();
        NExecCmd.of().system().setDirectory(jar2appFolderSrc).failFast().addCommand("chmod", "-R", "a+rw", jar2appFolderSrc.resolve("jar2app").toString()).run();
        return jar2appFolderSrc;
    }

    private List<NPath> createDistNativeJar2app(BinPlatform platform) {
        echo("**** [$id] create $v $p (Jar2App)...",
                NMaps.of("id",appId,
                        "v", NMsg.ofStyled("bin", NTextStyle.keyword())
                        , "p", platform
                )
        );
        NPath target = dist.resolve(evalNameNoVersion(platform, "bin", null));
        if (target.isDirectory()) {
            target.deleteTree();
        }
        NPath jar2AppRoot = installJar2App();
        NExecCmd.of().system()
                .setDirectory(jar2AppRoot)
                .addCommand("python3")
                .addCommand("./jar2app")
                .addCommand("--copyright=" + copyright)
                .addCommand("--short-version=" + version)
                .addCommand("--version=" + version)
                .addCommand(
                        icons == null ? null :
                                icons.stream().filter(x -> x.getName().endsWith(".icns"))
                                        .map(x -> "--icon=" + x.toString())
                                        .findFirst().orElse(null)
                )
                .addCommand("--bundle-identifier=" + appId)
                .addCommand("--display-name=" + displayName)
                .addCommand("--name=" + this.evalName(platform, null, null))
                .addCommand(evalSrcDistJar())
                .addCommand(target)
                .failFast()
                .run();
        return Arrays.asList(
                zipFolder(target.resolveSibling(target.getName() + ".app"), platform, null)
        );
    }

    private List<NPath> createDistNativePackrAllWithJava() {
        List<NPath> ret = new ArrayList<>();
        ret.addAll(createDistNativePackrWithJava(BinPlatform.LINUX64));
        ret.addAll(createDistNativePackrWithJava(BinPlatform.WINDOWS64));
//        ret.addAll(createDistNativePackrWithJava(BinPlatform.MAC64));

//        ret.addAll(createDistNativePackrWithJava(BinPlatform.LINUX32));
//        ret.addAll(createDistNativePackrWithJava(BinPlatform.WINDOWS32));
        return ret;
    }

    private NPath ensureRegularFile(String value, String name) {
        if (NBlankable.isBlank(value)) {
            throw new IllegalArgumentException("expected file " + name);
        }
        NPath p = NPath.of(value);
        if (!p.exists()) {
            throw new IllegalArgumentException("file " + name + " does not exists");
        }
        if (!p.isRegularFile()) {
            throw new IllegalArgumentException("file " + name + " is not a regular file");
        }
        return p;
    }

    private List<NPath> createDistNativePackrWithJava(BinPlatform platform) {
        echo("**** [$id] create $v $p (Packr)...",
                NMaps.of("id",appId,
                        "v", NMsg.ofStyled("bin-with-java", NTextStyle.keyword())
                        , "p", platform
                )
        );
        NPath f = dist.resolve(evalNameNoVersion(platform, "with-java", null));
        if (f.isDirectory()) {
            f.deleteTree();
        }
        NPath jre = null;
        switch (platform) {
            case LINUX64: {
                jre = ensureRegularFile(jre8Linux64, "jre8Linux64");
                break;
            }
            case LINUX32: {
                jre = ensureRegularFile(jre8Linux32, "jre8Linux32");
                break;
            }
            case WINDOWS64: {
                jre = ensureRegularFile(jre8Windows64, "jre8Windows64");
                break;
            }
            case WINDOWS32: {
                jre = ensureRegularFile(jre8Windows32, "jre8Windows32");
                break;
            }
            case MAC64: {
                jre = ensureRegularFile(jre8Mac64, "jre8Mac64");
                break;
            }
            default: {
                throw new IllegalArgumentException("unsupported " + platform);
            }
        }
        NPath packrbin = NPath.ofUserHome().resolve("packr-all-4.0.0.jar");
        if (!packrbin.exists()) {
            NPath.of("https://github.com/libgdx/packr/releases/download/4.0.0/packr-all-4.0.0.jar")
                    .copyTo(packrbin);
        }
        NExecCmd.of().system()
                .addCommand("java")
                .addCommand("-jar")
                .addCommand(packrbin)
                .addCommand("--platform")
                .addCommand(platform.id())
                .addCommand("--jdk")
                .addCommand(jre)
                .addCommand("--useZgcIfSupportedOs")
                .addCommand("--executable")
                .addCommand(evalName(platform, null, null))
                .addCommand("--classpath")
                .addCommand(evalSrcDistJar())
                .addCommand("--mainclass")
                .addCommand(NAssert.requireNonBlank(mainClass, "mainClass"))
                .addCommand("--vmargs")
                .addCommand("Xmx1G")
                .addCommand("--output")
                .addCommand(f)
                .failFast()
                .run();
        return Arrays.asList(zipFolder(f, platform, "with-java"));
    }


    public void build() {
        generatedFiles = new ArrayList<>();
        if (isSupported(PackageType.PORTABLE)) {
            generatedFiles.addAll(createDistPortableJar());
        }
        if (isSupported(PackageType.BIN)) {
            generatedFiles.addAll(createDistNativeGraalVMBin());
        }
        if (isSupported(PackageType.NATIVE)) {
            generatedFiles.addAll(createDistJPackageRPM());
        }
        if (isSupported(PackageType.JRE_BUNDLE)) {
            generatedFiles.addAll(createDistNativePackrAllWithJava());
        }
        if (isSupported(PackageType.NATIVE)) {
            generatedFiles.addAll(createDistNativeJar2appAllBin());
        }
    }

    private boolean isSupported(PackageType packageType) {
        return packageType != null && this.supported == null || this.supported.contains(packageType);
    }

    public String getJpackageHome() {
        return jpackageHome;
    }

    public NativeBuilder setJpackageHome(String jpackageHome) {
        this.jpackageHome = jpackageHome;
        return this;
    }

    public String getGraalvmHome() {
        return graalvmHome;
    }

    public NativeBuilder setGraalvmHome(String graalvmHome) {
        this.graalvmHome = graalvmHome;
        return this;
    }

    public String getJre8Linux64() {
        return jre8Linux64;
    }

    public NativeBuilder setJre8Linux64(String jre8Linux64) {
        this.jre8Linux64 = jre8Linux64;
        return this;
    }

    public String getJre8Linux32() {
        return jre8Linux32;
    }

    public NativeBuilder setJre8Linux32(String jre8Linux32) {
        this.jre8Linux32 = jre8Linux32;
        return this;
    }

    public String getJre8Windows64() {
        return jre8Windows64;
    }

    public NativeBuilder setJre8Windows64(String jre8Windows64) {
        this.jre8Windows64 = jre8Windows64;
        return this;
    }

    public String getJre8Windows32() {
        return jre8Windows32;
    }

    public NativeBuilder setJre8Windows32(String jre8Windows32) {
        this.jre8Windows32 = jre8Windows32;
        return this;
    }

    public String getJre8Mac64() {
        return jre8Mac64;
    }

    public NativeBuilder setJre8Mac64(String jre8Mac64) {
        this.jre8Mac64 = jre8Mac64;
        return this;
    }

    public NPath getDist() {
        return dist;
    }

    public NativeBuilder setDist(NPath dist) {
        this.dist = dist;
        return this;
    }

    public NPath getProjectFolder() {
        return projectFolder;
    }

    //    public NativeBuilder setProjectFolder(NPath projectFolder) {
//        this.projectFolder = projectFolder;
//        return this;
//    }
    public NativeBuilder setProjectFolder(NPath projectFolder) {
        this.projectFolder = projectFolder;
        return this;
    }

    public NativeBuilder setProjectFolder(NPath projectFolder, NId preferredId, String jarName) {
        this.projectFolder = projectFolder;
        //setProjectFolder(projectFolder);
        NDescriptor nDescriptor = NDescriptorParser.of().setDescriptorStyle(NDescriptorStyle.MAVEN).parse(getProjectFolder().resolve("pom.xml")).get();
        this.projectId = nDescriptor.getId();
        if (preferredId != null && preferredId.getVersion().isBlank()) {
            preferredId=preferredId.builder().setVersion(projectId.getVersion()).builder();
        }
        if (preferredId != null && NBlankable.isBlank(preferredId.getGroupId())) {
            preferredId=preferredId.builder().setGroupId(projectId.getGroupId()).builder();
        }
        if (preferredId != null && !NBlankable.isBlank(preferredId.getArtifactId())) {
            setAppName(preferredId.getArtifactId());
        }else{
            setAppName(projectId.getArtifactId());
        }
        setDisplayName(nDescriptor.getName());
        setAppId(projectId.getGroupId() + "." + projectId.getArtifactId());
        setVersion(projectId.getVersion());
        setJarPath(getProjectFolder().resolve("target").resolve(
                NBlankable.isBlank(jarName) ?
                        evalName(null, null, ".jar")
                        : NMsg.ofV(jarName, NMsgParam.of("version", () -> version.toString())).toString()
        ));
        setDist(getProjectFolder().resolve("dist"));
        return this;
    }

    public NPath getJarPath() {
        return jarPath;
    }

    public NativeBuilder setJarPath(NPath jarPath) {
        this.jarPath = jarPath;
        return this;
    }

    public List<NPath> getIcons() {
        return icons;
    }

    public NativeBuilder setIcons(List<NPath> icons) {
        this.icons = icons;
        return this;
    }

    public List<NPath> getGeneratedFiles() {
        return generatedFiles;
    }

    public NativeBuilder setGeneratedFiles(List<NPath> generatedFiles) {
        this.generatedFiles = generatedFiles;
        return this;
    }

    public String getAppName() {
        return appName;
    }

    public NativeBuilder setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public NativeBuilder setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public String getVendor() {
        return vendor;
    }

    public NativeBuilder setVendor(String vendor) {
        this.vendor = vendor;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public NativeBuilder setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public NVersion getVersion() {
        return version;
    }

    public NativeBuilder setVersion(NVersion version) {
        this.version = version;
        return this;
    }

    public String getMainClass() {
        return mainClass;
    }

    public NativeBuilder setMainClass(String mainClass) {
        this.mainClass = mainClass;
        return this;
    }

    public String getCopyright() {
        return copyright;
    }

    public NativeBuilder setCopyright(String copyright) {
        this.copyright = copyright;
        return this;
    }

    public enum PackageType {
        PORTABLE,
        BIN,
        NATIVE,
        JRE_BUNDLE,
    }
}
