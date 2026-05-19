package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.util.NCoreLogUtils;
import net.thevpc.nuts.text.NMsg;

import java.io.InputStream;

class MavenRepoIter extends NIdPathIteratorBase {
    private MavenFolderRepository r;

    public MavenRepoIter(MavenFolderRepository r) {
        this.r = r;
    }


    @Override
    public void undeploy(NId id) throws NExecutionException {
        r.undeploy().setId(id)
                //.setFetchMode(NutsFetchMode.LOCAL)
                .run();
    }
    @Override
    public NWorkspace getWorkspace() {
        return r.workspace();
    }
    @Override
    public boolean isDescFile(NPath pathname) {
        return pathname.name().endsWith(".pom");
    }

    @Override
    public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NPath rootURL) {
        NSession session = getWorkspace().currentSession();
        session.terminal().printProgress(NMsg.ofC("%-8s %s", "parse", NCoreLogUtils.forProgress(pathname)));
        return MavenUtils.of().parsePomXmlAndResolveParents(in, fetchMode, pathname.toString(), repository);
    }

    @Override
    public NId parseId(NPath pomFile, NPath rootPath, NDefinitionFilter filter, NRepository repository)  {
        String fn = pomFile.name();
        if (fn.endsWith(".pom")) {
            NPath versionFolder = pomFile.parent();
            if (versionFolder != null) {
                String vn = versionFolder.name();
                NPath artifactFolder = versionFolder.parent();
                if (artifactFolder != null) {
                    String an = artifactFolder.name();
                    if (fn.equals(an + "-" + vn + ".pom")) {
                        NPath groupFolder = artifactFolder.parent();
                        if (groupFolder != null) {
                            NPath gg = groupFolder.subpath(rootPath.nameCount(), groupFolder.nameCount());
                            StringBuilder gn = new StringBuilder();
                            for (int i = 0; i < gg.nameCount(); i++) {
                                String ns = gg.nameAt(i);
                                if (i > 0) {
                                    gn.append('.');
                                }
                                gn.append(ns);
                            }
                            return validate(
                                    NIdBuilder.of(gn.toString(),an)
                                            .version(vn)
                                            .build(),
                                    null, pomFile, rootPath, filter, repository);
                        }
                    }
                }
            }
        }
        return null;
    }
}
