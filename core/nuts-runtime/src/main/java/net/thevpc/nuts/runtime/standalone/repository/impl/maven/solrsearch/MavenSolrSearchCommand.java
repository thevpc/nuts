package net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.MavenFolderRepository;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * https://search.maven.org/classic/#api
 */
public class MavenSolrSearchCommand {
    //http://search.maven.org/solrsearch/select?q=g:"org.apache.maven.indexer"+AND+a:"maven-indexer"&rows=20&core=gav
    //http://search.maven.org/solrsearch/select?q=g:"net.thevpc.*"&rows=20&core=gav
    private MavenFolderRepository repo;

    public MavenSolrSearchCommand(MavenFolderRepository repo) {
        this.repo = repo;
    }

    public boolean isSolrSearchEnabled(NutsSession session) {
        NutsPath solrSearchUrl = getSolrSearchUrl(session);
        return solrSearchUrl != null && NutsUtilStrings.parseBoolean(repo.config().setSession(session).getConfigProperty("maven.solrsearch.enable", null), true, false);
    }

    public NutsIterator<NutsId> search(NutsIdFilter filter, NutsId[] baseIds, NutsFetchMode fetchMode, NutsSession session) {
        if(fetchMode==NutsFetchMode.REMOTE){
            if(isSolrSearchEnabled(session)){
                boolean someCorrect=false;
                boolean someIncorrect=false;
                List<NutsIterator<? extends NutsId>> list2 = new ArrayList<>();
                NutsPath solrSearchUrl=getSolrSearchUrl(session);
                for (NutsId baseId : baseIds) {
                    MavenSolrSearchRequest r=new MavenSolrSearchRequest(
                            baseId.getGroupId(),
                            baseId.getArtifactId()
                    );
                    Iterator<NutsId> ii = this.search(r, solrSearchUrl, filter, session);
                    if(ii!=null){
                        list2.add((NutsIterator) ii);
                        someCorrect=true;
                    }else {
                        return null;
                    }
                }
                if(someCorrect && !someIncorrect){
                    return IteratorUtils.concat(list2);
                }
            }
        }
        return null;
    }

    public NutsPath getSolrSearchUrl(NutsSession session) {
        String a = repo.config().setSession(session).getConfigProperty("maven.solrsearch.url", null);
        if (a != null) {
            return NutsPath.of(a, session);
        }
        return null;
    }

    public Iterator<NutsId> search(MavenSolrSearchRequest r, NutsPath url, NutsIdFilter idFilter, NutsSession session) {
        if (r != null) {
            String urlString = url.toString();
            if (urlString.startsWith("htmlfs:")) {
                urlString = urlString.substring("htmlfs:".length());
            }
            Map<String, String> m = r.toQueryMap();
            if (m != null) {
                StringBuilder q2 = new StringBuilder(urlString).append("?");
                int index = 0;
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    if (index > 0) {
                        q2.append("&");
                    }
                    try {
                        q2.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                    index++;
                }
                NutsPath query = NutsPath.of(q2.toString(), session);
                IteratorBuilder<NutsId> it = IteratorBuilder.<NutsId>ofSupplier(new Supplier<Iterator<NutsId>>() {
                    @Override
                    public Iterator<NutsId> get() {
                        return new Iterator<NutsId>() {
                            NutsArrayElement arr;
                            int index = 0;

                            @Override
                            public boolean hasNext() {
                                if (arr == null) {
                                    NutsElement e = NutsElements.of(session)
                                            .setLogProgress(true)
                                            .parse(query);
                                    if (e.isObject()) {
                                        NutsObjectElement o = e.asObject();
                                        String status = o.getSafeObject("responseHeader").getSafeString("status");
                                        if ("0".equals(status)) {
                                            arr = o.getSafeObject("response").getSafeArray("docs");
                                        }
                                    }
                                }
                                return index < arr.size();
                            }

                            @Override
                            public NutsId next() {
                                if (arr != null) {
                                    if (index < arr.size()) {
                                        NutsObjectElement d = arr.getObject(index);
                                        String g = d.getSafeString("g");
                                        String a = d.getSafeString("a");
                                        String v = d.getSafeString("v");
                                        index++;
                                        return new DefaultNutsIdBuilder().setGroupId(g).setArtifactId(a).setVersion(v).build();
                                    }
                                }
                                return null;
                            }
                        };
                    }
                }, (elems) -> elems.ofObject().set("url", query.toString()).build(), session);
                return it.filter(y->idFilter==null||idFilter.acceptId(y,session),elems->
                                elems.ofObject().set(
                                        "filterBy",elems.ofString(idFilter==null?"true":idFilter.toString())
                                ).build()
                        )
//                        .flatMap(
//                                NutsFunction.of(id -> repo.findNonSingleVersionImpl(id, idFilter, NutsFetchMode.REMOTE, session),
//                                        elems -> elems.ofObject().set("command", "search-versions").build())
//                        )
                        .build();
            }
        }
        return null;
    }
}
