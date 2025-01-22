package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.*;

import net.thevpc.nuts.build.util.Mvn;

public class JarsRunner extends AbstractRunner {

    public boolean buildJars = false;
    public JarsRunner() {
        super();
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {
    }

    @Override
    public void configureAfterOptions() {
        context().setRemoteTheVpcSshConnexion(
                NStringUtils.firstNonBlank(NMsg.ofV(
                        NStringUtils.trim(context().vars.get("PROD_SSH_CONNEXION"))
                        , context().varMapper()).toString(),context().getRemoteTheVpcSshUser() + "@thevpc.net")
        );
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--update-version": {
                cmdLine.withNextFlag((v, a) -> context().updateVersion = v);
                return true;
            }
            case "--keep-stamp": {
                cmdLine.withNextFlag((v, a) -> context().keepStamp = v);
                return true;
            }
            case "--production-mode": {
                cmdLine.withNextFlag((v, a) -> context().productionMode = v);
                return true;
            }

            case "--stable-api-version": {
                cmdLine.withNextEntry((v, a) -> context().nutsStableApiVersion = v);
                return true;
            }
            case "--stable-app-version": {
                cmdLine.withNextEntry((v, a) -> context().nutsStableAppVersion = v);
                return true;
            }
            case "--stable-runtime-version": {
                cmdLine.withNextEntry((v, a) -> context().nutsStableRuntimeVersion = v);
                return true;
            }

            case "--remote-ssh-user": {
                cmdLine.withNextEntry((v, a) -> context().remoteTheVpcSshUser = v);
                return true;
            }
            case "--remote-ssh-host": {
                cmdLine.withNextEntry((v, a) -> context().remoteTheVpcSshUser = v);
                return true;
            }
//            case "build-jars": {
//                cmdLine.withNextFlag((v, a, s) -> buildJars = v);
//                return true;
//            }
        }
        return false;
    }

    @Override
    public void run() {
//        if (buildJars) {
            if (context().publish) {
                runNutsPublishMaven();
                runNutsPublishPreview();
                runNutsPublishStable();
            }
//        }
    }

    private void runNutsPublishMaven() {
        echoV("**** publish $nuts maven...", NMaps.of("nuts", NMsg.ofStyledKeyword("nuts")));
        String nutsFolder = Mvn.folder(NId.get("net.thevpc:nuts").get());
        upload(localMvn().resolve(nutsFolder), removeMvn().resolve(nutsFolder));
        remoteCopyFolder(removeMvn().resolve(nutsFolder), remoteThevpcMavenPath().resolve(nutsFolder));
    }

    private void runNutsPublishPreview() {
        echoV("**** publish $nuts preview...", NMaps.of("nuts", NMsg.ofStyledKeyword("nuts")));
        NPath latestJarPath = localMvn().resolve(Mvn.jar(NWorkspace.of().getAppId()));
        latestJarPath.copyTo(context().websiteProjectFolder.resolve("src/resources/download").resolve(latestJarPath.getName()));
        latestJarPath.copyTo(context().websiteProjectFolder.resolve("src/resources/download").resolve("nuts-preview.jar"));
        remoteMkdirs(remoteTheVpcNutsPath().toString());
        remoteCopyFile(latestJarPath, remoteTheVpcNutsPath().resolve("nuts-preview.jar"));
    }


    private void runNutsPublishStable() {
        echoV("**** publish $nuts stable...", NMaps.of("nuts", NMsg.ofStyledKeyword("nuts")));
        NAssert.requireNonBlank(context().nutsStableAppVersion,"nutsAppStableVersion");
        String jarName = NWorkspace.of().getAppId().getArtifactId() + "-"+context().nutsStableAppVersion + ".jar";
//        NPath.of("https://repo1.maven.org/maven2/" + Mvn.jar(NWorkspace.of().getAppId().builder().setVersion(context().nutsStableVersion).build()))
//                        .copyTo(context().nutsRootFolder.resolve("installers/nuts-release-tool/dist").resolve(jarName));

        NPath localJarStable = context().websiteProjectFolder.resolve("src/resources/download").resolve(jarName);
        if(!localJarStable.isRegularFile()){
            throw new NIllegalArgumentException(NMsg.ofC("unable to find nuts stable jar at : %s", localJarStable));
        }
//        NPath.of("https://thevpc.net/maven/" + Mvn.jar(NWorkspace.of().getAppId().builder().setVersion(context().nutsStableAppVersion).build()))
//                        .copyTo(localJarStable);

        localJarStable.copyTo(context().websiteProjectFolder.resolve("src/resources/download").resolve("nuts-stable.jar"));

        remoteMkdirs(remoteTheVpcNutsPath().toString());
        upload(
                localJarStable
                , remoteTheVpcNutsPath().resolve("nuts-stable.jar")
        );
    }



}
