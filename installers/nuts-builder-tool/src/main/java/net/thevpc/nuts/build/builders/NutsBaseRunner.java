package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.thevpc.nuts.build.util.Mvn;
import net.thevpc.nuts.build.util.MvnArtifactType;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringUtils;

public class NutsBaseRunner extends AbstractRunner {

    public NutsBaseRunner(NSession context) {
        super(context);
    }

    @Override
    public void configureDefaults() {
        context().NUTS_INSTALLER_VERSION = Nuts.getVersion() + ".0";
        context().NUTS_ROOT_BASE = NPath.ofUserDirectory(session).normalize();
        NPath conf = NPath.of("nuts-build-release.conf", session);
        if (conf.exists()) {
            NRef<Boolean> wrap = NRef.of(false);
            NRef<StringBuilder> lastValue = NRef.of(new StringBuilder());
            NRef<Integer> lineNumber = NRef.of(0);
            conf.getLines().forEach(line -> {
                lineNumber.set(lineNumber.get() + 1);
                String toProcessLine = null;
                if (wrap.get()) {
                    if (line.endsWith("\\")) {
                        lastValue.get().append(line.substring(0, line.length() - 1));
                    } else {
                        lastValue.get().append(line);
                        wrap.set(false);
                        toProcessLine = lastValue.get().toString();
                    }
                } else {
                    String tline = line.trim();
                    if (tline.length() > 0 && !tline.startsWith("#")) {
                        if (line.endsWith("\\")) {
                            String timmedStart = NStringUtils.trimLeft(line);
                            String remainingLine = timmedStart.substring(0, timmedStart.length() - 1);
                            if (timmedStart.length() > 0) {
                                lastValue.set(new StringBuilder(remainingLine));
                                wrap.set(true);
                            } else {
                                lastValue.set(new StringBuilder(""));
                                wrap.set(true);
                            }
                        } else {
                            toProcessLine = line.trim();
                        }
                    }
                }
                if (toProcessLine != null && toProcessLine.length() > 0) {
                    int eq = toProcessLine.indexOf("=");
                    if (eq >= 0) {
                        String k = toProcessLine.substring(0, eq).trim();
                        String v = toProcessLine.substring(eq + 1);
                        if ("OPTIONS".equals(k)) {
                            context().options.addAll(NCmdLine.parseDefault(v).get().toStringList());
                        } else {
                            context().vars.put(k, v);
                        }
                    } else {
                        throw new IllegalArgumentException("[line:" + lineNumber.get() + "] expected key=value : " + toProcessLine);
                    }
                }
            });
        }
    }

    @Override
    public void buildConfiguration() {
        setRemoteSshConnexion(context().user + "@thevpc.net");
    }
    

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--update-version": {
                cmdLine.withNextFlag((v, a, s) -> context().updateVersion = v);
                return true;
            }
            case "--keep-stamp": {
                cmdLine.withNextFlag((v, a, s) -> context().keepStamp = v);
                return true;
            }
            case "--production-mode": {
                cmdLine.withNextFlag((v, a, s) -> context().productionMode = v);
                return true;
            }
            case "--publish-preview": {
                cmdLine.withNextFlag((v, a, s) -> context().publishNutsPreview = v);
                return true;
            }
            case "--remote-user": {
                cmdLine.withNextEntry((v, a, s) -> context().user = v);
                return true;
            }
        }
        return false;
    }

    public boolean isAnyCommandArmed() {
        return isAnyNutsArmed();
    }

    @Override
    public void run() {
        if (context().publishNutsPreview) {
            runNutsPublishPreview();
        }
    }

    private boolean isAnyNutsArmed() {
        return true;//publishNutsPreview;
    }

    private void runNutsPublishPreview() {
        echo("**** publish $nuts preview...", NMaps.of("nuts", NMsg.ofStyled("nuts", NTextStyle.keyword())));
        NId nid = session.getWorkspace().getApiId();
//        pushIdFiles(session,
//                "net.thevpc.nuts:nuts#0.8.4",
//                "net.thevpc.nuts:nuts-runtime#0.8.4.0",
//                "net.thevpc.nuts.toolbox:nsh#0.8.4.0"
//        );
        String nutsFolder = Mvn.folder(NId.of("net.thevpc:nuts").get());
        pushFolder(localMvn().resolve(nutsFolder), removeMvn().resolve(nutsFolder));
        remoteCopyFolder(removeMvn().resolve(nutsFolder), removeMavenThevpc().resolve(nutsFolder));
        remoteCopyFile(localMvn().resolve(Mvn.file(nid, MvnArtifactType.JAR)), remoteNutsInstall().resolve("nuts-preview.jar"));
    }

    

}
