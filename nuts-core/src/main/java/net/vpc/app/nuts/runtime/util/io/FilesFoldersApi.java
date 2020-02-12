/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.runtime.DefaultNutsVersion;
import net.vpc.app.nuts.runtime.log.NutsLogVerb;
import net.vpc.app.nuts.runtime.util.SearchTraceHelper;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 * @author vpc
 */
public class FilesFoldersApi {
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

    public static Item[] getFilesAndFolders(boolean files, boolean folders, String baseUrl, NutsSession session) {
        List<Item> all = new ArrayList<>();

        InputStream foldersFileStream = null;
        String dotFilesUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FILES;
        NutsVersion versionString= DefaultNutsVersion.valueOf("0.5.5");
        try {
            SearchTraceHelper.progressIndeterminate("search "+CoreIOUtils.compressUrl(baseUrl),session);
            foldersFileStream = session.workspace().io().monitor().source(dotFilesUrl).session(session).create();
            List<String> splitted = CoreStringUtils.split(CoreIOUtils.loadString(foldersFileStream, true), "\n\r");
            for (String s : splitted) {
                s=s.trim();
                if(s.length()>0) {
                    if (s.startsWith("#")) {
                        if (all.isEmpty()) {
                            s=s.substring(1).trim();
                            if (s.startsWith("version=")) {
                                versionString = DefaultNutsVersion.valueOf(s.substring("version=".length()).trim());
                            }
                        }
                    } else {
                        if(versionString.compareTo("0.5.7")<0){
                            if(files){
                                all.add(new Item(false,s));
                            }else{
                                //ignore the rest
                                break;
                            }
                        }else{
                            //version 0.5.7 or later
                            if(s.endsWith("/")){
                                s=s.substring(0,s.length()-1);
                                if(s.length()>0){
                                    if(folders) {
                                        all.add(new Item(true, s));
                                    }
                                }
                            }else{
                                if(files) {
                                    all.add(new Item(false, s));
                                }
                            }
                        }
                    }
                }
            }
        } catch (UncheckedIOException ex) {
            session.workspace().log().of(FilesFoldersApi.class).with().level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to navigate : file not found {0}",dotFilesUrl);
        }
        if(versionString.compareTo("0.5.7")<0){
            if (folders) {
                String[] foldersFileContent = null;
                String dotFolderUrl = baseUrl + "/" + CoreNutsConstants.Files.DOT_FOLDERS;
                try (InputStream stream = session.workspace().io().monitor().source(dotFolderUrl)
                        .session(session).create()){
                    foldersFileContent = CoreStringUtils.split(CoreIOUtils.loadString(stream, true), "\n\r")
                            .stream().map(x -> x.trim()).filter(x -> x.length() > 0).toArray(String[]::new);
                } catch (IOException | UncheckedIOException ex) {
                    session.workspace().log().of(FilesFoldersApi.class).with().level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to navigate : file not found {0}",dotFolderUrl);
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
            NutsWorkspace workspace, NutsRepository repository, String rootUrl, String basePath, NutsIdFilter filter, NutsSession session, int maxDepth, IteratorModel model
    ) {
        return new FilesFoldersApiIdIterator(workspace, repository, rootUrl, basePath, filter, session, model, maxDepth);
    }

    public interface IteratorModel {

        void undeploy(NutsId id, NutsSession session) throws NutsExecutionException;

        boolean isDescFile(String pathname);

        NutsDescriptor parseDescriptor(String pathname, InputStream in, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) throws IOException;
    }

}
