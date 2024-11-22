package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

import net.thevpc.nuts.build.util.Mvn;
import net.thevpc.nuts.build.util.MvnArtifactType;

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
        setRemoteSshConnexion(context().user + "@thevpc.net");
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
            case "--stable-version": {
                cmdLine.withNextEntry((v, a) -> context().nutsStableVersion = v);
                return true;
            }
            case "--stable-runtime-version": {
                cmdLine.withNextEntry((v, a) -> context().runtimeStableVersion = v);
                return true;
            }
            case "--remote-user": {
                cmdLine.withNextEntry((v, a) -> context().user = v);
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
        echo("**** publish $nuts maven...", NMaps.of("nuts", NMsg.ofStyled("nuts", NTextStyle.keyword())));
        String nutsFolder = Mvn.folder(NId.of("net.thevpc:nuts").get());
        upload(localMvn().resolve(nutsFolder), removeMvn().resolve(nutsFolder));
        remoteCopyFolder(removeMvn().resolve(nutsFolder), removeThevpcMaven().resolve(nutsFolder));
    }

    private void runNutsPublishPreview() {
        echo("**** publish $nuts preview...", NMaps.of("nuts", NMsg.ofStyled("nuts", NTextStyle.keyword())));
        NId nid = NWorkspace.get().getApiId();
        remoteCopyFile(localMvn().resolve(Mvn.file(nid, MvnArtifactType.JAR)), remoteTheVpcNuts().resolve("nuts-preview.jar"));
    }


    private void runNutsPublishStable() {
        echo("**** publish $nuts stable...", NMaps.of("nuts", NMsg.ofStyled("nuts", NTextStyle.keyword())));
        String stableVersion = context().nutsStableVersion;
        String jarName = "nuts-" + stableVersion + ".jar";
        NAssert.requireNonBlank(stableVersion,"nutsStableVersion");
        NPath.of("https://repo1.maven.org/maven2/net/thevpc/nuts/nuts/" + stableVersion + "/"+jarName)
                        .copyTo(context().root.resolve("installers/nuts-release-tool/dist").resolve(jarName));

        upload(
                context().root.resolve("installers/nuts-release-tool/dist").resolve(jarName)
                , remoteTheVpcNuts().resolve("nuts-stable.jar")
        );
    }



}
