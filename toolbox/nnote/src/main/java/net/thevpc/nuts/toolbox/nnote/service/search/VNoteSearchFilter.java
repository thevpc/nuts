/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search;

import java.util.stream.Stream;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.NNoteService;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringSearchResult;

/**
 *
 * @author vpc
 */
public interface VNoteSearchFilter {

    Stream<StringSearchResult<VNNote>> search(VNNote note, NNoteService service);
}
