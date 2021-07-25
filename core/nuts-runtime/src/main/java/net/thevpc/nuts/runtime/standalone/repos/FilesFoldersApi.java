/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.NutsLogVerb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.bundles.parsers.StringTokenizerUtils;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdById;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

/**
 * @author thevpc
 */
public class FilesFoldersApi {

    private static Item[] getDirList(boolean folders, boolean files, String baseUrl, NutsSession session) {
        //
        List<Item> all = new ArrayList<>();
        NutsWorkspace ws = session.getWorkspace();
        String dotFilesUrl = baseUrl;
//        NutsVersion versionString = ws.version().parser().parse("0.5.5");
        try {
            session.getTerminal().printProgress("%-8s %s","browse", session.getWorkspace().io().path(baseUrl).compressedForm());
            List<String> splitted = null;
            try (InputStream foldersFileStream
                    = ws.io().monitor().setSource(dotFilesUrl).setSession(session).create()) {
                splitted = new WebHtmlListParser().parse(foldersFileStream);
            } catch (IOException ex) {
                //
            }
            if (splitted != null) {
                for (String s : splitted) {
                    if (s.endsWith("/")) {
                        s = s.substring(0, s.length() - 1);
                        int y = s.lastIndexOf('/');
                        if (y > 0) {
                            s = s.substring(y + 1);
                        }
                        if (s.length() > 0 && !s.equals("..")) {
                            if (folders) {
                                all.add(new Item(true, s));
                            }
                        }
                    } else {
                        if (files) {
                            int y = s.lastIndexOf('/');
                            if (y > 0) {
                                s = s.substring(y + 1);
                            }
                            all.add(new Item(false, s));
                        }
                    }
                }
            }
        } catch (UncheckedIOException | NutsIOException ex) {
            ws.log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to navigate : file not found {0}", dotFilesUrl);
        }
        return all.toArray(new Item[0]);
    }

    public static Item[] getDirItems(boolean folders, boolean files, RemoteRepoApi strategy, String baseUrl, NutsSession session) {
        switch (strategy) {
            case DIR_TEXT: {
                return getDirText(folders, files, baseUrl, session);
            }
            case DIR_LIST: {
                return getDirList(folders, files, baseUrl, session);
            }
        }
        throw new NutsUnexpectedException(session, NutsMessage.cstyle("unexpected strategy %s", strategy));
    }

    private static Item[] getDirText(boolean folders, boolean files, String baseUrl, NutsSession session) {
        List<Item> all = new ArrayList<>();
        NutsWorkspace ws = session.getWorkspace();
        InputStream foldersFileStream = null;
        String dotFilesUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FILES;
        NutsVersion versionString = ws.version().parser().parse("0.5.5");
        try {
            session.getTerminal().printProgress("%-8s %s", "browse",session.getWorkspace().io().path(baseUrl).compressedForm());
            foldersFileStream = ws.io().monitor().setSource(dotFilesUrl).setSession(session).create();
            List<String> splitted = StringTokenizerUtils.split(CoreIOUtils.loadString(foldersFileStream, true), "\n\r");
            for (String s : splitted) {
                s = s.trim();
                if (s.length() > 0) {
                    if (s.startsWith("#")) {
                        if (all.isEmpty()) {
                            s = s.substring(1).trim();
                            if (s.startsWith("version=")) {
                                versionString = ws.version().parser().parse(s.substring("version=".length()).trim());
                            }
                        }
                    } else {
                        if (versionString.compareTo("0.5.7") < 0) {
                            if (files) {
                                all.add(new Item(false, s));
                            } else {
                                //ignore the rest
                                break;
                            }
                        } else {
                            //version 0.5.7 or later
                            if (s.endsWith("/")) {
                                s = s.substring(0, s.length() - 1);
                                int y = s.lastIndexOf('/');
                                if (y > 0) {
                                    s = s.substring(y + 1);
                                }
                                if (s.length() > 0 && !s.equals("..")) {
                                    if (folders) {
                                        all.add(new Item(true, s));
                                    }
                                }
                            } else {
                                if (files) {
                                    int y = s.lastIndexOf('/');
                                    if (y > 0) {
                                        s = s.substring(y + 1);
                                    }
                                    all.add(new Item(false, s));
                                }
                            }
                        }
                    }
                }
            }
        } catch (UncheckedIOException | NutsIOException ex) {
            ws.log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to navigate : file not found {0}", dotFilesUrl);
        }
        if (versionString.compareTo("0.5.7") < 0) {
            if (folders) {
                String[] foldersFileContent = null;
                String dotFolderUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FOLDERS;
                try (InputStream stream = ws.io().monitor().setSource(dotFolderUrl)
                        .setSession(session).create()) {
                    foldersFileContent = StringTokenizerUtils.split(CoreIOUtils.loadString(stream, true), "\n\r")
                            .stream().map(x -> x.trim()).filter(x -> x.length() > 0).toArray(String[]::new);
                } catch (IOException | UncheckedIOException | NutsIOException ex) {
                    ws.log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to navigate : file not found {0}", dotFolderUrl);
                }
                if (foldersFileContent != null) {
                    for (String folder : foldersFileContent) {
                        all.add(new Item(true, folder));
                    }
                }
            }
        }
        return all.toArray(new Item[0]);
    }

    public static Iterator<NutsId> createIterator(
            NutsWorkspace workspace, NutsRepository repository, String rootUrl, String basePath, NutsIdFilter filter, RemoteRepoApi strategy, NutsSession session, int maxDepth, IteratorModel model
    ) {
        return new FilesFoldersApiIdIterator(workspace, repository, rootUrl, basePath, filter, strategy, session, model, maxDepth);
    }

    public static abstract class AbstractIteratorModel implements IteratorModel {

        public NutsId validate(NutsId id, NutsDescriptor t, String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
            if (t != null) {
                if (!CoreNutsUtils.isEffectiveId(t.getId())) {
                    NutsDescriptor nutsDescriptor = null;
                    try {
                        nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
                    } catch (Exception ex) {
                        session.getWorkspace().log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).error(ex).log(
                                "error resolving effective descriptor for {0} in url {1} : {2}", t.getId(),
                                pathname,
                                ex);//e.printStackTrace();
                    }
                    t = nutsDescriptor;
                }
                if ((filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
                    NutsId nutsId = t.getId().builder().setNamespace(repository.getName()).build();
//                        nutsId = nutsId.setAlternative(t.getAlternative());
                    return nutsId;
                }
            }
            if (id != null) {
                if ((filter == null || filter.acceptSearchId(new NutsSearchIdById(id), session))) {
                    return id;
                }
            }
            return null;
        }

        @Override
        public NutsId parseId(String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
            NutsDescriptor t = null;
            try {
                t = parseDescriptor(pathname, session.getWorkspace().io()
                        .monitor().setSource(pathname).setSession(session).create(),
                        NutsFetchMode.LOCAL, repository, session, rootPath);
            } catch (Exception ex) {
                session.getWorkspace().log().of(FilesFoldersApi.class).with().session(session).level(Level.FINE).error(ex).log("error parsing url : {0} : {1}", pathname, toString());//e.printStackTrace();
            }
            if (t != null) {
                return validate(null, t, pathname, rootPath, filter, repository, session);
            }
            return null;
        }

    }

    public interface IteratorModel {

        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;

        boolean isDescFile(String pathname);

        NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, String rootURL) throws IOException;

        NutsId parseId(String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException;
    }

    //    private static final Logger LOG=Logger.getLogger(FilesFoldersApi.class.getName());
    public static class Item {

        boolean folder;
        String name;

        public Item(boolean folder, String name) {
            this.folder = folder;
            this.name = name;
        }

        public boolean isFolder() {
            return folder;
        }

        public String getName() {
            return name;
        }
    }

}
