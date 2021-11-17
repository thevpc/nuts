///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.deprecated;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
//import net.thevpc.nuts.runtime.core.CoreNutsConstants;
//import net.thevpc.nuts.NutsLogVerb;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UncheckedIOException;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.logging.Level;
//import net.thevpc.nuts.runtime.core.expr.StringTokenizerUtils;
//import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
//import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
//import net.thevpc.nuts.runtime.core.filters.NutsSearchIdById;
//import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
//
///**
// * @author thevpc
// */
//public class FilesFoldersApi {
//
//    private static Item[] getDirHtmlfs(boolean folders, boolean files, String baseUrl, NutsSession session) {
//        //
//        List<Item> all = new ArrayList<>();
//        String dotFilesUrl = baseUrl;
////        NutsVersion versionString = ws.version().parser().parse("0.5.5");
//        try {
//            session.getTerminal().printProgress("%-8s %s","browse", NutsPath.of(baseUrl,session).toCompressedForm());
//            List<String> splitted = null;
//            try (InputStream foldersFileStream
//                    = NutsInputStreamMonitor.of(session).setSource(dotFilesUrl).create()) {
//                splitted = new WebHtmlListParser().parseHtml(foldersFileStream);
//            } catch (IOException ex) {
//                //
//            }
//            if (splitted != null) {
//                for (String s : splitted) {
//                    if (s.endsWith("/")) {
//                        s = s.substring(0, s.length() - 1);
//                        int y = s.lastIndexOf('/');
//                        if (y > 0) {
//                            s = s.substring(y + 1);
//                        }
//                        if (s.length() > 0 && !s.equals("..")) {
//                            if (folders) {
//                                all.add(new Item(true, s));
//                            }
//                        }
//                    } else {
//                        if (files) {
//                            int y = s.lastIndexOf('/');
//                            if (y > 0) {
//                                s = s.substring(y + 1);
//                            }
//                            all.add(new Item(false, s));
//                        }
//                    }
//                }
//            }
//        } catch (UncheckedIOException | NutsIOException ex) {
//            NutsLoggerOp.of(FilesFoldersApi.class,session).level(Level.FINE).verb(NutsLogVerb.FAIL)
//                    .log(NutsMessage.jstyle("unable to navigate : file not found {0}", dotFilesUrl));
//        }
//        return all.toArray(new Item[0]);
//    }
//
//    public static Item[] getDirItems(boolean folders, boolean files, RemoteRepoApi strategy, String baseUrl, NutsSession session) {
//        switch (strategy) {
//            case DOTFILEFS: {
//                return getDirDotfilefs(folders, files, baseUrl, session);
//            }
//            case HTMLFS: {
//                return getDirHtmlfs(folders, files, baseUrl, session);
//            }
//        }
//        throw new NutsUnexpectedException(session, NutsMessage.cstyle("unexpected strategy %s", strategy));
//    }
//
//    private static Item[] getDirDotfilefs(boolean folders, boolean files, String baseUrl, NutsSession session) {
//        List<Item> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
//        InputStream foldersFileStream = null;
//        String dotFilesUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FILES;
//        NutsVersion versionString = NutsVersion.of("0.5.5",session);
//        try {
//            session.getTerminal().printProgress("%-8s %s", "browse",NutsPath.of(baseUrl,session).toCompressedForm());
//            foldersFileStream = NutsInputStreamMonitor.of(session).setSource(dotFilesUrl).create();
//            List<String> splitted = StringTokenizerUtils.split(CoreIOUtils.loadString(foldersFileStream, true,session), "\n\r");
//            for (String s : splitted) {
//                s = s.trim();
//                if (s.length() > 0) {
//                    if (s.startsWith("#")) {
//                        if (all.isEmpty()) {
//                            s = s.substring(1).trim();
//                            if (s.startsWith("version=")) {
//                                versionString = NutsVersion.of(s.substring("version=".length()).trim(),session);
//                            }
//                        }
//                    } else {
//                        if (versionString.compareTo("0.5.7") < 0) {
//                            if (files) {
//                                all.add(new Item(false, s));
//                            } else {
//                                //ignore the rest
//                                break;
//                            }
//                        } else {
//                            //version 0.5.7 or later
//                            if (s.endsWith("/")) {
//                                s = s.substring(0, s.length() - 1);
//                                int y = s.lastIndexOf('/');
//                                if (y > 0) {
//                                    s = s.substring(y + 1);
//                                }
//                                if (s.length() > 0 && !s.equals("..")) {
//                                    if (folders) {
//                                        all.add(new Item(true, s));
//                                    }
//                                }
//                            } else {
//                                if (files) {
//                                    int y = s.lastIndexOf('/');
//                                    if (y > 0) {
//                                        s = s.substring(y + 1);
//                                    }
//                                    all.add(new Item(false, s));
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (UncheckedIOException | NutsIOException ex) {
//            NutsLoggerOp.of(FilesFoldersApi.class,session).level(Level.FINE).verb(NutsLogVerb.FAIL)
//                    .log(NutsMessage.jstyle("unable to navigate : file not found {0}", dotFilesUrl));
//        }
//        if (versionString.compareTo("0.5.7") < 0) {
//            if (folders) {
//                String[] foldersFileContent = null;
//                String dotFolderUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FOLDERS;
//                try (InputStream stream = NutsInputStreamMonitor.of(session).setSource(dotFolderUrl)
//                        .create()) {
//                    foldersFileContent = StringTokenizerUtils.split(CoreIOUtils.loadString(stream, true,session), "\n\r")
//                            .stream().map(x -> x.trim()).filter(x -> x.length() > 0).toArray(String[]::new);
//                } catch (IOException | UncheckedIOException | NutsIOException ex) {
//                    NutsLoggerOp.of(FilesFoldersApi.class,session).level(Level.FINE).verb(NutsLogVerb.FAIL)
//                            .log(NutsMessage.jstyle("unable to navigate : file not found {0}", dotFolderUrl));
//                }
//                if (foldersFileContent != null) {
//                    for (String folder : foldersFileContent) {
//                        all.add(new Item(true, folder));
//                    }
//                }
//            }
//        }
//        return all.toArray(new Item[0]);
//    }
//
//    public static Iterator<NutsId> createIterator(
//            NutsSession workspace, NutsRepository repository, String rootUrl, String basePath, NutsIdFilter filter, RemoteRepoApi strategy, NutsSession session, int maxDepth, IteratorModel model
//    ) {
//        return new FilesFoldersApiIdIterator(workspace, repository, rootUrl, basePath, filter, strategy, session, model, maxDepth);
//    }
//
//    public static abstract class AbstractIteratorModel implements IteratorModel {
//
//        public NutsId validate(NutsId id, NutsDescriptor t, String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
//            if (t != null) {
//                if (!CoreNutsUtils.isEffectiveId(t.getId())) {
//                    NutsDescriptor nutsDescriptor = null;
//                    try {
//                        nutsDescriptor = NutsWorkspaceExt.of(session.getWorkspace()).resolveEffectiveDescriptor(t, session);
//                    } catch (Exception ex) {
//                        NutsLoggerOp.of(FilesFoldersApi.class,session).level(Level.FINE).error(ex).log(
//                                NutsMessage.jstyle("error resolving effective descriptor for {0} in url {1} : {2}", t.getId(),
//                                pathname,
//                                ex));//e.printStackTrace();
//                    }
//                    t = nutsDescriptor;
//                }
//                if ((filter == null || filter.acceptSearchId(new NutsSearchIdByDescriptor(t), session))) {
//                    NutsId nutsId = t.getId().builder().setRepository(repository.getName()).build();
////                        nutsId = nutsId.setAlternative(t.getAlternative());
//                    return nutsId;
//                }
//            }
//            if (id != null) {
//                if ((filter == null || filter.acceptSearchId(new NutsSearchIdById(id), session))) {
//                    return id;
//                }
//            }
//            return null;
//        }
//
//        @Override
//        public NutsId parseId(String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException {
//            NutsDescriptor t = null;
//            try {
//                t = parseDescriptor(pathname, NutsInputStreamMonitor.of(session).setSource(pathname).create(),
//                        NutsFetchMode.LOCAL, repository, session, rootPath);
//            } catch (Exception ex) {
//                NutsLoggerOp.of(FilesFoldersApi.class,session).level(Level.FINE).error(ex)
//                        .log(NutsMessage.jstyle("error parsing url : {0} : {1}", pathname, toString()));//e.printStackTrace();
//            }
//            if (t != null) {
//                return validate(null, t, pathname, rootPath, filter, repository, session);
//            }
//            return null;
//        }
//
//    }
//
//    public interface IteratorModel {
//
//        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;
//
//        boolean isDescFile(String pathname);
//
//        NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session, String rootURL) throws IOException;
//
//        NutsId parseId(String pathname, String rootPath, NutsIdFilter filter, NutsRepository repository, NutsSession session) throws IOException;
//    }
//
//    //    private static final Logger LOG=Logger.getLogger(FilesFoldersApi.class.getName());
//    public static class Item {
//
//        boolean folder;
//        String name;
//
//        public Item(boolean folder, String name) {
//            this.folder = folder;
//            this.name = name;
//        }
//
//        public boolean isFolder() {
//            return folder;
//        }
//
//        public String getName() {
//            return name;
//        }
//    }
//
//}
