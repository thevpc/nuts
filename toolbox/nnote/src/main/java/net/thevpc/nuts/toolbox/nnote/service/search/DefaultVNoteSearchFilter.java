/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search;

import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringQuerySearch;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringSearchResult;
import java.util.stream.Stream;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;

/**
 *
 * @author vpc
 */
public class DefaultVNoteSearchFilter implements VNoteSearchFilter {

    private StringQuerySearch p;

    public DefaultVNoteSearchFilter(String query) {
        p = new StringQuerySearch(query);
    }

    @Override
    public Stream<StringSearchResult<VNNote>> search(VNNote node, NNoteService service) {
        return p.search(new VNNoteStringToPatternHandler(service, node));
    }

}
