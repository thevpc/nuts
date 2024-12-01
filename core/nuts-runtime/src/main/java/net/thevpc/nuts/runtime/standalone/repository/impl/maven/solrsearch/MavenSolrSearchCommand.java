package net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.MavenFolderRepository;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NLiteral;

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

    public boolean isSolrSearchEnabled() {
        NPath solrSearchUrl = getSolrSearchUrl();
        String configProperty = repo.config().getConfigProperty("maven.solrsearch.enable").flatMap(NLiteral::asString).orNull();
        return solrSearchUrl != null
                && NLiteral.of(configProperty).asBoolean()
                .ifEmpty(true).orElse(false);
    }

    public NIterator<NId> search(NIdFilter filter, NId[] baseIds, NFetchMode fetchMode) {
        if(fetchMode== NFetchMode.REMOTE){
            if(isSolrSearchEnabled()){
                boolean someCorrect=false;
                boolean someIncorrect=false;
                List<NIterator<? extends NId>> list2 = new ArrayList<>();
                NPath solrSearchUrl=getSolrSearchUrl();
                for (NId baseId : baseIds) {
                    MavenSolrSearchRequest r=new MavenSolrSearchRequest(
                            baseId.getGroupId(),
                            baseId.getArtifactId()
                    );
                    Iterator<NId> ii = this.search(r, solrSearchUrl, filter);
                    if(ii!=null){
                        list2.add((NIterator) ii);
                        someCorrect=true;
                    }else {
                        return null;
                    }
                }
                if(someCorrect && !someIncorrect){
                    return NIteratorUtils.concat(list2);
                }
            }
        }
        return null;
    }

    public NPath getSolrSearchUrl() {
        String a = repo.config().getConfigProperty("maven.solrsearch.url").flatMap(NLiteral::asString).orNull();
        if (a != null) {
            return NPath.of(a);
        }
        return null;
    }

    public Iterator<NId> search(MavenSolrSearchRequest r, NPath url, NIdFilter idFilter) {
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
                NPath query = NPath.of(q2.toString());
                NIteratorBuilder<NId> it = NIteratorBuilder.<NId>ofSupplier(new Supplier<Iterator<NId>>() {
                    @Override
                    public Iterator<NId> get() {
                        return new Iterator<NId>() {
                            NArrayElement arr;
                            int index = 0;

                            @Override
                            public boolean hasNext() {
                                if (arr == null) {
                                    NElement e = NElements.of()
                                            .setLogProgress(true)
                                            .parse(query);
                                    if (e.isObject()) {
                                        NObjectElement o = e.asObject().get();
                                        String status = o.getStringByPath("responseHeader","status").orElse("");
                                        if ("0".equals(status)) {
                                            arr = o.getArrayByPath("response","docs").orElse(NArrayElement.ofEmpty());
                                        }
                                    }
                                }
                                return index < arr.size();
                            }

                            @Override
                            public NId next() {
                                if (arr != null) {
                                    if (index < arr.size()) {
                                        NObjectElement d = arr.getObject(index).get();
                                        String g = d.getString("g").orElse("");
                                        String a = d.getString("a").orElse("");
                                        String v = d.getString("v").orElse("");
                                        index++;
                                        return NIdBuilder.of(g,a).setVersion(v).build();
                                    }
                                }
                                return null;
                            }
                        };
                    }
                }, () -> NElements.of().ofObject().set("url", query.toString()).build());
                return it.filter(y->idFilter==null||idFilter.acceptId(y),
                        ()->
                                        NElements.of().ofObject().set(
                                        "filterBy", NElements.of().ofString(idFilter==null?"true":idFilter.toString())
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
