package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.NutsIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;

import java.io.IOException;
import java.io.InputStream;

class MavenRepoIter extends NutsIdPathIteratorBase {
    private MavenFolderRepository r;

    public MavenRepoIter(MavenFolderRepository r) {
        this.r = r;
    }

    @Override
    public void undeploy(NutsId id, NutsSession session) throws NutsExecutionException {
        r.undeploy().setId(id).setSession(session)
                //.setFetchMode(NutsFetchMode.LOCAL)
                .run();
    }

    @Override
    public boolean isDescFile(NutsPath pathname) {
        return pathname.getName().endsWith(".pom");
    }

    @Override
    public NutsDescriptor parseDescriptor(NutsPath pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, NutsPath rootURL) throws IOException {
        session.getTerminal().printProgress("%-8s %s", "parse", pathname.toCompressedForm());
        return MavenUtils.of(session).parsePomXmlAndResolveParents(in, fetchMode, pathname.toString(), repository);
    }

    @Override
    public NutsId parseId(NutsPath pomFile, NutsPath rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
        String fn = pomFile.getName();
        if (fn.endsWith(".pom")) {
            NutsPath versionFolder = pomFile.getParent();
            if (versionFolder != null) {
                String vn = versionFolder.getName();
                NutsPath artifactFolder = versionFolder.getParent();
                if (artifactFolder != null) {
                    String an = artifactFolder.getName();
                    if (fn.equals(an + "-" + vn + ".pom")) {
                        NutsPath groupFolder = artifactFolder.getParent();
                        if (groupFolder != null) {
                            NutsPath gg = groupFolder.subpath(rootPath.getPathCount(), groupFolder.getPathCount());
                            StringBuilder gn = new StringBuilder();
                            for (int i = 0; i < gg.getPathCount(); i++) {
                                String ns = gg.getItem(i);
                                if (i > 0) {
                                    gn.append('.');
                                }
                                gn.append(ns);
                            }
                            return validate(
                                    NutsIdBuilder.of(session)
                                            .setGroupId(gn.toString())
                                            .setArtifactId(an)
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
