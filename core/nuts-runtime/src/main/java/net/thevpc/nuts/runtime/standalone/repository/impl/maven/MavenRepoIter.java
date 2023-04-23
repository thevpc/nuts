package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;

import java.io.IOException;
import java.io.InputStream;

class MavenRepoIter extends NIdPathIteratorBase {
    private MavenFolderRepository r;

    public MavenRepoIter(MavenFolderRepository r) {
        this.r = r;
    }

    @Override
    public void undeploy(NId id, NSession session) throws NExecutionException {
        r.undeploy().setId(id).setSession(session)
                //.setFetchMode(NutsFetchMode.LOCAL)
                .run();
    }

    @Override
    public boolean isDescFile(NPath pathname) {
        return pathname.getName().endsWith(".pom");
    }

    @Override
    public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NSession session, NPath rootURL) throws IOException {
        session.getTerminal().printProgress(NMsg.ofC("%-8s %s", "parse", pathname.toCompressedForm()));
        return MavenUtils.of(session).parsePomXmlAndResolveParents(in, fetchMode, pathname.toString(), repository);
    }

    @Override
    public NId parseId(NPath pomFile, NPath rootPath, NIdFilter filter, NRepository repository, NSession session) throws IOException {
        String fn = pomFile.getName();
        if (fn.endsWith(".pom")) {
            NPath versionFolder = pomFile.getParent();
            if (versionFolder != null) {
                String vn = versionFolder.getName();
                NPath artifactFolder = versionFolder.getParent();
                if (artifactFolder != null) {
                    String an = artifactFolder.getName();
                    if (fn.equals(an + "-" + vn + ".pom")) {
                        NPath groupFolder = artifactFolder.getParent();
                        if (groupFolder != null) {
                            NPath gg = groupFolder.subpath(rootPath.getLocationItemsCount(), groupFolder.getLocationItemsCount());
                            StringBuilder gn = new StringBuilder();
                            for (int i = 0; i < gg.getLocationItemsCount(); i++) {
                                String ns = gg.getLocationItem(i);
                                if (i > 0) {
                                    gn.append('.');
                                }
                                gn.append(ns);
                            }
                            return validate(
                                    NIdBuilder.of(gn.toString(),an)
                                            .setVersion(vn)
                                            .build(),
                                    null, pomFile, rootPath, filter, repository, session);
                        }
                    }
                }
            }
        }
        return null;
    }
}
