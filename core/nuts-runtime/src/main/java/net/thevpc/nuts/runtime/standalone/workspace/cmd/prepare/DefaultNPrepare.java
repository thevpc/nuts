package net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NPrepare;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NRuntimeDistribution;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaProvider;
import net.thevpc.nuts.runtime.standalone.util.jclass.NJavaSdkUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.NRuntimeDistributionImpl;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;

import java.util.*;
import java.util.stream.Collectors;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNPrepare extends AbstractNPrepare {
    private final NLog LOG = NLog.of(DefaultNPrepare.class);
    private String companionRepository;

    public DefaultNPrepare() {
        super();
    }


    @Override
    public NPrepare run() {
        LOG.info(NMsg.ofC("Starting prepare operation for connection string: %s", connectionString()));
        try (DefaultNPrepareTransaction rc = new DefaultNPrepareTransaction(version(), connectionString())) {
            if (NBlankable.isBlank(rc.apiVersion)) {
                rc.apiVersion = NWorkspace.of().apiVersion();
                rc.rtVersion = NWorkspace.of().runtimeId().version();
                rc.appVersion = NWorkspace.of().appId().version();
            } else {
                rc.rtVersion = NSearch.of(NWorkspace.of().runtimeId().builder().version(NVersion.of(rc.apiVersion + ".0").toAtLeast()).build()).getResultIds().findFirst().map(NId::version).orElse(rc.apiVersion);
                rc.appVersion = NSearch.of(NWorkspace.of().appId().builder().version(rc.apiVersion.toAtLeast()).build()).getResultIds().findFirst().map(NId::version).orElse(rc.apiVersion);
            }
            rc.remoteUserHome = rc.remoteEnv().userHome();
            rc.remoteWorkspace = workspace();
            if (NBlankable.isBlank(rc.remoteWorkspace)) {
                rc.remoteWorkspace = "default-workspace";
            }
            if (NPath.of(rc.remoteWorkspace).isName()) {
                rc.remoteWorkspace = rc.remoteUserHome + "/.config/nuts/ws/" + rc.remoteWorkspace;
            } else {
                rc.remoteWorkspace = NPath.of(rc.remoteWorkspace).toAbsolute(rc.remoteUserHome).toString();
            }
            LOG.debug(NMsg.ofC("Remote user home: %s, workspace: %s", rc.remoteUserHome, rc.remoteWorkspace));
            provisionJava(rc);

            rc.companionRepository = rc.remoteUserHome + "/nuts-companion-respository-" + UUID.randomUUID();
            LOG.debug(NMsg.ofC("Using remote companion repository: %s", rc.companionRepository));

            NWorkspace workspace = NWorkspace.of();

            NId nutsApiId = NWorkspace.of().apiId().builder().version(rc.apiVersion).build();
            NId nutsAppId = NWorkspace.of().appId().builder().version(rc.appVersion).build();
            NId nutsBootId = NId.of("net.thevpc.nuts:nuts-boot#" + rc.apiVersion);
            NId nutsRuntimeId = workspace.runtimeId().builder().version(rc.rtVersion).build();

            Set<NId> deps = new HashSet<>();
            deps.add(nutsApiId);
            deps.add(nutsAppId);
            deps.add(nutsBootId);
            deps.add(nutsRuntimeId);
            deps.addAll(
                    NSearch.of().addId(nutsAppId).latest(true)
                            .targetApiVersion(nutsApiId.version())
                            .dependencyFilter(NDependencyFilter.ofRunnable())
                            .basePackage(true)
                            .inlineDependencies(true)
                            .targetEnv(rc.connectionString)
                            .getResultIds().toList().stream().map(x->x.longId()).collect(Collectors.toList())
            );
            deps.addAll(
                    NSearch.of().addId("net.thevpc.nsh:nsh").latest(true)
                            .targetApiVersion(nutsApiId.version())
                            .dependencyFilter(NDependencyFilter.ofRunnable())
                            .basePackage(true)
                            .inlineDependencies(true)
                            .targetEnv(rc.connectionString)
                            .getResultIds().toList().stream().map(x->x.longId()).collect(Collectors.toList())
            );

            if (ids != null) {
                for (NId id : ids) {
                    List<NId> resolved = NSearch.of().addId(id).latest(true)
                            .targetApiVersion(nutsApiId.version())
                            .dependencyFilter(NDependencyFilter.ofRunnable())
                            .basePackage(true)
                            .inlineDependencies(true)
                            .targetEnv(rc.connectionString)
                            .getResultIds().toList().stream().map(x->x.longId()).collect(Collectors.toList());
                    if (resolved.isEmpty()) {
                        deps.add(id);
                    } else {
                        deps.addAll(resolved);
                    }
                }
            }
            LOG.info(NMsg.ofC("Staging %s dependencies to companion repository...", deps.size()));
            for (NId dep : deps) {
                rc.pushId(dep);
            }
            List<String> args = new ArrayList<>();
            args.add(rc.remoteJava);
            args.add("-jar");
            args.add(rc.companionJar(nutsAppId).toString());
            args.add("--workspace=" + rc.remoteWorkspace);
            args.add("--bot");
            args.add("-y");
            args.add("--offline");
            args.add("--repos=shared=nuts@" + rc.companionRepository);
            args.addAll(extraOptions);
            args.add("install");
            for (NId dep : deps) {
                args.add(dep.toString());
            }
            LOG.info(NMsg.ofC("Executing remote installation command: %s", String.join(" ", args)));
            rc.runRemoteAsString(args.toArray(new String[0]));
            this.companionRepository = rc.companionRepository;
            LOG.info(NMsg.ofC("Prepare completed successfully. Companion repository: %s", this.companionRepository));
        }
        return this;
    }

    public String companionRepository() {
        return companionRepository;
    }

    private void provisionJava(DefaultNPrepareTransaction rc) {
        rc.remotePrivateJdk = rc.remoteWorkspace + "/.jdk";
        LOG.info(NMsg.ofC("Provisioning Java on target host. Private JDK path: %s", rc.remotePrivateJdk));
        if (!NBlankable.isBlank(java())) {
            String specifiedJava = java();
            LOG.debug(NMsg.ofC("Checking specified Java path: %s", specifiedJava));
            if (rc.runRemoteAsStringNoFail(specifiedJava, "-version")) {
                rc.remoteJava = specifiedJava;
                LOG.info(NMsg.ofC("Using specified Java executable: %s", rc.remoteJava));
                return;
            }
            String candidateBinJava = specifiedJava + "/bin/java";
            if (rc.runRemoteAsStringNoFail(candidateBinJava, "-version")) {
                rc.remoteJava = candidateBinJava;
                LOG.info(NMsg.ofC("Using Java executable inside specified home: %s", rc.remoteJava));
                return;
            }
        }
        LOG.debug(NMsg.ofC("Checking system 'java' command..."));
        if (rc.runRemoteAsStringNoFail("java", "-version")) {
            rc.remoteJava = "java";
            LOG.info(NMsg.ofC("Using system 'java' command on remote host"));
            return;
        }

        LOG.debug(NMsg.ofC("Checking remote private JDK at %s/bin/java...", rc.remotePrivateJdk));
        if (rc.runRemoteAsStringNoFail(rc.remotePrivateJdk + "/bin/java", "-version")) {
            rc.remoteJava = rc.remotePrivateJdk + "/bin/java";
            LOG.info(NMsg.ofC("Using existing remote private JDK: %s", rc.remoteJava));
            return;
        }

        LOG.info(NMsg.ofC("No valid Java found on remote host. Downloading and installing private JDK..."));
        NRuntimeDistribution nRuntimeDistribution = resolveAndInstallForRemote(rc,
                NRuntimeDistribution.JAVA_PRODUCT_JDK,
                NVersion.of("25"),
                rc.remoteEnv().osFamily(),
                rc.remoteEnv().archFamily(), null).get();
        rc.remoteJava = NPath.of(nRuntimeDistribution.path()).resolve("bin/java").toString();
        LOG.info(NMsg.ofC("Remote Java provisioned successfully: %s", rc.remoteJava));
    }

    private NOptional<NRuntimeDistribution> resolveAndInstallForRemote(DefaultNPrepareTransaction rc, @NNullable String product, @NNonNull NVersion version, @NNonNull NOsFamily os, @NNonNull NArchFamily arch, @NNullable String vendor) {
        List<JavaProvider> acceptableJavaProviders = new ArrayList<>(NJavaSdkUtils.of().javaProviders());
        NAssert.requireNamedNonBlank(version, "version");
        NAssert.requireNamedNonBlank(os, "os");
        NAssert.requireNamedNonBlank(arch, "arch");
        if (!NBlankable.isBlank(vendor)) {
            acceptableJavaProviders = acceptableJavaProviders.stream().filter(x -> NNameFormat.equalsIgnoreFormat(NStringUtils.strip(vendor), x.getName())).collect(Collectors.toList());
        }
        for (JavaProvider javaProvider : acceptableJavaProviders) {
            String product2 = NJavaSdkUtils.validateJavaProduct(product).orElse(NRuntimeDistribution.JAVA_PRODUCT_JDK);
            int version2 = NJavaSdkUtils.validateJavaMajorVersionOrDefault(version);

            NPath targetBin = NPath.of(NStoreKey.ofBin(NWorkspace.of().apiId()))
                    .resolve("remote-sdk/java/" + javaProvider.getName() + "/")
                    .resolve(javaProvider.getName() + "-" + version + "-" + os.id() + "-" + arch.id());
            LOG.debug(NMsg.ofC("Resolving download path for Java provider %s (version=%s, os=%s, arch=%s)", javaProvider.getName(), version, os, arch));
            NOptional<NPath> z = javaProvider.resolveDownloadPath(product2, version2, os, arch, targetBin);
            if (z.isPresent()) {
                NPath p = NPath.ofTempFile(z.get().name());
                z.get().copyTo(p);
                NPath remoteZip = NPath.of(rc.remoteWorkspace).resolve(p.name());
                NPath remoteZipAbsolute = rc.connectionString != null ? NPath.of(rc.connectionString.withPath(remoteZip.toString())) : NPath.of(remoteZip.toString());
                NPath remoteZipPath = null;
                rc.cd(rc.remoteWorkspace);
                try {
                    boolean transferred = false;
                    try {
                        remoteZipAbsolute.mkParentDirs();
                        p.copyTo(remoteZipAbsolute);
                        remoteZipPath = remoteZipAbsolute;
                        transferred = true;
                        LOG.debug(NMsg.ofC("Transferred JDK package via NPath copyTo: %s", remoteZipAbsolute));
                    } catch (Exception ex) {
                        LOG.debug(NMsg.ofC("copyTo failed for %s, trying scp fallback: %s", remoteZipAbsolute, ex.getMessage()));
                        if (rc.connectionString != null && !rc.localHost) {
                            rc.runRemoteAsString("mkdir", "-p", rc.remoteWorkspace);
                            String userHost = (rc.connectionString.userName() != null ? rc.connectionString.userName() + "@" : "") + rc.connectionString.host();
                            NExec.ofSystem().command("scp", "-o", "StrictHostKeyChecking=no", p.toString(), userHost + ":" + remoteZip)
                                    .failFast(true).grabbedAll();
                            remoteZipPath = remoteZip;
                            transferred = true;
                            LOG.debug(NMsg.ofC("Transferred JDK package via scp fallback to %s:%s", userHost, remoteZip));
                        } else {
                            throw ex;
                        }
                    }

                    if (transferred) {
                        String remoteZipLocation = remoteZip.toString();
                        String remoteJdkTmp = NPath.of(rc.remoteWorkspace).resolve(".jdk-tmp-" + UUID.randomUUID()).toString();
                        LOG.info(NMsg.ofC("Extracting JDK package %s to %s on remote host...", remoteZipLocation, rc.remotePrivateJdk));
                        if (rc.remoteEnv().osFamily() == NOsFamily.WINDOWS && p.name().toLowerCase().endsWith(".zip")) {
                            rc.runRemoteAsString("powershell", "-Command", "if (Test-Path '" + rc.remotePrivateJdk + "') { Remove-Item -Recurse -Force '" + rc.remotePrivateJdk + "' }");
                            rc.runRemoteAsString("powershell", "-Command", "Expand-Archive -Path '" + remoteZipLocation + "' -DestinationPath '" + remoteJdkTmp + "' -Force");
                            rc.runRemoteAsString("powershell", "-Command", "$j = Get-ChildItem -Path '" + remoteJdkTmp + "' -Filter 'java.exe' -Recurse | Select-Object -First 1; if ($j) { $h = $j.Directory.Parent.FullName; Move-Item -Path $h -Destination '" + rc.remotePrivateJdk + "' -Force }; Remove-Item -Recurse -Force '" + remoteJdkTmp + "'");
                            rc.runRemoteAsString("powershell", "-Command", "if (Test-Path '" + remoteZipLocation + "') { Remove-Item -Recurse -Force '" + remoteZipLocation + "' }");
                        } else if (p.name().toLowerCase().endsWith(".tar.gz") || p.name().toLowerCase().endsWith(".tgz")) {
                            rc.runRemoteAsString("rm", "-Rf", rc.remotePrivateJdk, remoteJdkTmp);
                            rc.runRemoteAsString("mkdir", "-p", remoteJdkTmp);
                            rc.runRemoteAsString("tar", "-xf", remoteZipLocation, "-C", remoteJdkTmp);
                            rc.runRemoteAsString("sh", "-c", "J=$(find " + remoteJdkTmp + " -type f -name java 2>/dev/null | head -n 1); if [ -n \"$J\" ]; then H=$(dirname $(dirname \"$J\")); mv \"$H\" " + rc.remotePrivateJdk + "; fi; rm -Rf " + remoteJdkTmp + " " + remoteZipLocation);
                        } else if (p.name().toLowerCase().endsWith(".zip")) {
                            rc.runRemoteAsString("rm", "-Rf", rc.remotePrivateJdk, remoteJdkTmp);
                            rc.runRemoteAsString("mkdir", "-p", remoteJdkTmp);
                            rc.runRemoteAsString("unzip", "-o", remoteZipLocation, "-d", remoteJdkTmp);
                            rc.runRemoteAsString("sh", "-c", "J=$(find " + remoteJdkTmp + " -type f -name java 2>/dev/null | head -n 1); if [ -n \"$J\" ]; then H=$(dirname $(dirname \"$J\")); mv \"$H\" " + rc.remotePrivateJdk + "; fi; rm -Rf " + remoteJdkTmp + " " + remoteZipLocation);
                        } else {
                            throw new NIllegalArgumentException(NMsg.ofC("unsupported file type : %s", p.name()));
                        }
                        LOG.info(NMsg.ofC("JDK extraction complete at %s", rc.remotePrivateJdk));
                        return NOptional.of(new NRuntimeDistributionImpl(
                                null, javaProvider.getName(), product2, null, javaProvider.getName() + "-" + version, rc.remotePrivateJdk, version.toString(), "zip", 0
                        ));
                    }
                } catch (Exception ex) {
                    LOG.error(NMsg.ofC("Failed to provision JDK via %s: %s", javaProvider.getName(), ex));
                } finally {
                    p.delete();
                    if (remoteZipPath != null) {
                        try {
                            remoteZipPath.delete();
                        } catch (Exception ex) {
                            //just ignore
                        }
                    }
                }
            }
        }
        Map<String, Object> env = new LinkedHashMap<>();
        if (!NBlankable.isBlank(product)) {
            env.put("product", product);
        }
        if (!NBlankable.isBlank(vendor)) {
            env.put("vendor", vendor);
        }
        if (!NBlankable.isBlank(version)) {
            env.put("version", version);
        }
        if (!NBlankable.isBlank(os)) {
            env.put("os", os);
        }
        if (!NBlankable.isBlank(arch)) {
            env.put("arch", arch);
        }
        return NOptional.ofEmpty(NMsg.ofC("java not found : %s", env));
    }
}

