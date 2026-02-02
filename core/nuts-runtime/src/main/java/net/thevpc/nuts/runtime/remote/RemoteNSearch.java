package net.thevpc.nuts.runtime.remote;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.command.NSearch;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElementBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.search.AbstractNSearch;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NScorable;

import java.util.Iterator;
import java.util.List;

@NScore(fixed = NScorable.UNSUPPORTED_SCORE)
public class RemoteNSearch extends AbstractNSearch {

    public RemoteNSearch() {
        super();
    }

    @Override
    public NSearch copy() {
        RemoteNSearch b = new RemoteNSearch();
        b.copyFrom(this);
        return b;
    }

    @Override
    public NFetch toFetch() {
        return null;
    }

    @Override
    protected NIterator<NId> getResultIdIteratorBase(Boolean forceInlineDependencies) {
        NObjectElementBuilder eb = NElement.ofObjectBuilder()
                .set("execType", getExecType().id())
                .set("targetApiVersion", getTargetApiVersion().toString())
                .set("ids", NElement.ofArrayBuilder().addAll(getIds().stream()
                        .map(Object::toString).toArray(String[]::new)).build());
        if (getDefinitionFilter() != null) {
            eb.set("filter", NElements.of().toElement(getDefinitionFilter()));
        }
        if (getRepositoryFilter() != null) {
            eb.set("repositories", NElement.ofString(getRepositoryFilter().toString()));
        }

        RemoteNWorkspace ws=(RemoteNWorkspace) NWorkspace.get();
        return NIterator.of(
                (Iterator <NId>)
                        ws.remoteCall(
                                ws.createCall(
                                "workspace.searchIds",
                                eb.build()
                        ),
                        List.class
                ).iterator()
        ).withDescription(NDescribables.ofDesc("searchRemoteIds"));
    }


}
