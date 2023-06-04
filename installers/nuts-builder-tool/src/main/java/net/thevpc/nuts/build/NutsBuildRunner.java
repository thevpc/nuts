package net.thevpc.nuts.build;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.AbstractRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NChronometer;
import net.thevpc.nuts.util.NMaps;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NutsBuildRunner extends AbstractRunner {
    private boolean keepStamp = false;
    private boolean updateVersion = false;
    private boolean publishNutsPreview = false;
    private Boolean productionMode = null;
    private String home = System.getProperty("user.home");
    private String user = System.getProperty("user.name");

    public NutsBuildRunner(NSession context) {
        super(context);
    }

    public void configure(String... args) {
        configure(NCmdLine.of(args));
    }

    public void configure(NCmdLine cmdLine) {
        while (cmdLine.hasNext()) {
            NArg c = cmdLine.peek().orNull();
            switch (c.key()) {
                case "--update-version": {
                    cmdLine.withNextFlag((v, a, s) -> updateVersion = v);
                    break;
                }
                case "--keep-stamp": {
                    cmdLine.withNextFlag((v, a, s) -> keepStamp = v);
                    break;
                }
                case "--production-mode": {
                    cmdLine.withNextFlag((v, a, s) -> productionMode = v);
                    break;
                }
                case "--publish-preview": {
                    cmdLine.withNextFlag((v, a, s) -> publishNutsPreview = v);
                    break;
                }
                case "--remote-user": {
                    cmdLine.withNextEntry((v, a, s) -> user = v);
                    break;
                }
                default: {
                    if (!session.configureFirst(cmdLine)) {
                        cmdLine.throwUnexpectedArgument();
                    }
                }
            }
        }
    }

    public boolean isAnyCommandArmed() {
        return isAnyNutsArmed();
    }


    public void run() {
        NChronometer chrono = NChronometer.startNow();
        setRemoteSshConnexion(user + "@thevpc.net");
        if (!isAnyCommandArmed()) {
            throw new NExecutionException(session, NMsg.ofPlain("expected command. try --all"), NExecutionException.ERROR_1);
        }
        if (isAnyNutsArmed()) {
            runNutsPublishPreview();
        }

        echo("Process finished in " + chrono.stop());
    }

    private boolean isAnyNutsArmed() {
        return publishNutsPreview;
    }

    private void runNutsPublishPreview() {
        if(publishNutsPreview) {
            echo("**** publish $nuts preview...", NMaps.of("nuts", NMsg.ofStyled("nuts", NTextStyle.keyword())));
            String nutsVersion = session.getWorkspace().getApiId().getVersion().toString();
//        pushIdFiles(session,
//                "net.thevpc.nuts:nuts#0.8.4",
//                "net.thevpc.nuts:nuts-runtime#0.8.4.0",
//                "net.thevpc.nuts.toolbox:nsh#0.8.4.0"
//        );

            pushFolder(home + "/.m2/repository/net/thevpc/nuts", home + "/.m2/repository/net/thevpc/");
            remoteCopyFolder(home + "/.m2/repository/net/thevpc/nuts", home + "/srv/maven-thevpc/net/thevpc/");
            remoteCopyFile(home + "/.m2/repository/net/thevpc/nuts/nuts/" + nutsVersion + "/nuts-" + nutsVersion + ".jar",
                    home + "/srv/tomcat/webapps-thevpc/ROOT/nuts-preview.jar");
        }
    }


    private void pushIdFiles(NSession session, String... ids) {

        NPath m2Path = NPath.of(home + "/.m2/repository", session);
        NPath mavenThevpc = NPath.of(home + "/srv/maven-thevpc/", session);
        class PathWithPrio {
            String p;
            NPath path;
            int prio;

            public PathWithPrio(String p, NPath path, int prio) {
                this.p = p;
                this.path = path;
                this.prio = prio;
            }
        }
        List<PathWithPrio> todo = new ArrayList<>();
        for (String sid : ids) {
            NId id = NId.of(sid).get();
            String g = String.join("/", id.getGroupId().split("[.]")) + "/" + id.getArtifactId() + "/" + id.getVersion().toString();
            NPath folder = NPath.ofUserHome(session).resolve(".m2/repository/")
                    .resolve(g);
            for (NPath sub : folder.list()) {
                if (sub.isRegularFile()) {
                    String name = sub.getName();
                    todo.add(
                            new PathWithPrio(
                                    g + "/" + name,
                                    sub,
                                    name.endsWith(".pom") ? 1 :
                                            name.endsWith(".asc") ? 2 :
                                                    name.endsWith("-sources.jar") ? 5 :
                                                            name.endsWith("-javadoc.jar") ? 6 :
                                                                    name.endsWith(".jar") ? 3 :
                                                                            name.endsWith(".repositories") ? 7 :
                                                                                    10
                            )
                    );
                }
            }
        }
        todo.sort(Comparator.comparing(x -> x.prio));
        for (PathWithPrio pathWithPrio : todo) {
            pushFile(pathWithPrio.path, m2Path.resolve(pathWithPrio.p).toString());
            remoteCopyFile(
                    m2Path.resolve(pathWithPrio.p).toString(),
                    mavenThevpc.resolve(pathWithPrio.p).toString()
            );
        }

    }


}
