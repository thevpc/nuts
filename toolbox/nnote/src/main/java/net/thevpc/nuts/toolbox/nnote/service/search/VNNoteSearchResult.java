/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.search;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringSearchResult;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;

/**
 *
 * @author vpc
 */
public class VNNoteSearchResult {

    private VNNote rootNote;
    private Stream<StringSearchResult<VNNote>> stream;

    public VNNoteSearchResult(VNNote rootNote, Stream<StringSearchResult<VNNote>> stream) {
        this.rootNote = rootNote;
        this.stream = stream;
    }

    public VNNote getRootNote() {
        return rootNote;
    }

    public List<StringSearchResult<VNNote>> list() {
        return stream.collect(Collectors.toList());
    }

    public Stream<StringSearchResult<VNNote>> stream() {
        return stream;
    }

}
