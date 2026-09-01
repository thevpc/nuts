package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.spi.base.NPredicateBase;

public class NDefinitionFilterToNIdPredicate2 extends NPredicateBase<NId> {
    private final NDefinitionFilter filter;

    public NDefinitionFilterToNIdPredicate2(NDefinitionFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean test(NId t) {
        if (filter == null) {
            return true;
        }
        return filter.acceptDefinition(NDefinitionHelper.ofDefinition(t));
    }

    @Override
    public String toString() {
        return filter.toString();
    }
}
