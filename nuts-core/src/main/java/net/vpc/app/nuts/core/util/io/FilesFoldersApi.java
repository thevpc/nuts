/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Iterator;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class FilesFoldersApi {

    public static String[] getFolders(String baseUrl, NutsSession session) {
        InputStream foldersFileStream = null;
        String foldersFileUrl = baseUrl + "/.folders";
        String[] foldersFileContent = null;
        try {
            foldersFileStream = session.workspace().io().monitor().source(foldersFileUrl).session(session).create();
            foldersFileContent = CoreStringUtils.split(CoreIOUtils.loadString(foldersFileStream, true), "\n\r")
                    .stream().map(x->x.trim()).filter(x->x.length()>0).toArray(String[]::new);
        } catch (UncheckedIOException ex) {
            //
        }
        return foldersFileContent;
    }

    public static String[] getFiles(String baseUrl, NutsSession session) {
        InputStream foldersFileStream = null;
        String foldersFileUrl = baseUrl + "/.files";
        String[] foldersFileContent = null;
        try {
            foldersFileStream = session.workspace().io().monitor().source(foldersFileUrl).session(session).create();
            foldersFileContent = CoreStringUtils.split(CoreIOUtils.loadString(foldersFileStream, true), "\n\r")
                    .stream().map(String::trim).filter(x->x.length()>0).toArray(String[]::new);
        } catch (UncheckedIOException ex) {
            //
        }
        return foldersFileContent;
    }

    public static Iterator<NutsId> createIterator(
            NutsWorkspace workspace, String repository, String rootUrl, String basePath, NutsIdFilter filter, NutsRepositorySession session, int maxDepth, IteratorModel model
    ) {
        return new FilesFoldersApiIdIterator(workspace, repository, rootUrl, basePath, filter, session, model, maxDepth);
    }

    public interface IteratorModel {

        void undeploy(NutsId id, NutsRepositorySession session) throws NutsExecutionException;

        boolean isDescFile(String pathname);

        NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsRepositorySession session) throws IOException;
    }

}
